package com.dreamgames.backendengineeringcasestudy.service.cache;

import com.dreamgames.backendengineeringcasestudy.model.entity.*;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import com.dreamgames.backendengineeringcasestudy.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.ADD_TOURNAMENT_REWARD_BATCH_SIZE;
import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.*;

@Service
@RequiredArgsConstructor
public class TournamentCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final GroupService groupService;
    private final UserCacheService userCacheService;

    public Optional<TournamentInfo> getTournamentInfo() {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(REDIS_TOURNAMENT_INFO_KEY);

        if (map.isEmpty()) {
            return Optional.empty();
        }
        TournamentInfo tournamentInfo = new TournamentInfo();

        tournamentInfo.setTournamentId((String) map.get(REDIS_TOURNAMENT_ID_KEY));
        tournamentInfo.setIsActive((String) map.get(REDIS_TOURNAMENT_IS_ACTIVE_KEY));

        return Optional.of(tournamentInfo);
    }

    public void setTournamentInfo(TournamentInfo tournamentInfo) {
        Map<String, String> map = new HashMap<>();
        map.put(REDIS_TOURNAMENT_ID_KEY, tournamentInfo.getTournamentId());
        map.put(REDIS_TOURNAMENT_IS_ACTIVE_KEY, tournamentInfo.getIsActive());
        redisTemplate.opsForHash().putAll(REDIS_TOURNAMENT_INFO_KEY, map);
    }

    public boolean isThereActiveTournament() {
        var tournamentInfo = getTournamentInfo();
        return tournamentInfo.filter(info -> Objects.equals(info.getIsActive(), "true")).isPresent();
    }

    public void addUserToGroupLeaderboard(long groupId, String userId) {
        redisTemplate.opsForZSet().add(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + groupId, userId, 0);
    }

    public GroupLeaderboard getGroupLeaderboard(long groupId, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + groupId, start, end);

        if (typedTuples == null || typedTuples.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leaderboard not found");
        }

        List<String> userIds = typedTuples.stream().map(ZSetOperations.TypedTuple::getValue).toList();
        Map<String, String> usernames = userCacheService.getUsernamesFromIds(userIds);


        GroupLeaderboard groupLeaderboard = new GroupLeaderboard();
        List<UserScore> userScores = new ArrayList<>();

        typedTuples.forEach(t -> {
            userScores.add(new UserScore(usernames.get(t.getValue()), Objects.requireNonNull(t.getScore()).intValue()));
        });

        groupLeaderboard.setUserScores(userScores);

        return groupLeaderboard;
    }

    public GroupLeaderboard getGroupLeaderboardByUserId(long userId, long start, long end) {

        Long groupId = groupService.getUsersGroupId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in a group"));

        return getGroupLeaderboard(groupId, start, end);
    }

    public CountryLeaderboard getCountriesLeaderboard(long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(REDIS_COUNTRY_LEADERBOARD_KEY, start, end);

        if (typedTuples == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leaderboard not found");
        }

        CountryLeaderboard countryLeaderboard = new CountryLeaderboard();
        List<CountryScore> countryScores = new ArrayList<>();
        typedTuples.forEach(t -> countryScores.add(new CountryScore(Country.valueOf(t.getValue()), Objects.requireNonNull(t.getScore()).intValue())));

        countryLeaderboard.setCountryScores(countryScores);

        return countryLeaderboard;

    }

    public void createCountryLeaderboard() {
        Country[] countries = Country.values();
        for (Country country : countries) {
            redisTemplate.opsForZSet().add(REDIS_COUNTRY_LEADERBOARD_KEY, country.name(), 0);
        }
    }

    public void incrementLeaderboardScore(long groupId, long userId, String country) {
        redisTemplate.opsForZSet().incrementScore(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + groupId, String.valueOf(userId), 1);
        redisTemplate.opsForZSet().incrementScore(REDIS_COUNTRY_LEADERBOARD_KEY, country, 1);
    }

    public int getGroupRank(long userId) {


        Long groupId = groupService.getUsersGroupId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in a group"));

        Long rank = redisTemplate.opsForZSet()
                .reverseRank(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + groupId, String.valueOf(userId));

        if (rank == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in leaderboard");
        }
        return rank.intValue();
    }

    public Cursor<String> getActiveGroupsInBatches() {
        return redisTemplate.opsForSet().scan(REDIS_ACTIVE_GROUPS_KEY, ScanOptions.scanOptions().count(ADD_TOURNAMENT_REWARD_BATCH_SIZE).build());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<LinkedHashSet<DefaultTypedTuple>> getGroupLeaderboardsInBatches(List<String> groupIds) {
        List<Object> res = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (String groupId : groupIds) {
                connection.zSetCommands().zRevRangeWithScores((REDIS_GROUP_LEADERBOARD_KEY_PREFIX + groupId).getBytes(), 0, 1);
            }
            return null;
        });

        return res.stream().map(o -> (LinkedHashSet<DefaultTypedTuple>) o).toList();

    }

    public void deleteAllTournamentData() {
        redisTemplate.delete(REDIS_ACTIVE_GROUPS_KEY);
        redisTemplate.delete(REDIS_COUNTRY_LEADERBOARD_KEY);
        redisTemplate.delete(REDIS_TOURNAMENT_INFO_KEY);

        int batchsize = 1000;

        Cursor<String> leaderboardCursor = redisTemplate.scan(ScanOptions.scanOptions()
                .match(REDIS_ALL_GROUP_LEADERBOARDS_PATTERN)
                .count(batchsize).build());

        int counter = 0;
        List<String> leaderboardBatches = new ArrayList<>();
        while (leaderboardCursor.hasNext()) {
            leaderboardBatches.add(leaderboardCursor.next());
            counter++;
            if (counter == batchsize) {
                redisTemplate.unlink(leaderboardBatches);
                leaderboardBatches.clear();
                counter = 0;
            }
        }

        // for residual batches
        redisTemplate.unlink(leaderboardBatches);

        counter = 0;

        List<String> groupBatches = new ArrayList<>();
        Cursor<String> groupCursor = redisTemplate.scan(ScanOptions.scanOptions().match(REDIS_ALL_GROUPS_PATTERN).count(1000).build());
        while (groupCursor.hasNext()) {
            groupBatches.add(groupCursor.next());
            counter++;
            if (counter == batchsize) {
                redisTemplate.unlink(groupBatches);
                groupBatches.clear();
                counter = 0;
            }
        }
        // for residual batches
        redisTemplate.unlink(groupBatches);

    }


}

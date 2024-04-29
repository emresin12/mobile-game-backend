package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.TournamentDao;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotActiveException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.dto.UserTournamentRewardDto;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentCacheService tournamentCacheService;
    private final TournamentDao tournamentDao;
    private final GroupService groupService;
    private final UserService userService;


    public long createTournament() {
        return tournamentDao.createTournament();
    }

    public void endTournament(long tournamentId) {
        tournamentDao.endTournament(tournamentId);
    }

    public TournamentInfo getActiveTournamentInfo() {
        return tournamentDao.getActiveTournamentInfo().orElseThrow(() ->
                new TournamentNotActiveException("No active tournament found"));
    }

    public TournamentInfo getTournamentInfoById() {
        return tournamentDao.getTournamentInfoById().orElseThrow(() ->
                new TournamentNotFoundException("No tournament found"));
    }

    public void addTournamentRewardsToUsers() {

        Cursor<String> activeGroupsCursor = tournamentCacheService.getActiveGroupsInBatches();

        List<String> groupIdBatch = new ArrayList<>();
        int counter = 0;

        ExecutorService executor = Executors.newFixedThreadPool(ADD_TOURNAMENT_REWARD_THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (activeGroupsCursor.hasNext()) {
            groupIdBatch.add(activeGroupsCursor.next());
            counter++;
            if (counter == ADD_TOURNAMENT_REWARD_BATCH_SIZE) {
                // When the batch size is reached, process the batch in a separate thread concurrently
                List<String> batch = new ArrayList<>(groupIdBatch);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    var groupLeaderboardBatches = tournamentCacheService.getGroupLeaderboardsInBatches(batch);
                    tournamentDao.batchInsertUserRewards(convertToUserTournamentRewardDto(groupLeaderboardBatches));
                }, executor);
                futures.add(future);
                groupIdBatch.clear();
                counter = 0;
            }
        }
        // residual batch
        if (!groupIdBatch.isEmpty()) {
            var groupLeaderboardBatches = tournamentCacheService.getGroupLeaderboardsInBatches(groupIdBatch);
            tournamentDao.batchInsertUserRewards(convertToUserTournamentRewardDto(groupLeaderboardBatches));
        }

        // wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();

        activeGroupsCursor.close();

    }

    @SuppressWarnings("rawtypes")
    public List<UserTournamentRewardDto> convertToUserTournamentRewardDto(List<LinkedHashSet<DefaultTypedTuple>> leaderboards) {
        List<UserTournamentRewardDto> rewardDtos = new ArrayList<>();
        for (LinkedHashSet<DefaultTypedTuple> leaderboard : leaderboards) {

            DefaultTypedTuple[] groupStats = leaderboard.toArray(DefaultTypedTuple[]::new);

            UserTournamentRewardDto firstUserRewardDto = new UserTournamentRewardDto();
            firstUserRewardDto.setUserId(Long.parseLong((String) Objects.requireNonNull(groupStats[0].getValue())));
            firstUserRewardDto.setReward(TOURNAMENT_REWARD_FIRST_PLACE);

            UserTournamentRewardDto secondUserRewardDto = new UserTournamentRewardDto();
            secondUserRewardDto.setUserId(Long.parseLong((String) Objects.requireNonNull(groupStats[1].getValue())));
            secondUserRewardDto.setReward(TOURNAMENT_REWARD_SECOND_PLACE);

            rewardDtos.add(firstUserRewardDto);
            rewardDtos.add(secondUserRewardDto);
        }

        return rewardDtos;
    }

    public void depositTournamentFeeToUsers() {
        List<Long> inactiveGroupIds = groupService.getInactiveGroups();
        userService.depositTournamentFeeToUsers(inactiveGroupIds);
    }

    public int getUserRank(long userId) {
        return tournamentCacheService.getGroupRank(userId);
    }

}

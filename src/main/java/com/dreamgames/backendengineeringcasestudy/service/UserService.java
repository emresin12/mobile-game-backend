package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.UserDao;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentEligibilityException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotActiveException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotFoundException;
import com.dreamgames.backendengineeringcasestudy.exception.UserNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.entity.GroupLeaderboard;
import com.dreamgames.backendengineeringcasestudy.model.entity.StaticUserData;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import com.dreamgames.backendengineeringcasestudy.model.entity.User;
import com.dreamgames.backendengineeringcasestudy.model.request.CreateUserRequest;
import com.dreamgames.backendengineeringcasestudy.service.cache.GroupCacheService;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import com.dreamgames.backendengineeringcasestudy.service.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final GroupService groupService;
    private final RewardService rewardService;
    private final UserCacheService userCacheService;
    private final TournamentCacheService tournamentCacheService;
    private final GroupCacheService groupCacheService;

    public User getUserById(long userId) {
        return userDao.getUserById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }


    public User createUser(CreateUserRequest user) {

        Long userId = userDao.createUser(user);

        User newUser = getUserById(userId);

        StaticUserData staticUserData = new StaticUserData();
        staticUserData.setUserId(String.valueOf(userId));
        staticUserData.setUsername(newUser.getUsername());
        staticUserData.setCountry(newUser.getCountry().name());

        userCacheService.setUserInfo(staticUserData);

        return newUser;
    }


    public User progressUserLevel(long userId) {
        // check if user exists
        int affectedRows = userDao.incrementUserStats(userId, 1, COIN_REWARD_PER_LEVEL);
        if (affectedRows != 1) {
            throw new UserNotFoundException("User not found");
        }

        // get group id of user
        Optional<Long> groupId = groupService.getUsersGroupId(userId);

        if (groupId.isPresent()) {
            // competing in tournament
            boolean isActive = groupCacheService.isGroupActive(groupId.get()).orElse(false);
            if (isActive) {
                // get country from cache and increment leaderboard scores
                String country = userCacheService.getUserCountry(userId);
                tournamentCacheService.incrementLeaderboardScore(groupId.get(), userId, country);
            }

        }

        return userDao.getUserById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    }

    public void validateUserTournamentEligibility(User user) {
        if (user.getLevel() < TOURNAMENT_REQUIRED_LEVEL) {
            throw new TournamentEligibilityException("User level is not enough for tournament");
        }
        if (user.getCoins() < TOURNAMENT_ENTRANCE_FEE) {
            throw new TournamentEligibilityException("User coins are not enough for tournament");
        }
        if (rewardService.hasUserPendingReward(user.getUserId())) {
            throw new TournamentEligibilityException("User has a pending reward to claim. Please claim it first.");
        }
        // check if user is already in a group
        if (groupService.getUsersGroupId(user.getUserId()).isPresent()) {
            throw new TournamentEligibilityException("User is already in a group");
        }
    }

    public void makeUserTournamentEntranceTransaction(long userId) {
        userDao.updateUserCoins(userId, -TOURNAMENT_ENTRANCE_FEE);
    }

    private User validateUser(long userId) {
        return userDao.getUserById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private TournamentInfo validateTournament() {
        TournamentInfo tournamentInfo = tournamentCacheService.getTournamentInfo().orElseThrow(() -> new TournamentNotFoundException("No tournament data found"));

        if (tournamentInfo.getIsActive().equals("false")) {
            throw new TournamentNotActiveException("Tournament is not active");
        }
        return tournamentInfo;
    }


    public GroupLeaderboard enterTournament(Long userId) {

        TournamentInfo tournamentInfo = validateTournament();
        User user = validateUser(userId);
        validateUserTournamentEligibility(user);


        // Consider distributed locks in order to scale the application.
        Long groupId;
        synchronized (this) {
            groupId = groupService.findAvailableGroup(userId, user.getCountry());
            if (groupId == null) {
                // no existing group found, create a new group
                groupId = groupService.createGroupWithAUser(Long.parseLong(tournamentInfo.getTournamentId()), userId, user.getCountry());
                groupCacheService.createEmptyGroup(groupId);
            }
        }

        long groupCount = groupCacheService.incrementGroupCount(groupId);
        if (groupCount == 5) {
            //set group as active to start competing
            groupService.setGroupStatus(groupId, true);
            groupCacheService.setGroupStatus(groupId, true);
            groupCacheService.addGroupToActiveGroups(groupId);
        }

        tournamentCacheService.addUserToGroupLeaderboard(groupId, userId.toString());

        makeUserTournamentEntranceTransaction(userId);

        return tournamentCacheService.getGroupLeaderboard(groupId, 0, 5);

    }

    @Transactional
    public User claimReward(long userId) {
        int reward = rewardService.getUserReward(userId);
        rewardService.removeUserReward(userId);
        userDao.updateUserCoins(userId, reward);
        return getUserById(userId);
    }


    public void depositTournamentFeeToUsers(List<Long> userIds) {
        userDao.depositCoinsToUsers(userIds, TOURNAMENT_ENTRANCE_FEE);
    }

}

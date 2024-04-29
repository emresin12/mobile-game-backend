package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.UserDao;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentEligibilityException;
import com.dreamgames.backendengineeringcasestudy.exception.UserNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.entity.GroupLeaderboard;
import com.dreamgames.backendengineeringcasestudy.model.entity.StaticUserData;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import com.dreamgames.backendengineeringcasestudy.model.entity.User;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import com.dreamgames.backendengineeringcasestudy.model.request.CreateUserRequest;
import com.dreamgames.backendengineeringcasestudy.service.cache.GroupCacheService;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import com.dreamgames.backendengineeringcasestudy.service.cache.UserCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;


    @Mock
    private GroupService groupService;

    @Mock
    private TournamentCacheService tournamentCacheService;

    @Mock
    private GroupCacheService groupCacheService;


    @Mock
    private UserCacheService userCacheService;


    @Mock
    private RewardService rewardService;

    @InjectMocks
    private UserService userService;

    private User user;
    private User newUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setLevel(STARTING_LEVEL);
        user.setCoins(STARTING_COINS);
        user.setCountry(Country.TURKEY);

        newUser = new User();
        newUser.setUserId(2L);
        newUser.setUsername("newuser");
        newUser.setLevel(STARTING_LEVEL);
        newUser.setCoins(STARTING_COINS);
        newUser.setCountry(Country.TURKEY);


    }

    @Test
    void getUserById_ValidUserId_ReturnsUser() {
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals(user, result);
        verify(userDao).getUserById(1L);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserNotFound() {
        when(userDao.getUserById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUserById(1L));
        verify(userDao).getUserById(1L);
    }

    @Test
    void createUser_ValidRequest_ReturnsCreatedUser() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setLevel(STARTING_LEVEL);
        request.setCoins(STARTING_COINS);
        request.setCountry(Country.TURKEY);

        StaticUserData staticUserData = new StaticUserData();
        staticUserData.setUserId(String.valueOf(2L));
        staticUserData.setUsername(newUser.getUsername());
        staticUserData.setCountry(newUser.getCountry().name());

        when(userDao.createUser(request)).thenReturn(2L);
        when(userDao.getUserById(2L)).thenReturn(Optional.of(newUser));

        User result = userService.createUser(request);

        assertEquals(newUser, result);
        assertEquals(2L, result.getUserId());

        verify(userDao).createUser(request);
        verify(userDao).getUserById(2L);
        verify(userCacheService).setUserInfo(staticUserData);
    }

    @Test
    void createUser_ErrorCreatingUser_ThrowsResponseStatusException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setLevel(STARTING_LEVEL);
        request.setCoins(STARTING_COINS);
        request.setCountry(Country.TURKEY);

        when(userDao.createUser(request)).thenReturn(2L);
        when(userDao.getUserById(2L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.createUser(request));

        verify(userDao).createUser(request);
        verify(userDao).getUserById(2L);
        verify(userCacheService, never()).setUserInfo(any(StaticUserData.class));
    }

    @Test
    void progressUserLevel_ValidUserNotInGroup_houldIncrementUserLevelAndCoins() {
        User updatedUser = new User();
        updatedUser.setUserId(1L);
        updatedUser.setUsername("updatedUser");
        updatedUser.setLevel(STARTING_LEVEL + 1);
        updatedUser.setCoins(STARTING_COINS + COIN_REWARD_PER_LEVEL);
        updatedUser.setCountry(Country.TURKEY);

        when(userDao.incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL)).thenReturn(1);
        // not in tournament
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.empty());
        when(userDao.getUserById(1L)).thenReturn(Optional.of(updatedUser));

        User result = userService.progressUserLevel(1L);

        assertEquals(updatedUser, result);
        verify(userDao).incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL);
        verify(groupService).getUsersGroupId(1L);
        verify(userDao).getUserById(1L);
        verify(tournamentCacheService, never()).incrementLeaderboardScore(anyLong(), anyLong(), anyString());
    }

    @Test
    void progressUserLevel_ValidUserInActiveGroup_ReturnsUpdatedUserAndUpdatesLeaderboard() {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setUsername("updatedUser");
        updatedUser.setLevel(user.getLevel() + 1);
        updatedUser.setCoins(user.getCoins() + COIN_REWARD_PER_LEVEL);
        updatedUser.setCountry(user.getCountry());

        when(userDao.incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL)).thenReturn(1);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.of(1L));
        when(groupCacheService.isGroupActive(1L)).thenReturn(Optional.of(true));
        when(userCacheService.getUserCountry(1L)).thenReturn(user.getCountry().name());
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));

        User result = userService.progressUserLevel(1L);

        assertEquals(user, result);
        verify(userDao).incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL);
        verify(groupService).getUsersGroupId(1L);
        verify(userDao).getUserById(1L);
        verify(userCacheService).getUserCountry(1L);
        verify(tournamentCacheService).incrementLeaderboardScore(1L, 1L, "TURKEY");
    }

    @Test
    void progressUserLevel_ValidUserInInactiveGroup_ReturnsUpdatedUserAndUpdatesLeaderboard() {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setUsername("updatedUser");
        updatedUser.setLevel(user.getLevel() + 1);
        updatedUser.setCoins(user.getCoins() + COIN_REWARD_PER_LEVEL);
        updatedUser.setCountry(user.getCountry());

        when(userDao.incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL)).thenReturn(1);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.of(1L));
        when(groupCacheService.isGroupActive(1L)).thenReturn(Optional.of(false));
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));

        User result = userService.progressUserLevel(1L);

        assertEquals(user, result);
        verify(userDao).incrementUserStats(1L, 1, COIN_REWARD_PER_LEVEL);
        verify(groupService).getUsersGroupId(1L);
        verify(userDao).getUserById(1L);
        verify(userCacheService, never()).getUserCountry(anyLong());
        verify(tournamentCacheService, never()).incrementLeaderboardScore(anyLong(), anyLong(), anyString());
    }


    @Test
    void progressUserLevel_InvalidUser_ThrowsResponseStatusException() {
        when(userDao.incrementUserStats(2L, 1, COIN_REWARD_PER_LEVEL)).thenReturn(0);

        assertThrows(UserNotFoundException.class, () -> userService.progressUserLevel(2L));
        verify(userDao).incrementUserStats(2L, 1, COIN_REWARD_PER_LEVEL);
        verify(groupService, never()).getUsersGroupId(anyLong());
        verify(userDao, never()).getUserById(anyLong());
        verify(userCacheService, never()).getUserCountry(anyLong());
        verify(tournamentCacheService, never()).incrementLeaderboardScore(anyLong(), anyLong(), anyString());
    }

    @Test
    void validateUserTournamentEligibility_NotEnoughUserLevel_shouldThrowException() {
        user.setLevel(TOURNAMENT_REQUIRED_LEVEL - 1);

        assertThrows(TournamentEligibilityException.class, () -> userService.validateUserTournamentEligibility(user));

        verify(rewardService, never()).hasUserPendingReward(anyLong());
        verify(groupService, never()).getUsersGroupId(anyLong());
    }

    @Test
    void validateUserTournamentEligibility_NotEnoughCoins_shouldThrowException() {
        user.setCoins(TOURNAMENT_ENTRANCE_FEE - 1);

        assertThrows(TournamentEligibilityException.class, () -> userService.validateUserTournamentEligibility(user));

        verify(rewardService, never()).hasUserPendingReward(anyLong());
        verify(groupService, never()).getUsersGroupId(anyLong());
    }

    @Test
    void validateUserTournamentEligibility_UserHasPendingReward_shouldThrowException() {
        when(rewardService.hasUserPendingReward(1L)).thenReturn(true);

        assertThrows(TournamentEligibilityException.class, () -> userService.validateUserTournamentEligibility(user));

        verify(rewardService).hasUserPendingReward(1L);
        verify(groupService, never()).getUsersGroupId(anyLong());
    }

    @Test
    void validateUserTournamentEligibility_AlreadyInGroupUser_shouldThrowException() {
        when(rewardService.hasUserPendingReward(1L)).thenReturn(false);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.of(1L));

        assertThrows(TournamentEligibilityException.class, () -> userService.validateUserTournamentEligibility(user));

        verify(rewardService).hasUserPendingReward(1L);
        verify(groupService).getUsersGroupId(1L);
    }

    @Test
    void makeUserTournamentEntranceTransaction_shouldUpdateUserCoins() {
        userService.makeUserTournamentEntranceTransaction(1L);

        verify(userDao).updateUserCoins(1L, -TOURNAMENT_ENTRANCE_FEE);
    }

    @Test
    void enterTournament_ThereIsAvailableGroupWithNotActiveGroup_shouldEnterUserIntoTournament() {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setTournamentId("1");
        tournamentInfo.setIsActive("true");

        when(tournamentCacheService.getTournamentInfo()).thenReturn(Optional.of(tournamentInfo));
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));
        when(rewardService.hasUserPendingReward(1L)).thenReturn(false);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.empty());
        when(groupService.findAvailableGroup(1L, Country.TURKEY)).thenReturn(1L);
        when(groupCacheService.incrementGroupCount(1L)).thenReturn(2L);
        when(tournamentCacheService.getGroupLeaderboard(1L, 0, 5)).thenReturn(new GroupLeaderboard());

        GroupLeaderboard result = userService.enterTournament(1L);

        assertNotNull(result);
        verify(groupService, never()).setGroupStatus(anyLong(), anyBoolean());
        verify(groupCacheService, never()).setGroupStatus(anyLong(), anyBoolean());
        verify(groupCacheService, never()).addGroupToActiveGroups(anyLong());
        verify(tournamentCacheService).addUserToGroupLeaderboard(1L, "1");
        verify(tournamentCacheService).getGroupLeaderboard(1L, 0, 5);
    }

    @Test
    void enterTournament_NoAvailableGroup_shouldCreateNewGroup() {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setTournamentId("1");
        tournamentInfo.setIsActive("true");

        when(tournamentCacheService.getTournamentInfo()).thenReturn(Optional.of(tournamentInfo));
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));
        when(rewardService.hasUserPendingReward(1L)).thenReturn(false);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.empty());
        when(groupService.findAvailableGroup(1L, Country.TURKEY)).thenReturn(null);
        when(groupService.createGroupWithAUser(1L, 1L, Country.TURKEY)).thenReturn(1L);
        when(groupCacheService.incrementGroupCount(1L)).thenReturn(1L);
        when(tournamentCacheService.getGroupLeaderboard(1L, 0, 5)).thenReturn(new GroupLeaderboard());

        GroupLeaderboard result = userService.enterTournament(1L);

        assertNotNull(result);
        verify(groupService).createGroupWithAUser(1L, 1L, Country.TURKEY);
        verify(groupCacheService).createEmptyGroup(1L);
        verify(tournamentCacheService).addUserToGroupLeaderboard(1L, "1");
        verify(groupCacheService).incrementGroupCount(1L);

        verify(groupService, never()).setGroupStatus(anyLong(), anyBoolean());
        verify(groupCacheService, never()).setGroupStatus(anyLong(), anyBoolean());
        verify(groupCacheService, never()).addGroupToActiveGroups(anyLong());

        verify(tournamentCacheService).addUserToGroupLeaderboard(1L, "1");
        verify(tournamentCacheService).getGroupLeaderboard(1L, 0, 5);
    }

    @Test
    void enterTournament_ThereIsAvailableGroupWithActiveGroup_shouldEnterUserIntoTournament() {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setTournamentId("1");
        tournamentInfo.setIsActive("true");

        when(tournamentCacheService.getTournamentInfo()).thenReturn(Optional.of(tournamentInfo));
        when(userDao.getUserById(1L)).thenReturn(Optional.of(user));
        when(rewardService.hasUserPendingReward(1L)).thenReturn(false);
        when(groupService.getUsersGroupId(1L)).thenReturn(Optional.empty());
        when(groupService.findAvailableGroup(1L, Country.TURKEY)).thenReturn(1L);
        when(groupCacheService.incrementGroupCount(1L)).thenReturn(5L);
        when(tournamentCacheService.getGroupLeaderboard(1L, 0, 5)).thenReturn(new GroupLeaderboard());

        GroupLeaderboard result = userService.enterTournament(1L);

        assertNotNull(result);
        verify(groupService).setGroupStatus(1L, true);
        verify(groupCacheService).setGroupStatus(1L, true);
        verify(groupCacheService).addGroupToActiveGroups(1L);
        verify(tournamentCacheService).addUserToGroupLeaderboard(1L, "1");
    }

    @Test
    void claimReward_shouldClaimUserRewardAndUpdateUserCoins() {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setLevel(user.getLevel());
        updatedUser.setCoins(user.getCoins() + 1000);


        when(rewardService.getUserReward(1L)).thenReturn(1000);
        when(userDao.getUserById(1L)).thenReturn(Optional.of(updatedUser));

        User result = userService.claimReward(1L);

        assertEquals(updatedUser.getUserId(), result.getUserId());
        assertEquals(updatedUser.getCoins(), result.getCoins());
        verify(rewardService).removeUserReward(1L);
        verify(userDao).updateUserCoins(1L, 1000);
    }

    @Test
    void depositTournamentFeeToUsers_shouldDepositCoinsToUsers() {
        List<Long> userIds = List.of(1L, 2L);

        userService.depositTournamentFeeToUsers(userIds);

        verify(userDao).depositCoinsToUsers(userIds, TOURNAMENT_ENTRANCE_FEE);
    }


}
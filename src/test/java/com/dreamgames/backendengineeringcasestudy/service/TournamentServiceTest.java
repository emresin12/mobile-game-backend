package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.TournamentDao;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotActiveException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.dto.UserTournamentRewardDto;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.TOURNAMENT_REWARD_FIRST_PLACE;
import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.TOURNAMENT_REWARD_SECOND_PLACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentCacheService tournamentCacheService;

    @Mock
    private TournamentDao tournamentDao;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        // Additional setup if needed
    }

    @Test
    void createTournament_shouldCreateNewTournament() {
        long expectedTournamentId = 1L;
        when(tournamentDao.createTournament()).thenReturn(expectedTournamentId);

        long actualTournamentId = tournamentService.createTournament();

        assertEquals(expectedTournamentId, actualTournamentId);
        verify(tournamentDao).createTournament();
    }

    @Test
    void endTournament_shouldEndTournament() {
        long tournamentId = 1L;

        tournamentService.endTournament(tournamentId);

        verify(tournamentDao).endTournament(tournamentId);
    }

    @Test
    void getActiveTournamentInfo_shouldReturnActiveTournamentInfo() {
        TournamentInfo expectedTournamentInfo = new TournamentInfo();
        when(tournamentDao.getActiveTournamentInfo()).thenReturn(Optional.of(expectedTournamentInfo));

        TournamentInfo actualTournamentInfo = tournamentService.getActiveTournamentInfo();

        assertEquals(expectedTournamentInfo, actualTournamentInfo);
        verify(tournamentDao).getActiveTournamentInfo();
    }

    @Test
    void getActiveTournamentInfo_shouldThrowExceptionWhenNoActiveTournament() {
        when(tournamentDao.getActiveTournamentInfo()).thenReturn(Optional.empty());

        assertThrows(TournamentNotActiveException.class, () -> tournamentService.getActiveTournamentInfo());
        verify(tournamentDao).getActiveTournamentInfo();
    }

    @Test
    void getTournamentInfoById_shouldReturnTournamentInfo() {
        long tournamentId = 1L;
        TournamentInfo expectedTournamentInfo = new TournamentInfo();
        when(tournamentDao.getTournamentInfoById()).thenReturn(Optional.of(expectedTournamentInfo));

        TournamentInfo actualTournamentInfo = tournamentService.getTournamentInfoById();

        assertEquals(expectedTournamentInfo, actualTournamentInfo);
        verify(tournamentDao).getTournamentInfoById();
    }

    @Test
    void getTournamentInfoById_shouldThrowExceptionWhenTournamentNotFound() {
        long tournamentId = 1L;
        when(tournamentDao.getTournamentInfoById()).thenReturn(Optional.empty());

        assertThrows(TournamentNotFoundException.class, () -> tournamentService.getTournamentInfoById());
        verify(tournamentDao).getTournamentInfoById();
    }

    @Test
    void addTournamentRewardsToUsers_shouldAddRewardsToUsers() {
        // Mocking the behavior of tournamentCacheService and tournamentDao
        Cursor<String> mockCursor = mock(Cursor.class);
        when(tournamentCacheService.getActiveGroupsInBatches()).thenReturn(mockCursor);
        when(mockCursor.hasNext()).thenReturn(true, true, false);
        when(mockCursor.next()).thenReturn("1", "2");

        LinkedHashSet<DefaultTypedTuple> leaderboard1 = new LinkedHashSet<>();
        leaderboard1.add(new DefaultTypedTuple<>("1", 100.0));
        leaderboard1.add(new DefaultTypedTuple<>("2", 90.0));

        LinkedHashSet<DefaultTypedTuple> leaderboard2 = new LinkedHashSet<>();
        leaderboard2.add(new DefaultTypedTuple<>("3", 80.0));
        leaderboard2.add(new DefaultTypedTuple<>("4", 70.0));

        List<LinkedHashSet<DefaultTypedTuple>> groupLeaderboards = new ArrayList<>();
        groupLeaderboards.add(leaderboard1);
        groupLeaderboards.add(leaderboard2);

        List<UserTournamentRewardDto> userRewards = new ArrayList<>();
        UserTournamentRewardDto userReward1 = new UserTournamentRewardDto();
        userReward1.setUserId(1L);
        userReward1.setReward(TOURNAMENT_REWARD_FIRST_PLACE);
        userRewards.add(userReward1);

        UserTournamentRewardDto userReward2 = new UserTournamentRewardDto();
        userReward2.setUserId(2L);
        userReward2.setReward(TOURNAMENT_REWARD_SECOND_PLACE);
        userRewards.add(userReward2);

        UserTournamentRewardDto userReward3 = new UserTournamentRewardDto();
        userReward3.setUserId(3L);
        userReward3.setReward(TOURNAMENT_REWARD_FIRST_PLACE);
        userRewards.add(userReward3);

        UserTournamentRewardDto userReward4 = new UserTournamentRewardDto();
        userReward4.setUserId(4L);
        userReward4.setReward(TOURNAMENT_REWARD_SECOND_PLACE);
        userRewards.add(userReward4);

        when(tournamentCacheService.getGroupLeaderboardsInBatches(List.of("1", "2"))).thenReturn(groupLeaderboards);

        tournamentService.addTournamentRewardsToUsers();

        verify(tournamentDao, times(1)).batchInsertUserRewards(userRewards);
    }

    @Test
    void depositTournamentFeeToUsers_shouldDepositFeeToUsers() {
        List<Long> inactiveGroupIds = List.of(1L, 2L);
        when(groupService.getInactiveGroups()).thenReturn(inactiveGroupIds);

        tournamentService.depositTournamentFeeToUsers();

        verify(groupService).getInactiveGroups();
        verify(userService).depositTournamentFeeToUsers(inactiveGroupIds);
    }

    @Test
    void getUserRank_shouldReturnUserRank() {
        long userId = 1L;
        int expectedRank = 3;
        when(tournamentCacheService.getGroupRank(userId)).thenReturn(expectedRank);

        int actualRank = tournamentService.getUserRank(userId);

        assertEquals(expectedRank, actualRank);
        verify(tournamentCacheService).getGroupRank(userId);
    }
}
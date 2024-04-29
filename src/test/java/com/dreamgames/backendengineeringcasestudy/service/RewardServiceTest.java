package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.RewardDao;
import com.dreamgames.backendengineeringcasestudy.exception.RewardAssignmentException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardDeletionException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardRetrievalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardDao rewardDao;

    @InjectMocks
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        // Additional setup if needed
    }

    @Test
    void assignRewardToUser_shouldAssignRewardSuccessfully() {
        long userId = 1L;
        int reward = 100;

        when(rewardDao.assignRewardToUser(userId, reward)).thenReturn(true);

        assertDoesNotThrow(() -> rewardService.assignRewardToUser(userId, reward));
        verify(rewardDao).assignRewardToUser(userId, reward);
    }

    @Test
    void assignRewardToUser_shouldThrowExceptionWhenAssignmentFails() {
        long userId = 1L;
        int reward = 100;

        when(rewardDao.assignRewardToUser(userId, reward)).thenReturn(false);

        assertThrows(RewardAssignmentException.class,
                () -> rewardService.assignRewardToUser(userId, reward));

        verify(rewardDao).assignRewardToUser(userId, reward);
    }

    @Test
    void getUserReward_shouldReturnUserReward() {
        long userId = 1L;
        int reward = 100;

        when(rewardDao.getUserReward(userId)).thenReturn(Optional.of(reward));

        int result = rewardService.getUserReward(userId);

        assertEquals(reward, result);
        verify(rewardDao).getUserReward(userId);
    }

    @Test
    void getUserReward_shouldThrowExceptionWhenRewardNotFound() {
        long userId = 1L;

        when(rewardDao.getUserReward(userId)).thenReturn(Optional.empty());

        assertThrows(RewardRetrievalException.class, () -> rewardService.getUserReward(userId));
        verify(rewardDao).getUserReward(userId);
    }

    @Test
    void isUserRewardClaimed_shouldReturnTrueWhenRewardIsClaimed() {
        long userId = 1L;

        when(rewardDao.getUserReward(userId)).thenReturn(Optional.of(100));

        boolean result = rewardService.hasUserPendingReward(userId);

        assertTrue(result);
        verify(rewardDao).getUserReward(userId);
    }

    @Test
    void isUserRewardClaimed_shouldReturnFalseWhenRewardIsNotClaimed() {
        long userId = 1L;

        when(rewardDao.getUserReward(userId)).thenReturn(Optional.empty());

        boolean result = rewardService.hasUserPendingReward(userId);

        assertFalse(result);
        verify(rewardDao).getUserReward(userId);
    }

    @Test
    void removeUserReward_shouldRemoveRewardSuccessfully() {
        long userId = 1L;

        when(rewardDao.deleteUserReward(userId)).thenReturn(true);

        assertDoesNotThrow(() -> rewardService.removeUserReward(userId));
        verify(rewardDao).deleteUserReward(userId);
    }

    @Test
    void removeUserReward_shouldThrowExceptionWhenRewardNotFound() {
        long userId = 1L;

        when(rewardDao.deleteUserReward(userId)).thenReturn(false);

        assertThrows(RewardDeletionException.class,
                () -> rewardService.removeUserReward(userId));

        verify(rewardDao).deleteUserReward(userId);
    }
}
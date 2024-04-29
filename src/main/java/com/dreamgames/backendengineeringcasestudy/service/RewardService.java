package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.RewardDao;
import com.dreamgames.backendengineeringcasestudy.exception.RewardAssignmentException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardDeletionException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardRetrievalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardDao rewardDao;

    public void assignRewardToUser(long userId, int reward) {
        if (!rewardDao.assignRewardToUser(userId, reward)) {
            throw new RewardAssignmentException("Failed to assign reward to user");
        }
    }

    public int getUserReward(long userId) {
        return rewardDao.getUserReward(userId).orElseThrow(() -> new RewardRetrievalException("No reward found for user"));
    }

    public boolean hasUserPendingReward(long userId) {
        return rewardDao.getUserReward(userId).isPresent();
    }

    public void removeUserReward(long userId) {
        if (!rewardDao.deleteUserReward(userId)) {
            throw new RewardDeletionException("Failed to remove reward for user");
        }
    }
}

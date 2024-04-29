package com.dreamgames.backendengineeringcasestudy.dao;

import com.dreamgames.backendengineeringcasestudy.exception.RewardAssignmentException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardDeletionException;
import com.dreamgames.backendengineeringcasestudy.exception.RewardRetrievalException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RewardDao {
    private final JdbcTemplate jdbcTemplate;

    public RewardDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean assignRewardToUser(long userId, int reward) {
        try {
            String sql = "INSERT INTO tournament_rewards (user_id, reward) VALUES (?, ?)";
            return jdbcTemplate.update(sql, userId, reward) > 0;
        } catch (DataAccessException e) {
            throw new RewardAssignmentException("Error assigning reward to user", e);
        }
    }

    public Optional<Integer> getUserReward(long userId) {
        try {
            String sql = "SELECT reward FROM tournament_rewards WHERE user_id = ?";
            Integer reward = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            return Optional.ofNullable(reward);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RewardRetrievalException("Error retrieving user reward", e);
        }
    }

    public boolean deleteUserReward(long userId) {
        try {
            String sql = "DELETE FROM tournament_rewards WHERE user_id = ?";
            int rowsAffected = jdbcTemplate.update(sql, userId);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RewardDeletionException("Error deleting user reward", e);
        }
    }
}
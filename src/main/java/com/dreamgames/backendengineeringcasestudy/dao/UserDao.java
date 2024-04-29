package com.dreamgames.backendengineeringcasestudy.dao;

import com.dreamgames.backendengineeringcasestudy.exception.*;
import com.dreamgames.backendengineeringcasestudy.mapper.UserMapper;
import com.dreamgames.backendengineeringcasestudy.model.entity.User;
import com.dreamgames.backendengineeringcasestudy.model.request.CreateUserRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserDao {

    final private JdbcTemplate jdbcTemplate;
    final private SimpleJdbcInsert simpleJdbcInsert;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
    }

    public Optional<User> getUserById(long userId) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            User user = jdbcTemplate.queryForObject(sql, new UserMapper(), userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new UserNotFoundException("User not found", e);
        }
    }

    public int incrementUserStats(long userId, int levelIncrement, int coinsIncrement) {
        try {
            String sql = "UPDATE users SET level = level + ?, coins = coins + ? WHERE user_id = ?";
            return jdbcTemplate.update(sql, levelIncrement, coinsIncrement, userId);
        } catch (DataAccessException e) {
            throw new UserStatsUpdateException("Error updating user stats", e);
        }
    }

    public int updateUserCoins(long userId, int coinsDelta) {
        try {
            String sql = "UPDATE users SET coins = coins + ? WHERE user_id = ?";
            return jdbcTemplate.update(sql, coinsDelta, userId);
        } catch (DataAccessException e) {
            throw new UserCoinsUpdateException("Error updating user coins", e);
        }
    }

    public Long createUser(CreateUserRequest user) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("username", user.getUsername());
            parameters.put("level", user.getLevel());
            parameters.put("coins", user.getCoins());
            parameters.put("country", user.getCountry().name());

            return simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        } catch (DataAccessException e) {
            throw new UserCreationException("Error creating user", e);
        }
    }

    public void depositCoinsToUsers(List<Long> userIds, int coinsDelta) {
        try {
            String sql = "UPDATE users SET coins = coins + ? WHERE user_id = ?";
            jdbcTemplate.batchUpdate(sql, userIds, userIds.size(), (ps, userId) -> {
                ps.setInt(1, coinsDelta);
                ps.setLong(2, userId);
            });
        } catch (DataAccessException e) {
            throw new UserCoinsBatchUpdateException("Error depositing coins to users", e);
        }
    }
}
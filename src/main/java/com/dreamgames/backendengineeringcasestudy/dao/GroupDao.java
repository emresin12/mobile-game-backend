package com.dreamgames.backendengineeringcasestudy.dao;

import com.dreamgames.backendengineeringcasestudy.exception.GroupCreationException;
import com.dreamgames.backendengineeringcasestudy.exception.GroupMembershipException;
import com.dreamgames.backendengineeringcasestudy.exception.GroupNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
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
public class GroupDao {
    final private JdbcTemplate jdbcTemplate;
    final private SimpleJdbcInsert simpleGroupInsert;
    final private SimpleJdbcInsert simpleGroupMemberInsert;

    public GroupDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleGroupInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("tournament_groups").usingGeneratedKeyColumns("group_id");
        this.simpleGroupMemberInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("group_members");
    }

    public long createGroup(long tournamentId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("tournament_id", tournamentId);
            params.put("is_active", false);

            return simpleGroupInsert.executeAndReturnKey(params).longValue();

        } catch (DataAccessException e) {
            throw new GroupCreationException("Error creating tournament group", e);
        }
    }

    public void addUserToGroup(long groupId, long userId, Country country) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("group_id", groupId);
            params.put("user_id", userId);
            params.put("country", country.name());

            simpleGroupMemberInsert.execute(params);

        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error adding user to group", e);
        }
    }

    public Long findAvailableGroup(Country country) {
        try {
            String sql = """
                    SELECT g.group_id
                    FROM group_members g
                             LEFT JOIN group_members gm ON g.group_id = gm.group_id AND gm.country = ?
                    WHERE gm.group_id IS NULL limit 1;""";

            return jdbcTemplate.queryForObject(sql, Long.class, country.name());

        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new GroupNotFoundException("Error finding available group", e);
        }
    }

    public Optional<Boolean> isGroupActive(long groupId) {
        try {
            String sql = "select is_active from tournament_groups where group_id = ?";
            Boolean isActive = jdbcTemplate.queryForObject(sql, Boolean.class, groupId);

            return Optional.ofNullable(isActive);

        } catch (DataAccessException e) {
            throw new GroupNotFoundException("Error finding group", e);
        }
    }

    public void setGroupStatus(long groupId, boolean isActive) {
        try {
            String sql = "update tournament_groups set is_active = ? where group_id = ?";

            jdbcTemplate.update(sql, isActive, groupId);

        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error setting group status", e);
        }
    }

    public void deleteAllGroupMemberships() {
        try {
            String sql = "delete from group_members";

            jdbcTemplate.update(sql);

        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error deleting group memberships", e);
        }
    }

    public void deleteAllGroups() {
        try {
            String sql = "delete from tournament_groups";

            jdbcTemplate.update(sql);

        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error deleting groups", e);
        }
    }

    public List<Long> getInactiveGroupUserIds() {
        try {
            String sql = "select user_id from group_members gm join tournament_groups tg on gm.group_id = tg.group_id where tg.is_active = false";

            return jdbcTemplate.queryForList(sql, Long.class);

        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error getting inactive group user ids", e);
        }
    }

    public Optional<Long> getUsersGroupId(long userId) {
        try {
            String sql = "SELECT group_id FROM group_members WHERE user_id = ?";
            Long userGroupId = jdbcTemplate.queryForObject(sql, Long.class, userId);
            return Optional.ofNullable(userGroupId);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new GroupMembershipException("Error getting user's group id", e);
        }
    }

}

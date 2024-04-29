package com.dreamgames.backendengineeringcasestudy.dao;

import com.dreamgames.backendengineeringcasestudy.exception.TournamentCreationException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentEndException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentInfoRetrievalException;
import com.dreamgames.backendengineeringcasestudy.exception.TournamentRewardInsertionException;
import com.dreamgames.backendengineeringcasestudy.model.dto.UserTournamentRewardDto;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TournamentDao {
    final private JdbcTemplate jdbcTemplate;
    final private SimpleJdbcInsert simpleTournamentsInsert;

    public TournamentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleTournamentsInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("tournaments")
                .usingGeneratedKeyColumns("tournament_id");
    }

    public long createTournament() {
        try {
            Map<String, Object> params = new HashMap<>();
            LocalDate today = LocalDate.now();

            params.put("is_active", true);
            params.put("date", today);
            return simpleTournamentsInsert.executeAndReturnKey(params).longValue();

        } catch (DataAccessException e) {
            throw new TournamentCreationException("Error creating tournament", e);
        }
    }

    public void endTournament(long tournamentId) {
        try {
            String sql = "UPDATE tournaments SET is_active = false WHERE tournament_id = ?";
            jdbcTemplate.update(sql, tournamentId);
        } catch (DataAccessException e) {
            throw new TournamentEndException("Error ending tournament", e);
        }
    }

    public Optional<TournamentInfo> getActiveTournamentInfo() {
        try {
            String sql = "SELECT * FROM tournaments WHERE is_active = true";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                TournamentInfo tournamentInfo = new TournamentInfo();
                tournamentInfo.setTournamentId(rs.getString("tournament_id"));
                tournamentInfo.setIsActive(rs.getString("is_active"));
                return Optional.of(tournamentInfo);
            });
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new TournamentInfoRetrievalException("Error getting active tournament info", e);
        }
    }

    public Optional<TournamentInfo> getTournamentInfoById() {
        String sql = "SELECT * FROM tournaments WHERE tournament_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                TournamentInfo tournamentInfo = new TournamentInfo();
                tournamentInfo.setTournamentId(rs.getString("tournament_id"));
                tournamentInfo.setIsActive(rs.getString("is_active"));
                return Optional.of(tournamentInfo);
            });
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public void batchInsertUserRewards(List<UserTournamentRewardDto> userTournamentRewards) {
        try {
            jdbcTemplate.batchUpdate("INSERT INTO tournament_rewards (user_id, reward) VALUES (?,?)",
                    userTournamentRewards, userTournamentRewards.size(), (ps, argument) -> {
                        ps.setLong(1, argument.getUserId());
                        ps.setInt(2, argument.getReward());
                    });
        } catch (DataAccessException e) {
            throw new TournamentRewardInsertionException("Error adding tournament reward to user", e);
        }
    }
}
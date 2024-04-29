package com.dreamgames.backendengineeringcasestudy.service.cache;

import com.dreamgames.backendengineeringcasestudy.model.entity.CountryLeaderboard;
import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentCacheServiceTest {
    @InjectMocks
    TournamentCacheService tournamentCacheService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void setTournamentInfo_ValidTournamentInfo_SetsTournamentInfoInRedis() {
        TournamentInfo tournamentInfo = new TournamentInfo();
        tournamentInfo.setTournamentId("1");
        tournamentInfo.setIsActive("true");

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        tournamentCacheService.setTournamentInfo(tournamentInfo);

        verify(redisTemplate.opsForHash()).putAll(eq(REDIS_TOURNAMENT_INFO_KEY), anyMap());
    }

    @Test
    void getTournamentInfo_TournamentInfoExists_ReturnsTournamentInfo() {
        TournamentInfo expectedTournamentInfo = new TournamentInfo();
        expectedTournamentInfo.setTournamentId("1");
        expectedTournamentInfo.setIsActive("true");

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        when(redisTemplate.opsForHash().entries(REDIS_TOURNAMENT_INFO_KEY)).thenReturn(Map.of(
                REDIS_TOURNAMENT_ID_KEY, "1",
                REDIS_TOURNAMENT_IS_ACTIVE_KEY, "true"
        ));

        TournamentInfo result = tournamentCacheService.getTournamentInfo().orElseThrow(() ->
                new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "No active tournament found"));


        assertEquals(expectedTournamentInfo.getTournamentId(), result.getTournamentId());
        assertEquals(expectedTournamentInfo.getIsActive(), result.getIsActive());
        verify(redisTemplate.opsForHash()).entries(REDIS_TOURNAMENT_INFO_KEY);
    }

    @Test
    void addUserToGroupLeaderboard_ValidGroupIdAndUserId_AddsUserToGroupLeaderboard() {
        long groupId = 1L;
        String userId = "1";

        when(redisTemplate.opsForZSet()).thenReturn(mock(ZSetOperations.class));
        tournamentCacheService.addUserToGroupLeaderboard(groupId, userId);

        verify(redisTemplate.opsForZSet()).add(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + "1", "1", 0);
    }

    @Test
    void getGroupLeaderboard_LeaderboardNotFound_ThrowsResponseStatusException() {
        long groupId = 1L;
        long start = 0;
        long end = 5;

        when(redisTemplate.opsForZSet()).thenReturn(mock(ZSetOperations.class));
        when(redisTemplate.opsForZSet().reverseRangeWithScores(eq(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + "1"), eq(start), eq(end)))
                .thenReturn(new LinkedHashSet<>());

        assertThrows(ResponseStatusException.class, () -> tournamentCacheService.getGroupLeaderboard(groupId, start, end));
        verify(redisTemplate.opsForZSet()).reverseRangeWithScores(REDIS_GROUP_LEADERBOARD_KEY_PREFIX + "1", 0, 5);
        verify(redisTemplate, never()).executePipelined((SessionCallback<?>) any());
    }

    @Test
    void getCountriesLeaderboard_ValidRange_ReturnsCountryLeaderboard() {
        int start = 0;
        int end = 4;

        Set<ZSetOperations.TypedTuple<String>> expectedSet = new LinkedHashSet<>();
        expectedSet.add(ZSetOperations.TypedTuple.of("TURKEY", 1000.0));
        expectedSet.add(ZSetOperations.TypedTuple.of("USA", 900.0));
        expectedSet.add(ZSetOperations.TypedTuple.of("UK", 800.0));
        expectedSet.add(ZSetOperations.TypedTuple.of("FRANCE", 700.0));
        expectedSet.add(ZSetOperations.TypedTuple.of("GERMANY", 600.0));


        when(redisTemplate.opsForZSet()).thenReturn(mock(ZSetOperations.class));
        when(redisTemplate.opsForZSet().reverseRangeWithScores(eq(REDIS_COUNTRY_LEADERBOARD_KEY), eq((long) start), eq((long) end)))
                .thenReturn(expectedSet);

        CountryLeaderboard result = tournamentCacheService.getCountriesLeaderboard(start, end);

        assertNotNull(result);
        assertEquals(5, result.getCountryScores().size());
        assertEquals("TURKEY", result.getCountryScores().get(0).getCountry().name());
        assertEquals(1000, result.getCountryScores().get(0).getScore());
        assertEquals("USA", result.getCountryScores().get(1).getCountry().name());
        assertEquals(900, result.getCountryScores().get(1).getScore());
        assertEquals("UK", result.getCountryScores().get(2).getCountry().name());
        assertEquals(800, result.getCountryScores().get(2).getScore());
        assertEquals("FRANCE", result.getCountryScores().get(3).getCountry().name());
        assertEquals(700, result.getCountryScores().get(3).getScore());
        assertEquals("GERMANY", result.getCountryScores().get(4).getCountry().name());
        assertEquals(600, result.getCountryScores().get(4).getScore());
        verify(redisTemplate.opsForZSet()).reverseRangeWithScores(REDIS_COUNTRY_LEADERBOARD_KEY, 0L, 4L);
    }

    @Test
    void getCountriesLeaderboard_LeaderboardNotFound_ThrowsResponseStatusException() {
        long start = 0L;
        long end = 4;

        when(redisTemplate.opsForZSet()).thenReturn(mock(ZSetOperations.class));
        when(redisTemplate.opsForZSet().reverseRangeWithScores(eq(REDIS_COUNTRY_LEADERBOARD_KEY), eq(start), eq(end)))
                .thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> tournamentCacheService.getCountriesLeaderboard(start, end));
        verify(redisTemplate.opsForZSet()).reverseRangeWithScores(REDIS_COUNTRY_LEADERBOARD_KEY, start, end);
    }


}

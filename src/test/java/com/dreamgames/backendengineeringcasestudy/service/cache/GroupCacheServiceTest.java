package com.dreamgames.backendengineeringcasestudy.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Optional;

import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupCacheServiceTest {

    private GroupCacheService groupCacheService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;


    @BeforeEach
    void setUp() {
        groupCacheService = new GroupCacheService(redisTemplate);

    }

    @Test
    void createEmptyGroup_shouldCreateGroupWithInitialValues() {
        long groupId = 1L;

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        groupCacheService.createEmptyGroup(groupId);

        verify(redisTemplate.opsForHash()).putAll(eq(REDIS_GROUP_KEY_PREFIX + groupId), anyMap());
    }

    @Test
    void incrementGroupCount_shouldIncrementCount() {
        long groupId = 1L;
        long expectedCount = 2L;

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        when(redisTemplate.opsForHash().increment(REDIS_GROUP_KEY_PREFIX + groupId, REDIS_GROUP_COUNT_KEY, 1)).thenReturn(expectedCount);

        long actualCount = groupCacheService.incrementGroupCount(groupId);

        assertEquals(expectedCount, actualCount);
    }

    @Test
    void setGroupStatus_shouldSetStatus() {
        long groupId = 1L;
        boolean isActive = true;

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        groupCacheService.setGroupStatus(groupId, isActive);

        verify(redisTemplate.opsForHash()).put(REDIS_GROUP_KEY_PREFIX + groupId, REDIS_GROUP_IS_ACTIVE_KEY, String.valueOf(isActive));
    }

    @Test
    void addGroupToActiveGroups_shouldAddGroupToSet() {
        long groupId = 1L;

        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));
        groupCacheService.addGroupToActiveGroups(groupId);

        verify(redisTemplate.opsForSet()).add(REDIS_ACTIVE_GROUPS_KEY, String.valueOf(groupId));
    }

    @Test
    void isGroupActive_shouldReturnTrue_whenGroupIsActive() {
        long groupId = 1L;

        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));
        when(redisTemplate.opsForSet().isMember(REDIS_ACTIVE_GROUPS_KEY, String.valueOf(groupId))).thenReturn(true);

        Optional<Boolean> result = groupCacheService.isGroupActive(groupId);

        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    void isGroupActive_shouldReturnFalse_whenGroupIsNotActive() {
        long groupId = 1L;
        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));
        when(redisTemplate.opsForSet().isMember(REDIS_ACTIVE_GROUPS_KEY, String.valueOf(groupId))).thenReturn(false);

        Optional<Boolean> result = groupCacheService.isGroupActive(groupId);

        assertTrue(result.isPresent());
        assertFalse(result.get());
    }
}
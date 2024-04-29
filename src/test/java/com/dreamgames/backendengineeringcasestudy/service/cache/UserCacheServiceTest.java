package com.dreamgames.backendengineeringcasestudy.service.cache;

import com.dreamgames.backendengineeringcasestudy.model.entity.StaticUserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.REDIS_USER_COUNTRY_KEY;
import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.REDIS_USER_INFO_KEY_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCacheServiceTest {
    @InjectMocks
    UserCacheService userCacheService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void setUserInfo_ValidUserInfo_SetsUserInfoInRedis() {
        StaticUserData staticUserData = new StaticUserData();
        staticUserData.setUserId("1");
        staticUserData.setUsername("testUser");
        staticUserData.setCountry("TURKEY");

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        userCacheService.setUserInfo(staticUserData);

        verify(redisTemplate.opsForHash()).putAll(eq(REDIS_USER_INFO_KEY_PREFIX + "1"), anyMap());
    }

    @Test
    void getUserCountry_ValidUserId_ReturnsUserCountry() {
        String expectedCountry = "TURKEY";

        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        when(redisTemplate.opsForHash().get(REDIS_USER_INFO_KEY_PREFIX + "1", REDIS_USER_COUNTRY_KEY)).thenReturn(expectedCountry);

        String result = userCacheService.getUserCountry(1L);

        assertEquals(expectedCountry, result);
        verify(redisTemplate.opsForHash()).get(REDIS_USER_INFO_KEY_PREFIX + "1", REDIS_USER_COUNTRY_KEY);
    }

    @Test
    void getUsernamesFromIds_ValidUserIds_ReturnsUsernamesMap() {
        List<String> userIds = List.of("1", "2", "3");
        List<Object> usernames = List.of("user1", "user2", "user3");


        when(redisTemplate.executePipelined((RedisCallback<?>) any())).thenReturn(usernames);

        Map<String, String> result = userCacheService.getUsernamesFromIds(userIds);

        assertEquals(3, result.size());
        assertEquals("user1", result.get("1"));
        assertEquals("user2", result.get("2"));
        assertEquals("user3", result.get("3"));
        verify(redisTemplate).executePipelined((RedisCallback<?>) any());
    }

}

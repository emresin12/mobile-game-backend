package com.dreamgames.backendengineeringcasestudy.service.cache;

import com.dreamgames.backendengineeringcasestudy.model.entity.StaticUserData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.*;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisTemplate<String, String> redisTemplate;


    public void setUserInfo(StaticUserData staticUserData) {
        Map<String, String> map = new HashMap<>();
        map.put(REDIS_USER_USERNAME_KEY, staticUserData.getUsername());
        map.put(REDIS_USER_COUNTRY_KEY, staticUserData.getCountry());
        redisTemplate.opsForHash().putAll(REDIS_USER_INFO_KEY_PREFIX + staticUserData.getUserId(), map);
    }

    public String getUserCountry(Long userId) {
        return (String) redisTemplate.opsForHash()
                .get(REDIS_USER_INFO_KEY_PREFIX + userId.toString(), REDIS_USER_COUNTRY_KEY);
    }

    public Map<String, String> getUsernamesFromIds(List<String> userIds) {
        //execute in pipeline
        List<Object> results = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (String id : userIds) {
                connection.hashCommands().hGet((REDIS_USER_INFO_KEY_PREFIX + id).getBytes(), REDIS_USER_USERNAME_KEY.getBytes());
            }
            return null;
        });

        HashMap<String, String> usernameMap = new HashMap<>();

        for (int i = 0; i < userIds.size(); i++) {
            usernameMap.put(userIds.get(i), (String) results.get(i));
        }


        return usernameMap;
    }
}

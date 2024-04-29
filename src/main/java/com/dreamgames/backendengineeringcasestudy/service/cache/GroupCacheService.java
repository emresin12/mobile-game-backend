package com.dreamgames.backendengineeringcasestudy.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

import static com.dreamgames.backendengineeringcasestudy.constants.RedisConstants.*;

@Service
@RequiredArgsConstructor
public class GroupCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    public void createEmptyGroup(long groupId) {

        HashMap<String, String> map = new HashMap<>();
        map.put(REDIS_GROUP_IS_ACTIVE_KEY, "false");
        map.put(REDIS_GROUP_COUNT_KEY, "0");
        redisTemplate.opsForHash().putAll(REDIS_GROUP_KEY_PREFIX + groupId, map);
    }

    public long incrementGroupCount(long groupId) {

        return redisTemplate.opsForHash().increment(REDIS_GROUP_KEY_PREFIX + groupId, REDIS_GROUP_COUNT_KEY, 1);
    }

    public void setGroupStatus(long groupId, boolean isActive) {
        // set the status
        redisTemplate.opsForHash().put(REDIS_GROUP_KEY_PREFIX + groupId, REDIS_GROUP_IS_ACTIVE_KEY, String.valueOf(isActive));
    }

    public void addGroupToActiveGroups(long groupId) {
        redisTemplate.opsForSet().add(REDIS_ACTIVE_GROUPS_KEY, String.valueOf(groupId));
    }

    public Optional<Boolean> isGroupActive(long groupId) {
        return Optional.ofNullable(redisTemplate.opsForSet().isMember(REDIS_ACTIVE_GROUPS_KEY, String.valueOf(groupId)));
    }

}

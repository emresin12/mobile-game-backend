package com.dreamgames.backendengineeringcasestudy.constants;

public class RedisConstants {
    //Tournament Cache Keys
    public static final String REDIS_TOURNAMENT_INFO_KEY = "tournament";
    public static final String REDIS_TOURNAMENT_ID_KEY = "tournament_id";
    public static final String REDIS_TOURNAMENT_IS_ACTIVE_KEY = "is_active";
    public static final String REDIS_GROUP_LEADERBOARD_KEY_PREFIX = "group_leaderboard:";
    public static final String REDIS_COUNTRY_LEADERBOARD_KEY = "country_leaderboard";
    public static final String REDIS_ALL_GROUP_LEADERBOARDS_PATTERN = "group_leaderboard:*";

    //Group Cache Keys
    public static final String REDIS_ALL_GROUPS_PATTERN = "group:*";
    public static final String REDIS_GROUP_KEY_PREFIX = "group:";
    public static final String REDIS_GROUP_COUNT_KEY = "count";
    public static final String REDIS_GROUP_IS_ACTIVE_KEY = "is_active";
    public static final String REDIS_ACTIVE_GROUPS_KEY = "active_groups";

    //User Cache Keys
    public static final String REDIS_USER_INFO_KEY_PREFIX = "user:";
    public static final String REDIS_USER_COUNTRY_KEY = "country";
    public static final String REDIS_USER_USERNAME_KEY = "username";
}


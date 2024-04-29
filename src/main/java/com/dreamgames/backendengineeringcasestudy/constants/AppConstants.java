package com.dreamgames.backendengineeringcasestudy.constants;

public class AppConstants {
    public static final int STARTING_COINS = 5000;
    public static final int STARTING_LEVEL = 1;
    public static final int COIN_REWARD_PER_LEVEL = 25;
    public static final int TOURNAMENT_ENTRANCE_FEE = 1000;
    public static final int NUMBER_OF_COUNTRIES = 5;
    public static final int NUMBER_OF_GROUPS = 5;
    public static final int TOURNAMENT_REQUIRED_LEVEL = 20;
    public static final int TOURNAMENT_REWARD_FIRST_PLACE = 10000;
    public static final int TOURNAMENT_REWARD_SECOND_PLACE = 5000;

    // Batching and threading constants
    public static final int ADD_TOURNAMENT_REWARD_BATCH_SIZE = 1000;
    public static final int ADD_TOURNAMENT_REWARD_THREAD_POOL_SIZE = 5;
}

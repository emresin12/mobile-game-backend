package com.dreamgames.backendengineeringcasestudy.exception;

public class UserStatsUpdateException extends RuntimeException {
    public UserStatsUpdateException(String message) {
        super(message);
    }

    public UserStatsUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

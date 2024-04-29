package com.dreamgames.backendengineeringcasestudy.exception;

public class RewardDeletionException extends RuntimeException {
    public RewardDeletionException(String message) {
        super(message);
    }

    public RewardDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

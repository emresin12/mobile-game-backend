package com.dreamgames.backendengineeringcasestudy.exception;

public class RewardRetrievalException extends RuntimeException {
    public RewardRetrievalException(String message) {
        super(message);
    }

    public RewardRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

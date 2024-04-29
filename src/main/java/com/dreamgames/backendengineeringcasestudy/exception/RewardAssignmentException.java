package com.dreamgames.backendengineeringcasestudy.exception;

public class RewardAssignmentException extends RuntimeException {
    public RewardAssignmentException(String message) {
        super(message);
    }

    public RewardAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}

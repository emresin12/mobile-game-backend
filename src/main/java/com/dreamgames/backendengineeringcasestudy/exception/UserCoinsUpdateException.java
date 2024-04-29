package com.dreamgames.backendengineeringcasestudy.exception;

public class UserCoinsUpdateException extends RuntimeException {
    public UserCoinsUpdateException(String message) {
        super(message);
    }

    public UserCoinsUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

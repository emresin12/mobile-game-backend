package com.dreamgames.backendengineeringcasestudy.exception;

public class UserCoinsBatchUpdateException extends RuntimeException {
    public UserCoinsBatchUpdateException(String message) {
        super(message);
    }

    public UserCoinsBatchUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

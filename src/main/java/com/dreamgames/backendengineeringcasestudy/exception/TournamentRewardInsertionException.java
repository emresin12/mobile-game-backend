package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentRewardInsertionException extends RuntimeException {
    public TournamentRewardInsertionException(String message) {
        super(message);
    }

    public TournamentRewardInsertionException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentEligibilityException extends RuntimeException {
    public TournamentEligibilityException(String message) {
        super(message);
    }

    public TournamentEligibilityException(String message, Throwable cause) {
        super(message, cause);
    }
}

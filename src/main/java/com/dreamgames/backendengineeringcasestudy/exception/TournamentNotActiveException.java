package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentNotActiveException extends RuntimeException {
    public TournamentNotActiveException(String message) {
        super(message);
    }

    public TournamentNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}

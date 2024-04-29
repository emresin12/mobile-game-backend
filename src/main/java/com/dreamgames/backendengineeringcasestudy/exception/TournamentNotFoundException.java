package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(String message) {
        super(message);
    }

    public TournamentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentCreationException extends RuntimeException {
    public TournamentCreationException(String message) {
        super(message);
    }

    public TournamentCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

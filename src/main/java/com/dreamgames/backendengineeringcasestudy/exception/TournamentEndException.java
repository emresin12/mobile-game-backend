package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentEndException extends RuntimeException {
    public TournamentEndException(String message) {
        super(message);
    }

    public TournamentEndException(String message, Throwable cause) {
        super(message, cause);
    }
}

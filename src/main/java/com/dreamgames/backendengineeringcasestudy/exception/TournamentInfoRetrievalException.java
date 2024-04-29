package com.dreamgames.backendengineeringcasestudy.exception;

public class TournamentInfoRetrievalException extends RuntimeException {
    public TournamentInfoRetrievalException(String message) {
        super(message);
    }

    public TournamentInfoRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.dreamgames.backendengineeringcasestudy.exception;

public class GroupMembershipException extends RuntimeException {
    public GroupMembershipException(String message) {
        super(message);
    }

    public GroupMembershipException(String message, Throwable cause) {
        super(message, cause);
    }
}

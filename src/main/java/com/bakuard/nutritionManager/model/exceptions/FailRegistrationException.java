package com.bakuard.nutritionManager.model.exceptions;

public class FailRegistrationException extends AuthException {

    public FailRegistrationException() {
    }

    public FailRegistrationException(String message) {
        super(message);
    }

    public FailRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailRegistrationException(Throwable cause) {
        super(cause);
    }

}

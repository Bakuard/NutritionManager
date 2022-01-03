package com.bakuard.nutritionManager.model.exceptions;

public class FailChangeCredentialsException extends AuthException {

    public FailChangeCredentialsException() {
    }

    public FailChangeCredentialsException(String message) {
        super(message);
    }

    public FailChangeCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailChangeCredentialsException(Throwable cause) {
        super(cause);
    }

}

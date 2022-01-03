package com.bakuard.nutritionManager.model.exceptions;

public class FailEnterException extends AuthException {

    public FailEnterException() {

    }

    public FailEnterException(String message) {
        super(message);
    }

    public FailEnterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailEnterException(Throwable cause) {
        super(cause);
    }

}

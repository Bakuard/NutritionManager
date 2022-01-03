package com.bakuard.nutritionManager.model.exceptions;

public class IncorrectJwsException extends AbstractDomainException {

    public IncorrectJwsException() {
    }

    public IncorrectJwsException(String message) {
        super(message);
    }

    public IncorrectJwsException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectJwsException(Throwable cause) {
        super(cause);
    }

}

package com.bakuard.nutritionManager.model.exceptions;

public class TagValidateException extends ValidateException {

    public TagValidateException() {
    }

    public TagValidateException(String message) {
        super(message);
    }

    public TagValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TagValidateException(Throwable cause) {
        super(cause);
    }

}

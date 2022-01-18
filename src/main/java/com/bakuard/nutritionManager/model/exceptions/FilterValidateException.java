package com.bakuard.nutritionManager.model.exceptions;

public class FilterValidateException extends ValidateException {

    public FilterValidateException() {
    }

    public FilterValidateException(String message) {
        super(message);
    }

    public FilterValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterValidateException(Throwable cause) {
        super(cause);
    }

}

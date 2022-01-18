package com.bakuard.nutritionManager.model.exceptions;

public class MenuValidateException extends ValidateException {

    public MenuValidateException() {
    }

    public MenuValidateException(String message) {
        super(message);
    }

    public MenuValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public MenuValidateException(Throwable cause) {
        super(cause);
    }

}

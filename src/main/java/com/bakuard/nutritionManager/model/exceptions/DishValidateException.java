package com.bakuard.nutritionManager.model.exceptions;

public class DishValidateException extends ValidateException {

    public DishValidateException() {
    }

    public DishValidateException(String message) {
        super(message);
    }

    public DishValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DishValidateException(Throwable cause) {
        super(cause);
    }

}

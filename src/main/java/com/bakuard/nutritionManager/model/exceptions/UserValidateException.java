package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что при создании или изменении объекта пользователя был нарушен как минимум один инвариант
 */
public class UserValidateException extends ValidateException {

    public UserValidateException() {
    }

    public UserValidateException(String message) {
        super(message);
    }

    public UserValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserValidateException(Throwable cause) {
        super(cause);
    }

}

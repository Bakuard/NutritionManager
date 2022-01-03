package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что пользователь {@link com.bakuard.nutritionManager.model.User} с такими данными уже существует
 * в БД.
 */
public class UserAlreadyExistsException extends UnknownSourceException {

    public UserAlreadyExistsException() {

    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }

}

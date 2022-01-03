package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что репозиторию не удалось найти определенного пользователя по некоторому критерию.
 */
public class UnknownUserException extends UnknownSourceException {

    public UnknownUserException() {
    }

    public UnknownUserException(String message) {
        super(message);
    }

    public UnknownUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownUserException(Throwable cause) {
        super(cause);
    }

}

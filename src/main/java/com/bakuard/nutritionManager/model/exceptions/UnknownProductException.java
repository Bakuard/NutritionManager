package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что репозиторию не удалось найти определенный продукт по некоторому критерию.
 */
public class UnknownProductException extends UnknownSourceException {

    public UnknownProductException() {
    }

    public UnknownProductException(String message) {
        super(message);
    }

    public UnknownProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownProductException(Throwable cause) {
        super(cause);
    }

}

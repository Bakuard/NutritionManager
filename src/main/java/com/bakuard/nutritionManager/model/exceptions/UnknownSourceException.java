package com.bakuard.nutritionManager.model.exceptions;

/**
 * Супертип для всех исключений указывающих что некоторый ресурс не удалось найти.
 */
public abstract class UnknownSourceException extends AbstractDomainException {

    public UnknownSourceException() {
    }

    public UnknownSourceException(String message) {
        super(message);
    }

    public UnknownSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownSourceException(Throwable cause) {
        super(cause);
    }

}

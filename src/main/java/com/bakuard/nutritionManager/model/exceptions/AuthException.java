package com.bakuard.nutritionManager.model.exceptions;

/**
 * Супертип для всех ошибок связанных с аутентификацией и авторизацией.
 */
public abstract class AuthException extends AbstractDomainException {

    public AuthException() {
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

}

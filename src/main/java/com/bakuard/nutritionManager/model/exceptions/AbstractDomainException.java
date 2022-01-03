package com.bakuard.nutritionManager.model.exceptions;

/**
 * Общий супертип для всех исключений связанных с бизнес логикой.
 */
public abstract class AbstractDomainException extends RuntimeException {

    public AbstractDomainException() {
    }

    public AbstractDomainException(String message) {
        super(message);
    }

    public AbstractDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbstractDomainException(Throwable cause) {
        super(cause);
    }

    /**
     * Возвращаемое значение используется для идентификации объекта исключения при формировании очета об ошибке
     * пользователю.
     * @return контекстные данные исключения.
     */
    public String getMessageKey() {
        return getClass().getSimpleName();
    }

}

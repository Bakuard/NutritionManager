package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что для создания метаданных страницы пытаются использовать отрицательное число в качестве общего кол-ва
 * элементов выборки.
 */
public class NegativePageTotalItemsException extends RuntimeException {

    public NegativePageTotalItemsException() {
    }

    public NegativePageTotalItemsException(String message) {
        super(message);
    }

    public NegativePageTotalItemsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NegativePageTotalItemsException(Throwable cause) {
        super(cause);
    }

}

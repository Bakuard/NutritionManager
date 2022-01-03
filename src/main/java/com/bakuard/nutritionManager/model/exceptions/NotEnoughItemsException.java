package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что коллекция объектов переданная некоторому методу содержит недостаточное кол-во элементов.
 */
public class NotEnoughItemsException extends IncorrectFiledValueException {

    public NotEnoughItemsException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public NotEnoughItemsException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public NotEnoughItemsException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public NotEnoughItemsException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public NotEnoughItemsException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public NotEnoughItemsException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public NotEnoughItemsException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public NotEnoughItemsException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что для некоторого поля/параметра попытались задать числовое значение меньшее или равное нулю.<br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class NotPositiveValueException extends IncorrectFiledValueException {

    public NotPositiveValueException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public NotPositiveValueException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public NotPositiveValueException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public NotPositiveValueException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public NotPositiveValueException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public NotPositiveValueException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public NotPositiveValueException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public NotPositiveValueException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

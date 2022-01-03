package com.bakuard.nutritionManager.model.exceptions;

public class IncorrectStringLengthException extends IncorrectFiledValueException {

    public IncorrectStringLengthException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public IncorrectStringLengthException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public IncorrectStringLengthException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public IncorrectStringLengthException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public IncorrectStringLengthException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public IncorrectStringLengthException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public IncorrectStringLengthException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public IncorrectStringLengthException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

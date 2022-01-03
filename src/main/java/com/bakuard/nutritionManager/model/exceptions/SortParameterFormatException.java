package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что переданные параметры сортировки в виде строки не распознаются системой.
 */
public class SortParameterFormatException extends IncorrectFiledValueException {

    public SortParameterFormatException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public SortParameterFormatException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public SortParameterFormatException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public SortParameterFormatException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public SortParameterFormatException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public SortParameterFormatException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public SortParameterFormatException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public SortParameterFormatException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

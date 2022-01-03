package com.bakuard.nutritionManager.model.exceptions;

/**
 * Выбрасывается, если некоторому полю объекта или параметру метода пытаются назначить отрицательное числовое
 * значение и такое значение для данного поля/параметра является недопустимым.<br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class NegativeValueException extends IncorrectFiledValueException {

    public NegativeValueException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public NegativeValueException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public NegativeValueException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public NegativeValueException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public NegativeValueException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public NegativeValueException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public NegativeValueException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public NegativeValueException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

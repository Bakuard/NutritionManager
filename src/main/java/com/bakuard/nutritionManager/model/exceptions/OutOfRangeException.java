package com.bakuard.nutritionManager.model.exceptions;

/**
 * Выбрасывается, если некоторому полю объекта или параметру метода пытаются назначить чиловое значение выходящее
 * за некоторый допустимый диапозон.<br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class OutOfRangeException extends IncorrectFiledValueException {

    public OutOfRangeException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public OutOfRangeException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public OutOfRangeException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public OutOfRangeException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public OutOfRangeException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public OutOfRangeException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public OutOfRangeException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public OutOfRangeException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

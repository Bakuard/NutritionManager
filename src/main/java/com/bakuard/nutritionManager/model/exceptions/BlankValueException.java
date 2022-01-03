package com.bakuard.nutritionManager.model.exceptions;

/**
 * Выбрасывается, если некоторому полю объекта или параметру метода пытаются назначить строку не содержащую
 * ни одного отображаемого символа и такая строка для данного поля/параметра является недопустимой.<br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class BlankValueException extends IncorrectFiledValueException {

    public BlankValueException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public BlankValueException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public BlankValueException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public BlankValueException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public BlankValueException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public BlankValueException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public BlankValueException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public BlankValueException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

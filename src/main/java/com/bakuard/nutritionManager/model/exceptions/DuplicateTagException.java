package com.bakuard.nutritionManager.model.exceptions;

/**
 * Выбрасывается в случае, когда один и тот же тег пытаются задать для контекстных данных о
 * продукте или данных блюда - более одного раза.<br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class DuplicateTagException extends IncorrectFiledValueException {

    public DuplicateTagException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public DuplicateTagException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public DuplicateTagException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public DuplicateTagException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public DuplicateTagException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public DuplicateTagException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public DuplicateTagException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public DuplicateTagException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

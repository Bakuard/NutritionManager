package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что для некоторого поля не было задано никакого значения (т.е. было получено значение null
 * или другое дефолтное значение). <br/>
 * Описание параметров каждого конструктора смотри в {@link IncorrectFiledValueException}.
 */
public class MissingValueException extends IncorrectFiledValueException {

    public static <T>T check(T value, Class<?> type, String fieldName) {
        if(value == null) {
            throw new MissingValueException(type.getSimpleName() + "." + fieldName + " can't be null", type, fieldName);
        }
        return value;
    }


    public MissingValueException(Class<?> type, String fieldName) {
        super(type, fieldName);
    }

    public MissingValueException(String message, Class<?> type, String fieldName) {
        super(message, type, fieldName);
    }

    public MissingValueException(String message, Throwable cause, Class<?> type, String fieldName) {
        super(message, cause, type, fieldName);
    }

    public MissingValueException(Throwable cause, Class<?> type, String fieldName) {
        super(cause, type, fieldName);
    }

    public MissingValueException(Class<?> type, String fieldName, Object rejectedValue) {
        super(type, fieldName, rejectedValue);
    }

    public MissingValueException(String message, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, type, fieldName, rejectedValue);
    }

    public MissingValueException(String message, Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(message, cause, type, fieldName, rejectedValue);
    }

    public MissingValueException(Throwable cause, Class<?> type, String fieldName, Object rejectedValue) {
        super(cause, type, fieldName, rejectedValue);
    }

}

package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что при создании продукта был нарушен как минимум один инвариант.
 */
public class ProductValidateException extends ValidateException {

    public ProductValidateException() {
        super();
    }

    public ProductValidateException(String message) {
        super(message);
    }

    public ProductValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductValidateException(Throwable cause) {
        super(cause);
    }

}

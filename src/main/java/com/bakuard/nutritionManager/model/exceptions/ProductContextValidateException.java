package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что при создании {@link com.bakuard.nutritionManager.model.ProductContext} был нарушен как
 * минимум один инвариант.
 */
public class ProductContextValidateException extends ValidateException {

    public ProductContextValidateException() {

    }

    public ProductContextValidateException(String message) {
        super(message);
    }

    public ProductContextValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductContextValidateException(Throwable cause) {
        super(cause);
    }

}

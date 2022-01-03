package com.bakuard.nutritionManager.model.exceptions;

/**
 * Указывает, что продукт с таким же контекстом ({@link com.bakuard.nutritionManager.model.ProductContext})
 * уже существует у данного пользователя.
 */
public class ProductAlreadyExistsException extends AbstractDomainException {

    public ProductAlreadyExistsException() {

    }

    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductAlreadyExistsException(Throwable cause) {
        super(cause);
    }

}

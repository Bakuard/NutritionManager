package com.bakuard.nutritionManager.model.exceptions;

import java.util.List;

/**
 * Может быть выброшенно в момент вызова {@link com.bakuard.nutritionManager.model.util.Page.Info#createPage(List)}.
 * Указывает, что фактическй размер страницы и размер списка её элементов не совпадают.
 */
public class IncorrectPageContentSizeException extends RuntimeException {

    public IncorrectPageContentSizeException() {
    }

    public IncorrectPageContentSizeException(String message) {
        super(message);
    }

    public IncorrectPageContentSizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectPageContentSizeException(Throwable cause) {
        super(cause);
    }

}

package com.bakuard.nutritionManager.model.util;

import com.bakuard.nutritionManager.model.exceptions.ValidateException;

public interface AbstractBuilder<T> {

    public T tryBuild() throws ValidateException;

}

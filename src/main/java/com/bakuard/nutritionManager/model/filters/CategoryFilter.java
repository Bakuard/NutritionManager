package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;

import com.google.common.collect.ImmutableList;

public class CategoryFilter implements Filter {

    /**
     * Создает и возвращает новый объект ограничения CategoryConstraint.
     * @param name категория продуктов используемых в создаваемом ограничении.
     * @return новый объект ограничения CategoryConstraint.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *              1. если name равен null.<br/>
     *              2. если name не содержит ни одноо отображаемого символа.
     */
    public static CategoryFilter of(String name) {
        return new CategoryFilter(name);
    }


    private final String category;

    private CategoryFilter(String category) {
        Checker.of(getClass(), "constructor").
                nullValue("category", category).
                blankValue("category", category).
                checkWithServiceException();

        this.category = category;
    }

    @Override
    public Type getType() {
        return Type.CATEGORY;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public String getCategory() {
        return category;
    }

}

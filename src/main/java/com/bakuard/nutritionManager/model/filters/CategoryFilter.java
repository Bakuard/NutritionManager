package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.FilterValidateException;

import com.google.common.collect.ImmutableList;

public class CategoryFilter implements Filter {

    /**
     * Создает и возвращает новый объект ограничения CategoryConstraint.
     * @param name категория продуктов используемых в создаваемом ограничении.
     * @return новый объект ограничения CategoryConstraint.
     * @throws FilterValidateException если выполняется одно из следующих условий:<br/>
     *              1. если name равен null.<br/>
     *              2. если name не содержит ни одноо отображаемого символа.
     */
    public static CategoryFilter of(String name) {
        return new CategoryFilter(name);
    }


    private final String category;

    private CategoryFilter(String category) {
        tryThrow(
                Constraint.check(getClass(), "category",
                        Constraint.nullValue(category),
                        Constraint.blankValue(category))
        );

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


    private void tryThrow(Constraint constraint) {
        if(constraint != null) {
            FilterValidateException e = new FilterValidateException("Fail to update user.");
            e.addReason(constraint);
            throw e;
        }
    }

}

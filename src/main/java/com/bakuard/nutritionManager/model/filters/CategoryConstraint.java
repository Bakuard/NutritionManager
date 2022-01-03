package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.BlankValueException;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;

import com.google.common.collect.ImmutableList;

public class CategoryConstraint implements Constraint {

    /**
     * Создает и возвращает новый объект ограничения CategoryConstraint.
     * @param name категория продуктов используемых в создаваемом ограничении.
     * @return новый объект ограничения CategoryConstraint.
     * @throws MissingValueException если name равен null.
     * @throws BlankValueException если name не содержит ни одноо отображаемого символа.
     */
    public static CategoryConstraint of(String name) {
        return new CategoryConstraint(name);
    }


    private final String category;

    private CategoryConstraint(String category) {
        MissingValueException.check(category, getClass(), "category");
        if(category.isBlank())
            throw new BlankValueException("CategoryConstraint category can't be blank", getClass(), "category");

        this.category = category;
    }

    @Override
    public Type getType() {
        return Type.CATEGORY;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return ImmutableList.of();
    }

    public String getCategory() {
        return category;
    }

}

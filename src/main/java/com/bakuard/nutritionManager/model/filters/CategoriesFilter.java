package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class CategoriesFilter implements Filter {

    /**
     * Создает и возвращает новый объект ограничения CategoryConstraint.
     * @param name категория продуктов используемых в создаваемом ограничении.
     * @return новый объект ограничения CategoryConstraint.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *              1. если name равен null.<br/>
     *              2. если name не содержит ни одноо отображаемого символа.
     */
    public static CategoriesFilter of(String name) {
        return new CategoriesFilter(name);
    }


    private final ImmutableList<String> categories;

    private CategoriesFilter(List <String> categories) {
        Checker.of(getClass(), "constructor").
                nullValue("categories", categories).
                containsNull("categories", categories).
                notEnoughItems("categories", categories, 1).
                containsBlankValue("categories", categories).
                checkWithServiceException();

        this.categories = ImmutableList.copyOf(categories);
    }

    @Override
    public Type getType() {
        return Type.CATEGORY;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getCategories() {
        return categories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoriesFilter that = (CategoriesFilter) o;
        return Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    @Override
    public String toString() {
        return "CategoryFilter{" +
                "category='" + category + '\'' +
                '}';
    }

}

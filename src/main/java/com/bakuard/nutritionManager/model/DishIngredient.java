package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.filters.Filter;

import java.math.BigDecimal;
import java.util.Objects;

public class DishIngredient {

    private final String name;
    private final Filter filter;
    private final BigDecimal quantity;

    DishIngredient(String name, Filter filter, BigDecimal quantity) {
        this.name = name;
        this.filter = filter;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public Filter getConstraint() {
        return filter;
    }

    public BigDecimal getNecessaryQuantity(BigDecimal servingNumber) {
        return quantity.multiply(servingNumber);
    }

    public int getProductsNumber() {
        return 0;
    }

    public Product getProductBy(int productIndex) {
        return null;
    }

    public BigDecimal getActualQuantity(BigDecimal servingNumber, int productIndex) {
        return null;
    }

    public BigDecimal getPrice(BigDecimal servingNumber, int productIndex) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return name.equals(that.name) &&
                filter.equals(that.filter) &&
                quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filter, quantity);
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "name='" + name + '\'' +
                ", constraint=" + filter +
                ", quantity=" + quantity +
                '}';
    }

}

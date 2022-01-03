package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.filters.Constraint;

import java.math.BigDecimal;
import java.util.Objects;

public class DishIngredient {

    private final String name;
    private final Constraint constraint;
    private final BigDecimal quantity;

    DishIngredient(String name, Constraint constraint, BigDecimal quantity) {
        this.name = name;
        this.constraint = constraint;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public Constraint getConstraint() {
        return constraint;
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
                constraint.equals(that.constraint) &&
                quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, constraint, quantity);
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "name='" + name + '\'' +
                ", constraint=" + constraint +
                ", quantity=" + quantity +
                '}';
    }

}

package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class QuantityFilter extends AbstractFilter {

    public enum Relative {
        LESS,
        GREATER,
        LESS_OR_EQUAL,
        GREATER_OR_EQUAL,
        EQUAL
    }


    private final BigDecimal quantity;
    private final Relative relative;

    QuantityFilter(BigDecimal quantity, Relative relative) {
        Validator.check(
                "Quantity.quantity", notNull(quantity).and(() -> notNegative(quantity)),
                "Quantity.relative", notNull(relative)
        );

        this.quantity = quantity;
        this.relative = relative;
    }

    @Override
    public Type getType() {
        return Type.MIN_QUANTITY;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Relative getRelative() {
        return relative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuantityFilter quantityFilter1 = (QuantityFilter) o;
        return quantity.equals(quantityFilter1.quantity) &&
                relative == quantityFilter1.relative;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, relative);
    }

    @Override
    public String toString() {
        return getType() + "(" + relative + ", " + quantity + ')';
    }

}

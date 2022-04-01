package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Objects;

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
        ValidateException.check(
                Rule.of("Quantity.quantity").notNull(quantity).
                        and(r -> r.notNegative(quantity)),
                Rule.of("Quantity.relative").notNull(relative)
        );

        this.quantity = quantity;
        this.relative = relative;
    }

    @Override
    public Type getType() {
        return Type.MIN_QUANTITY;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
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
        return "Quantity{" +
                "quantity=" + quantity +
                ", relative=" + relative +
                '}';
    }

}

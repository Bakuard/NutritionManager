package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Validator;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class AndFilter implements Filter {

    private final ImmutableList<Filter> operands;

    AndFilter(List<Filter> operands) {
        Validator.create().
                notNull("operands", operands).
                notContainsNull("operands", operands).
                containsAtLeast("operands", operands, 2).
                validate();

        this.operands = ImmutableList.copyOf(operands);
    }

    @Override
    public Type getType() {
        return Type.AND;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return operands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndFilter andFilter = (AndFilter) o;
        return Objects.equals(operands, andFilter.operands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operands);
    }

    @Override
    public String toString() {
        return "AndFilter{" +
                "operands=" + operands +
                '}';
    }

}

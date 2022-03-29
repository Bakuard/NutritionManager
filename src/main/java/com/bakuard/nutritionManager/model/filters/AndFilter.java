package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class AndFilter implements Filter {

    private final ImmutableList<Filter> operands;

    AndFilter(List<Filter> operands) {
        ValidateException.check(
                Rule.of("AndFilter.operands").notNull(operands).
                        and(v -> v.notContainsNull(operands)).
                        and(v -> v.min(operands.size(), 2))
        );

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

package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class OrElseFilter implements Filter {

    private final ImmutableList<Filter> operands;

    OrElseFilter(List<Filter> operands) {
        Checker.of(getClass(), "operands").
                notNull("operands", operands).
                notContainsNull("operands", operands).
                containsAtLeast("operands", operands, 2).
                validate();

        this.operands = ImmutableList.copyOf(operands);
    }

    @Override
    public Type getType() {
        return Type.OR_ELSE;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return operands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrElseFilter orElseFilter = (OrElseFilter) o;
        return operands.equals(orElseFilter.operands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operands);
    }

    @Override
    public String toString() {
        return "Or{" +
                "operands=" + operands +
                '}';
    }

}

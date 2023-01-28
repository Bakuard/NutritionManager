package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class OrElseFilter extends AbstractFilter {

    private final ImmutableList<Filter> operands;

    OrElseFilter(List<Filter> operands) {
        Validator.check(
                "OrElseFilter.operands", notNull(operands).
                        and(() -> notContainsNull(operands)).
                        and(() -> min(operands.size(), 2))
        );

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
        return "OrElseFilter{" +
                "operands=" + operands +
                '}';
    }

}

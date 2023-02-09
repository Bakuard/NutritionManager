package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class OrFilter extends AbstractFilter {

    private final ImmutableList<Filter> operands;

    OrFilter(List<Filter> operands) {
        Validator.check(
                "OrFilter.operands", notNull(operands).
                        and(() -> notContainsNull(operands)).
                        and(() -> min(operands.size(), 2))
        );

        this.operands = ImmutableList.copyOf(operands);
        this.operands.forEach(operand -> ((AbstractFilter)operand).parent = OrFilter.this);
    }

    @Override
    public Type getType() {
        return Type.OR;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return operands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrFilter orFilter = (OrFilter) o;
        return operands.equals(orFilter.operands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operands);
    }

    @Override
    public String toString() {
        return "OrFilter" + operands;
    }

}

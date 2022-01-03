package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.NotEnoughItemsException;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndConstraint implements Constraint {

    /**
     * Создает и возвращает новый объект ограничения AndConstraint.
     * @param constraints список ограничений выступающих как операнды данного ограничения.
     * @return новый объект ограничения AndConstraint.
     * @throws NotEnoughItemsException если кол-во ограничений в спсике constraints меньше двух.
     * @throws MissingValueException если хотябы один из операндов имеет значение null или передаваемый список
     *                               операндов имеет значение null.
     */
    public static AndConstraint of(List<Constraint> constraints) {
        return new AndConstraint(ImmutableList.copyOf(constraints));
    }

    /**
     * Создает и возвращает новый объект ограничения AndConstraint.
     * @param a первый обязательный операнд ограничения AndConstraint.
     * @param b второй обязательный операнд ограничения AndConstraint.
     * @param other другие не обязательные операнды ограничения AndConstraint.
     * @return новый объект ограничения AndConstraint.
     * @throws MissingValueException  если хотябы один из операндов или массив other имеют значение null.
     */
    public static AndConstraint of(Constraint a, Constraint b, Constraint... other) {
        return new AndConstraint(a, b, other);
    }


    private final ImmutableList<Constraint> operands;

    private AndConstraint(Constraint a, Constraint b, Constraint... other) {
        MissingValueException.check(other, getClass(), "operands");
        ArrayList<Constraint> operands = new ArrayList<>(Arrays.asList(other));
        operands.add(a);
        operands.add(b);
        operands.forEach(op -> MissingValueException.check(op, getClass(), "operand"));

        this.operands = ImmutableList.copyOf(operands);
    }

    private AndConstraint(List<Constraint> operands) {
        MissingValueException.check(operands, getClass(), "operands");
        operands.forEach(op -> MissingValueException.check(op, getClass(), "operand"));

        if(operands.size() < 2) {
            throw new NotEnoughItemsException(
                    "AndConstraint takes at least two operands", getClass(), "constraints");
        }
        this.operands = ImmutableList.copyOf(operands);
    }

    @Override
    public Type getType() {
        return Type.AND;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return operands;
    }

}

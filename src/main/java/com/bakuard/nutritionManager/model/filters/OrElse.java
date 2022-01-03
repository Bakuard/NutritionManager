package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.NotEnoughItemsException;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OrElse implements Constraint {

    /**
     * Создает и возвращает новый объект OrElse для указываемых ограничений.
     * @param a первый обязательный операнд ограничения OrElse.
     * @param b второй обязательный операнд ограничения OrElse.
     * @param other необязательные операнды ограничения OrElse.
     * @return возвращает новый объект OrElse для указываемых ограничений.
     * @throws MissingValueException если хотябы один из операндов или массив other имеют значение null.
     */
    public static OrElse of(Constraint a, Constraint b, Constraint... other) {
        return new OrElse(a, b, other);
    }

    /**
     * Создает и возвращает новый объект OrElse для указываемых ограничений.
     * @param constraints список ограничений выступающих как операнды данного ограничения.
     * @return возвращает новый объект OrElse для указываемых ограничений.
     * @throws NotEnoughItemsException если кол-во ограничений в списке constraints меньше двух.
     * @throws MissingValueException если хотябы один из операндов имеет значение null или передаваемый список
     *                               операндов имеет значение null.
     */
    public static OrElse of(List<Constraint> constraints) {
        return new OrElse(constraints);
    }


    private final ImmutableList<Constraint> operands;

    private OrElse(Constraint a, Constraint b, Constraint... other) {
        MissingValueException.check(other, getClass(), "operands");
        ArrayList<Constraint> operands = new ArrayList<>(Arrays.asList(other));
        operands.add(a);
        operands.add(b);
        operands.forEach(op -> MissingValueException.check(op, getClass(), "operand"));

        this.operands = ImmutableList.copyOf(operands);
    }

    private OrElse(List<Constraint> operands) {
        MissingValueException.check(operands, getClass(), "operands");
        operands.forEach(op -> MissingValueException.check(op, getClass(), "operand"));

        if(operands.size() < 2) {
            throw new NotEnoughItemsException(
                    "OrElse takes at least two operands", getClass(), "constraints");
        }
        this.operands = ImmutableList.copyOf(operands);
    }

    @Override
    public Type getType() {
        return Type.OR_ELSE;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return operands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrElse orElse = (OrElse) o;
        return operands.equals(orElse.operands);
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

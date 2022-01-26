package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndFilter implements Filter {

    /**
     * Создает и возвращает новый объект ограничения AndConstraint.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return новый объект ограничения AndConstraint.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотябы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static AndFilter of(List<Filter> filters) {
        return new AndFilter(ImmutableList.copyOf(filters));
    }

    /**
     * Создает и возвращает новый объект ограничения AndConstraint.
     * @param a первый обязательный операнд ограничения AndConstraint.
     * @param b второй обязательный операнд ограничения AndConstraint.
     * @param other другие не обязательные операнды ограничения AndConstraint.
     * @return новый объект ограничения AndConstraint.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static AndFilter of(Filter a, Filter b, Filter... other) {
        ArrayList<Filter> filters = new ArrayList<>();
        filters.add(a);
        filters.add(b);
        if(other == null) filters.add(null);
        else filters.addAll(Arrays.asList(other));

        return new AndFilter(filters);
    }


    private final ImmutableList<Filter> operands;

    private AndFilter(List<Filter> operands) {
        Checker.of(getClass(), "constructor").
                nullValue("operands", operands).
                containsNull("operands", operands).
                notEnoughItems("operands", operands, 2).
                checkWithServiceException();

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


}
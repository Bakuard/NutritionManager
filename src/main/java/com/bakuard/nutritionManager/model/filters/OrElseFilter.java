package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OrElseFilter implements Filter {

    /**
     * Создает и возвращает новый объект OrElse для указываемых ограничений.
     * @param a первый обязательный операнд ограничения OrElse.
     * @param b второй обязательный операнд ограничения OrElse.
     * @param other необязательные операнды ограничения OrElse.
     * @return возвращает новый объект OrElse для указываемых ограничений.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотябы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static OrElseFilter of(Filter a, Filter b, Filter... other) {
        List<Filter> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        if(other == null) list.add(null);
        else list.addAll(Arrays.asList(other));

        return new OrElseFilter(list);
    }

    /**
     * Создает и возвращает новый объект OrElse для указываемых ограничений.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return возвращает новый объект OrElse для указываемых ограничений.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static OrElseFilter of(List<Filter> filters) {
        return new OrElseFilter(filters);
    }


    private final ImmutableList<Filter> operands;

    private OrElseFilter(List<Filter> operands) {
        Checker.of(getClass(), "operands").
                nullValue("operands", operands).
                containsNull("operands", operands).
                notEnoughItems("operands", operands, 2).
                checkWithServiceException();

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

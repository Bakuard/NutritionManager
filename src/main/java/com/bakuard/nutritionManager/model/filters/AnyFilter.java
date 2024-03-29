package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

/**
 * Данное ограничение используется при фильтрации продуктов, блюд и меню по какому-то конкретному полю строкового
 * типа. Если значение искомого поля продукта, блюда или меню соответствует любому из перечисленных в данном
 * фильтре значений - то такой продукт, блюдо или меню соответствует данному фильтру.<br/><br/>
 * Определить фильтруемое поле можно по возвращаемому значению метода {@link #getType()}<br/><br/>
 * Объекты данного класса не изменяемы.
 */
public class AnyFilter extends AbstractFilter {

    private final ImmutableList<String> values;
    private final Type type;

    AnyFilter(List<String> values, int minItems, Type type) {
        Validator.check(
                "AnyFilter." + type, notNull(values).
                        and(() -> notContainsNull(values)).
                        and(() -> min(values.size(), minItems)).
                        and(() -> noneMatch(values, String::isBlank))
        );

        this.values = ImmutableList.copyOf(values);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public ImmutableList<String> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyFilter anyFilter = (AnyFilter) o;
        return Objects.equals(values, anyFilter.values) && type == anyFilter.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, type);
    }

    @Override
    public String toString() {
        return getType().name() + values;
    }

}

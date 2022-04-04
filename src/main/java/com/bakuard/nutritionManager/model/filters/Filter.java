package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface Filter {

    public static enum Type {
        OR_ELSE,
        AND,
        MIN_TAGS,
        CATEGORY,
        SHOPS,
        GRADES,
        MANUFACTURER,
        INGREDIENTS,
        USER,
        MIN_QUANTITY
    }

    /**
     * Создает и возвращает новый объект ограничения AndFilter.
     * @param a первый обязательный операнд ограничения AndFilter.
     * @param b второй обязательный операнд ограничения AndFilter.
     * @param other другие не обязательные операнды ограничения AndFilter.
     * @return новый объект ограничения AndFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static AndFilter and(Filter a, Filter b, Filter... other) {
        return new AndFilter(toList(a, b, other));
    }

    /**
     * Создает и возвращает новый объект ограничения AndFilter.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return новый объект ограничения AndFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотябы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static AndFilter and(List<Filter> filters) {
        return new AndFilter(filters);
    }

    /**
     * Создает и возвращает новый объект OrElseFilter для указываемых ограничений.
     * @param a первый обязательный операнд ограничения OrElseFilter.
     * @param b второй обязательный операнд ограничения OrElseFilter.
     * @param other необязательные операнды ограничения OrElseFilter.
     * @return возвращает новый объект OrElseFilter для указываемых ограничений.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотябы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static OrElseFilter orElse(Filter a, Filter b, Filter... other) {
        return new OrElseFilter(toList(a, b, other));
    }

    /**
     * Создает и возвращает новый объект OrElseFilter для указываемых ограничений.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return возвращает новый объект OrElseFilter для указываемых ограничений.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static OrElseFilter orElse(List<Filter> filters) {
        return new OrElseFilter(filters);
    }

    public static AnyFilter anyCategory(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Filter.Type.CATEGORY);
    }

    public static AnyFilter anyCategory(List<String> values) {
        return new AnyFilter(values, 1, Type.CATEGORY);
    }

    public static AnyFilter anyManufacturer(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.MANUFACTURER);
    }

    public static AnyFilter anyManufacturer(List<String> values) {
        return new AnyFilter(values, 1, Type.MANUFACTURER);
    }

    public static AnyFilter anyShop(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.SHOPS);
    }

    public static AnyFilter anyShop(List<String> values) {
        return new AnyFilter(values, 1, Type.SHOPS);
    }

    public static AnyFilter anyGrade(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.GRADES);
    }

    public static AnyFilter anyGrade(List<String> values) {
        return new AnyFilter(values, 1, Type.GRADES);
    }

    public static AnyFilter anyIngredient(String productCategory, String... other) {
        return new AnyFilter(toList(productCategory, other), 1, Type.INGREDIENTS);
    }

    public static AnyFilter anyIngredient(List<String> productCategories) {
        return new AnyFilter(productCategories, 1, Type.INGREDIENTS);
    }

    public static MinTagsFilter minTags(Tag a, Tag... other) {
        return new MinTagsFilter(toList(a, other));
    }

    /**
     * Создает и возвращает новый объект MinTagsFilter содержащий указанные теги.
     * @param values теги для которых определяется создаваемое ограничение MinTagsFilter.
     * @return новый объект MinTagsFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из элементов имеет значение null.<br/>
     *          2. если передаваемый список имеет значение null.<br/>
     *          3. если передаваемый список пустой.
     */
    public static MinTagsFilter minTags(List<Tag> values) {
        return new MinTagsFilter(values);
    }

    public static UserFilter user(User user) {
        return new UserFilter(user);
    }

    public static QuantityFilter less(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.LESS);
    }

    public static QuantityFilter lessOrEqual(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.LESS_OR_EQUAL);
    }

    public static QuantityFilter greater(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.GREATER);
    }

    public static QuantityFilter greaterOrEqual(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.GREATER_OR_EQUAL);
    }

    public static QuantityFilter equal(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.EQUAL);
    }


    private static List<String> toList(String a, String... other) {
        ArrayList<String> result = Lists.newArrayList(other);
        result.add(a);
        return result;
    }

    private static List<Tag> toList(Tag a, Tag... other) {
        ArrayList<Tag> result = Lists.newArrayList(other);
        result.add(a);
        return result;
    }

    private static List<Filter> toList(Filter a, Filter b, Filter... other) {
        ArrayList<Filter> result = Lists.newArrayList(other);
        result.add(a);
        result.add(b);
        return result;
    }


    public Type getType();

    public ImmutableList<Filter> getOperands();

    public boolean containsOnly(Type... types);

    public boolean containsAtLeast(Type... types);

    public <T extends Filter> T findAny(Type type);

}

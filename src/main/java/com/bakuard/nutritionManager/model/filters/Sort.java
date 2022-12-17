package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.bakuard.nutritionManager.validation.Rule.anyMatch;

/**
 * Представляет параметры сортировки. Также содержит набор статических методов для преобразования
 * параметров сортировки заданных в виде строки в объекты данного класса.
 */
public class Sort {

    /**
     * Возвращает параметры сортировки по умолчанию для продуктов.
     */
    public static Sort productDefaultSort() {
        return products("category");
    }

    /**
     * Возвращает параметры сортировки по умолчанию для блюд.
     */
    public static Sort dishDefaultSort() {
        return dishes("name");
    }

    /**
     * Возвращает параметры сортировки по умолчанию для менюф.
     */
    public static Sort menuDefaultSort() {
        return menus("name");
    }

    /**
     * Преобразует параметры сортировки sortRules в объект Sort. При этом проверяется -
     * поддерживаются ли указанные параметры для продуктов. <br/><br/>
     * Формат строки sortRules: <br/>
     * Общий вид: field1_direction, field2_direction, ..., fieldN_direction
     * <ol>
     * <li> где fieldN - поле сортировки. </li>
     * <li> direction - направление сортировки. Допустимые значения - "asc", "desc", "ascending", "descending".
     *                  Направление сортировки можно опустить оставив только поле. В таком случае по умолчанию
     *                  используется значение "asc". </li>
     * </ol>
     * Пример: category, price_desc
     * @param sortRules параметры сортировки в виде строки
     * @return параметры сортировки в виде объекта данного класса
     */
    public static Sort products(String sortRules) {
        List<Param> params = toSortRuleStream(sortRules).
                map(p -> {
                    String[] sr = p.split("_");
                    return new Param(checkParameter(sr, "products"), checkDirection(sr));
                }).
                toList();
        return params.isEmpty() ? productDefaultSort() : new Sort(params);
    }

    /**
     * Преобразует параметры сортировки sortRules в объект Sort. При этом проверяется -
     * поддерживаются ли указанные параметры для блюд. <br/><br/>
     * Формат строки sortRules: <br/>
     * Общий вид: field1_direction, field2_direction, ..., fieldN_direction
     * <ol>
     * <li> где fieldN - поле сортировки. </li>
     * <li> direction - направление сортировки. Допустимые значения - "asc", "desc", "ascending", "descending".
     *                  Направление сортировки можно опустить оставив только поле. В таком случае по умолчанию
     *                  используется значение "asc". </li>
     * </ol>
     * Пример: name, dishId_desc
     * @param sortRules параметры сортировки в виде строки
     * @return параметры сортировки в виде объекта данного класса
     */
    public static Sort dishes(String sortRules) {
        List<Param> params = toSortRuleStream(sortRules).
                map(p -> {
                    String[] sr = p.split("_");
                    return new Param(checkParameter(sr, "dishes"), checkDirection(sr));
                }).
                toList();
        return params.isEmpty() ? productDefaultSort() : new Sort(params);
    }

    /**
     * Преобразует параметры сортировки sortRules в объект Sort. При этом проверяется -
     * поддерживаются ли указанные параметры для меню. <br/><br/>
     * Формат строки sortRules: <br/>
     * Общий вид: field1_direction, field2_direction, ..., fieldN_direction
     * <ol>
     * <li> где fieldN - поле сортировки. </li>
     * <li> direction - направление сортировки. Допустимые значения - "asc", "desc", "ascending", "descending".
     *                  Направление сортировки можно опустить оставив только поле. В таком случае по умолчанию
     *                  используется значение "asc". </li>
     * </ol>
     * Пример: name, menuId_desc
     * @param sortRules параметры сортировки в виде строки
     * @return параметры сортировки в виде объекта данного класса
     */
    public static Sort menus(String sortRules) {
        List<Param> params = toSortRuleStream(sortRules).
                map(p -> {
                    String[] sr = p.split("_");
                    return new Param(checkParameter(sr, "menus"), checkDirection(sr));
                }).
                toList();
        return params.isEmpty() ? productDefaultSort() : new Sort(params);
    }


    private static boolean checkDirection(String[] preparedSortRule) {
        String sortDirection = preparedSortRule.length > 1 ? preparedSortRule[1] : "asc";
        final String processedSortDirection = StringUtils.normalizeSpace(sortDirection).toUpperCase();
        boolean isAscending = true;

        Validator.check("Sort.direction",
                anyMatch(List.of("asc", "desc", "ascending", "descending"), p -> p.equalsIgnoreCase(sortDirection))
        );

        switch(processedSortDirection) {
            case "ASC", "ASCENDING" -> isAscending = true;
            case "DESC", "DESCENDING" -> isAscending = false;
        }

        return isAscending;
    }

    private static String checkParameter(String[] preparedSortRule, String sortedEntityTypeName) {
        final String processedParameter = StringUtils.normalizeSpace(preparedSortRule[0]);

        switch(sortedEntityTypeName) {
            case "products" -> Validator.check(
                    "Sort.products.parameter", anyMatch(List.of("category", "price", "productId"), processedParameter)
            );
            case "dishes" -> Validator.check(
                    "Sort.dishes.parameter", anyMatch(List.of("name", "dishId"), processedParameter)
            );
            case "menus" -> Validator.check(
                    "Sort.menus.parameter", anyMatch(List.of("name", "menuId"), processedParameter)
            );
            default -> throw new IllegalArgumentException(
                    "Unknown sortedEntityTypeName = '" + sortedEntityTypeName + '\'');
        }

        return processedParameter;
    }

    private static Stream<String> toSortRuleStream(String sortRules) {
        return sortRules == null || sortRules.isBlank() ?
                Stream.empty() :
                Arrays.stream(sortRules.split(",")).map(String::trim);
    }


    private final List<Param> parameters;

    private Sort(List<Param> parameters) {
        this.parameters = parameters;
    }

    /**
     * Возвращает все параметры сортировки в порядке указанном при создании данного объекта.
     */
    public List<Param> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Возвращает все параметры сортировки в виде Stream, который возвращает из в порядке указанном
     * при создании данного объекта.
     */
    public Stream<Param> getParametersAsStream() {
        return parameters.stream();
    }

    /**
     * Перебирает все параметры сортировки в порядке указанном при создании данного объекта.
     * @param action функция обратного вызова обрабатывающая каждый параметр сортировки.
     */
    public void forEachParam(Consumer<Param> action) {
        parameters.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort sort = (Sort) o;
        return parameters.equals(sort.parameters);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    @Override
    public String toString() {
        return "Sort" + parameters;
    }


    public static record Param(String param, boolean direction) {

        public boolean isAscending() {
            return direction;
        }

        public boolean isDescending() {
            return !direction;
        }

    }

}

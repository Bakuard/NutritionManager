package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.util.Pair;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Задает правило сортировки продуктов. Объекты данного класса неизменяемы.
 */
public final class ProductSort {

    private static final ProductSort defaultSort =
            new ProductSort(ProductSort.Parameter.CATEGORY, SortDirection.ASCENDING);

    /**
     * Возвращает правило сортировки продуктов применяемое по умолчанию (т.е. в тех случаях, когда пользователь
     * не указал правило сортировки). По умолчанию продукты сортируются по категории в порядке возрастания.
     * @return правило сортировки продуктов применяемое по умолчанию.
     */
    public static ProductSort defaultSort() {
        return defaultSort;
    }


    /**
     * Параметры(поля объекта) по которым идет сортировка.
     */
    public enum Parameter {
        /**
         * Указвает, что сортировка проводится по категории продукта.
         */
        CATEGORY,

        /**
         * Указывает, что сортировка проводится по точке продажи продукта.
         */
        SHOP,

        /**
         * Указывает, что сортировка проводится по цене продукта.
         */
        PRICE,

        /**
         * Указывает, что сортировка проводится по сорту или разновдиности продукта.
         */
        VARIETY,

        /**
         * Указывает, что сортировка проводится по производителю продукта.
         */
        MANUFACTURER
    }

    private ArrayList<Pair<Parameter, SortDirection>> params;

    /**
     * Создает объект представляющий правило сортировки продуктов по параметру parameter.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание или убывание).
     * @throws ValidateException если parameter или direction являются null.
     */
    public ProductSort(Parameter parameter, SortDirection direction) {
        Checker.of().
                notNull("parameter", parameter).
                notNull("direction", direction).
                validate();

        params = new ArrayList<>();
        params.add(new Pair<>(parameter, direction));
    }

    /**
     * Создает объект представляющий правило сортировки продуктов по параметру parameter.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание или убывание).
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *                          1. если parameter или direction являются null.<br/>
     *                          2. если строка параметра или направления сортировки не соответсуют
     *                             ни одному известному значению.
     */
    public ProductSort(String parameter, String direction) {
        params = new ArrayList<>();
        params.add(from(parameter, direction));
    }

    private ProductSort(ArrayList<Pair<Parameter, SortDirection>> params) {
        this.params = params;
    }

    /**
     * Создает и возвращает новый объект сортировки, который в дополнении к параметрам текущего объекта также
     * проводит сортировку по указанному параметру. Если текущий объект уже учитывает указанный параметр, то
     * для нового объекта сортировки этот параметр будет иметь наивысший приоритет при сортировке, иначе данный
     * параметр будет иметь наименьший приоритет при сортировке относительно всех параметров текущего объекта.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание или убывание).
     * @return новый объект сортировки.
     * @throws ValidateException если parameter или direction является null.
     */
    public ProductSort byParameter(Parameter parameter, SortDirection direction) {
        Checker.of().
                notNull("parameter", parameter).
                notNull("direction", direction).
                validate();

        Pair<Parameter, SortDirection> pair = new Pair<>(parameter, direction);
        ArrayList<Pair<Parameter, SortDirection>> newParams = new ArrayList<>(params);

        int index = newParams.indexOf(pair);
        if(index == -1) newParams.add(pair);
        else Collections.swap(newParams, 0, index);

        return new ProductSort(newParams);
    }

    /**
     * Возвращает кол-во параметров данного объекта, которые будут участвовать в сортировке.
     * @return кол-во параметров данного объекта, которые будут участвовать в сортировке.
     */
    public int getCountParameters() {
        return params.size();
    }

    /**
     * Возвращает тип параметра участвующего в сортировке по его приоритету задаваемому индексом. Индексы
     * начинаются с нуля, самый последний индекс имеет значение равное {@link #getCountParameters()} - 1.
     * Чем выше индекс, тем ниже приоритет параметра в сортировке.
     * @param parameterIndex индекс искомого параметра.
     * @return параметр сортировки.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *                          1. если parameterIndex < 0.<br/>
     *                          2. parameterIndex >= {@link #getCountParameters()}.
     */
    public Parameter getParameterType(int parameterIndex) {
        Checker.of().
                range("parameterIndex", parameterIndex, 0, params.size() - 1).
                validate("Fail to get parameter type from ProductSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex);

        return params.get(parameterIndex).getFirst();
    }

    /**
     * Возвращает направление сортировки для параметра с индексом parameterIndex. Индексы начинаются с нуля,
     * самый последний индекс имеет значение равное {@link #getCountParameters()} - 1. Чем выше индекс, тем ниже
     * приоритет параметра в сортировке.
     * @param parameterIndex индекс искомого параметра.
     * @return направление сортировки.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *                          1. если parameterIndex < 0.<br/>
     *                          2. parameterIndex >= {@link #getCountParameters()}.
     */
    public SortDirection getDirection(int parameterIndex) {
        Checker.of().
                range("parameterIndex", parameterIndex, 0, params.size() - 1).
                validate("Fail to get direction sort from ProductSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex);

        return params.get(parameterIndex).getSecond();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSort that = (ProductSort) o;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }


    private Pair<Parameter, SortDirection> from(String parameter, String direction) {
        Checker checker = Checker.of().
                notNull("parameter", parameter).
                notNull("direction", direction).
                validate();

        Parameter p = null;
        SortDirection d = null;

        switch(parameter) {
            case "category" -> p = Parameter.CATEGORY;
            case "price" -> p = Parameter.PRICE;
            case "variety" -> p = Parameter.VARIETY;
            case "shop" -> p = Parameter.SHOP;
            case "manufacturer" -> p = Parameter.MANUFACTURER;
            default -> checker.
                    failure("parameter", ConstraintType.UNKNOWN_PARAMETER);
        }

        switch(direction) {
            case "asc" -> d = SortDirection.ASCENDING;
            case "desc" -> d = SortDirection.DESCENDING;
            default -> checker.
                    failure("direction", ConstraintType.UNKNOWN_PARAMETER);
        }

        checker.validate();

        return new Pair<>(p, d);
    }

}

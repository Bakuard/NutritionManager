package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        ValidateException.check(
                Rule.of("ProductSort.parameter").notNull(parameter),
                Rule.of("ProductSort.direction").notNull(direction)
        );

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
        ValidateException.check(
                Rule.of("ProductSort.parameter").notNull(parameter),
                Rule.of("ProductSort.direction").notNull(direction)
        );

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
        ValidateException.check(
                "ProductSort.getParameterType",
                "Fail to get parameter type from ProductSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex,
                Rule.of("ProductSort.parameterIndex").range(parameterIndex, 0, params.size() - 1)
        );

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
        ValidateException.check(
                "ProductSort.getDirection",
                "Fail to get direction sort from ProductSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex,
                Rule.of("ProductSort.parameterIndex").range(parameterIndex, 0, params.size() - 1)
        );

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
        Container<SortDirection> c = new Container<>();

        ValidateException.check(
                Rule.of("ProductSort.parameter").notNull(parameter),
                Rule.of("ProductSort.existing_parameter").notNull(Parameter.valueOf(parameter)),
                Rule.of("ProductSort.direction").notNull(direction).
                        and(r -> {
                            switch(direction) {
                                case "asc": c.set(SortDirection.ASCENDING);
                                case "desc": c.set(SortDirection.DESCENDING);
                            }

                            if(c.isEmpty()) return r.failure(Constraint.CONTAINS_ITEM);
                            else return r.success(Constraint.CONTAINS_ITEM);
                        })
        );

        return new Pair<>(Parameter.valueOf(parameter), c.get());
    }

}

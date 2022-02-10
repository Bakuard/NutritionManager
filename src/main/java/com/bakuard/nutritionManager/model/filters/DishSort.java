package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;
import com.bakuard.nutritionManager.model.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * Задает правило сортировки блюд. Объекты данного класса неизменяемы.
 */
public final class DishSort {

    private static final DishSort defaultSort =
            new DishSort(Parameter.NAME, SortDirection.ASCENDING);

    /**
     * Возвращает правило сортировки блюд применяемое по умолчанию (т.е. в тех случаях, когда пользователь
     * не указал правило сортировки). По умолчанию блюда сортируются по имени в порядке возрастания.
     * @return правило сортировки блюд применяемое по умолчанию.
     */
    public static DishSort defaultSort() {
        return defaultSort;
    }


    /**
     * Павраметры по которым идет сортировка.
     */
    public enum Parameter {
        /**
         * Указывает, что сортировка проводится по наименованию блюда.
         */
        NAME,

        /**
         * Указывает, что сортировка проводится по единице измерения кол-ва блюда.
         */
        UNIT
    }

    private ArrayList<Pair<Parameter, SortDirection>> params;

    /**
     * Создает объект представляющий правило сортировки блюд по параметру parameter.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание ил убывание).
     * @throws ServiceException если parameter или direction является null.
     */
    public DishSort(Parameter parameter, SortDirection direction) {
        Checker.of(getClass(), "constructor").
                nullValue("parameter", parameter).
                nullValue("direction", direction).
                checkWithServiceException();

        params = new ArrayList<>();
        params.add(new Pair<>(parameter, direction));
    }

    /**
     * Создает объект представляющий правило сортировки блюд по параметру parameter.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание или убывание).
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *                          1. если parameter или direction являются null.<br/>
     *                          2. если строка параметра или направления сортировки не соответсуют
     *                             ни одному известному значению.
     */
    public DishSort(String parameter, String direction) {
        params = new ArrayList<>();
        params.add(from(parameter, direction));
    }

    private DishSort(ArrayList<Pair<Parameter, SortDirection>> params) {
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
     * @throws ServiceException если parameter или direction является null.
     */
    public DishSort byParameter(Parameter parameter, SortDirection direction) {
        Checker.of(getClass(), "byParameter").
                nullValue("parameter", parameter).
                nullValue("direction", direction).
                checkWithServiceException();

        Pair<Parameter, SortDirection> pair = new Pair<>(parameter, direction);
        ArrayList<Pair<Parameter, SortDirection>> newParams = new ArrayList<>(params);

        int index = newParams.indexOf(pair);
        if(index == -1) newParams.add(pair);
        else Collections.swap(newParams, 0, index);

        return new DishSort(newParams);
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
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *                          1. если parameterIndex < 0.<br/>
     *                          2. parameterIndex >= {@link #getCountParameters()}.
     */
    public Parameter getParameterType(int parameterIndex) {
        Checker.of(getClass(), "getParameterType").
                outOfRange("parameterIndex", parameterIndex, 0, params.size() - 1).
                checkWithServiceException("Fail to get parameter type from DishSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex);

        return params.get(parameterIndex).getFirst();
    }

    /**
     * Возвращает направление сортировки для параметра с индексом parameterIndex. Индексы начинаются с нуля,
     * самый последний индекс имеет значение равное {@link #getCountParameters()} - 1. Чем выше индекс, тем ниже
     * приоритет параметра в сортировке.
     * @param parameterIndex индекс искомого параметра.
     * @return направление сортировки.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *                          1. если parameterIndex < 0.<br/>
     *                          2. parameterIndex >= {@link #getCountParameters()}.
     */
    public SortDirection getDirection(int parameterIndex) {
        Checker.of(getClass(), "getParameterType").
                outOfRange("parameterIndex", parameterIndex, 0, params.size() - 1).
                checkWithServiceException("Fail to get direction sort from DishSort. Index must belong " +
                        "[0, " + (params.size() - 1) + "], actual = " + parameterIndex);

        return params.get(parameterIndex).getSecond();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishSort dishSort = (DishSort) o;
        return params.equals(dishSort.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }


    private Pair<Parameter, SortDirection> from(String parameter, String direction) {
        Checker checker = Checker.of(getClass(), "byParameter").
                nullValue("parameter", parameter).
                nullValue("direction", direction).
                checkWithServiceException();

        Parameter p = null;
        SortDirection d = null;

        switch(parameter) {
            case "name" -> p = Parameter.NAME;
            case "unit" -> p = Parameter.UNIT;
            default -> checker.
                    addConstraint("parameter", ConstraintType.UNKNOWN_PARAMETER);
        }

        switch(direction) {
            case "asc" -> d = SortDirection.ASCENDING;
            case "desc" -> d = SortDirection.DESCENDING;
            default -> checker.
                    addConstraint("direction", ConstraintType.UNKNOWN_PARAMETER);
        }

        checker.checkWithServiceException();

        return new Pair<>(p, d);
    }

}

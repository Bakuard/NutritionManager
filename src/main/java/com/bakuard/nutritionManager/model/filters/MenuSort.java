package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.OutOfRangeException;

/**
 * Задает правило сортировки меню. Объекты данного класса неизменяемы.
 */
public final class MenuSort {

    /**
     * Павраметры по которым идет сортировка.
     */
    public enum Parameter {
        /**
         * Указывает, что сортировка проводится по наименованию меню.
         */
        NAME
    }

    /**
     * Создает объект представляющий правило сортировки меню по параметру parameter.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание ил убывание).
     * @throws MissingValueException если parameter или direction является null.
     */
    public MenuSort(Parameter parameter, SortDirection direction) {

    }

    /**
     * Создает и возвращает новый объект сортировки, который в дополнении к параметрам текущего объекта также
     * проводит сортировку по указанному параметру. Если текущий объект уже учитывает указанный параметр, то
     * для нового объекта сортировки этот параметр будет иметь наивысший приоритет при сортировке, иначе данный
     * параметр будет иметь наименьший приоритет при сортировке относительно всех параметров текущего объекта.
     * @param parameter параметр сортировки.
     * @param direction направление сортировки (возрастание ил убывание).
     * @return новый объект сортировки.
     * @throws MissingValueException если parameter или direction является null.
     */
    public ProductSort byParameter(Parameter parameter, SortDirection direction) {
        return null;
    }

    /**
     * Возвращает кол-во параметров данного объекта, которые будут участвовать в сортировке.
     * @return кол-во параметров данного объекта, которые будут участвовать в сортировке.
     */
    public int getCountParameters() {
        return 0;
    }

    /**
     * Возвращает тип параметра участвующего в сортировке по его приоритету задаваемому индексом. Индексы
     * начинаются с нуля, самый последний индекс имеет значение равное {@link #getCountParameters()} - 1.
     * Чем выше индекс, тем ниже приоритет параметра в сортировке.
     * @param parameterIndex индекс искомого параметра.
     * @return параметр сортировки.
     * @throws OutOfRangeException если parameterIndex < 0 или parameterIndex >= {@link #getCountParameters()}.
     */
    public Parameter getParameterType(int parameterIndex) {
        return null;
    }

    /**
     * Возвращает направление сортировки для параметра с индексом parameterIndex. Индексы начинаются с нуля,
     * самый последний индекс имеет значение равное {@link #getCountParameters()} - 1. Чем выше индекс, тем ниже
     * приоритет параметра в сортировке.
     * @param parameterIndex индекс искомого параметра.
     * @return направление сортировки.
     * @throws OutOfRangeException если parameterIndex < 0 или parameterIndex >= {@link #getCountParameters()}.
     */
    public SortDirection getDirection(int parameterIndex) {
        return null;
    }

}

package com.bakuard.nutritionManager.model.util;

/**
 * Общий интерфейс для всех классов используемых для жадания параметров пагинации.
 */
public interface Pageable {

    /**
     * Возвращает ожидаемый размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     * 1. если страница является последней или первой и в обоих случаях оставшееся кол-во элементов меньше указанного
     * значения.<br/>
     * 2. если заданный размер страницы меньше 1, то фактический размер страницы будет равен 1.<br/>
     * Все классы релизующие данный интерфейс должны быть неизменяемыми.
     * @return ожидаемый размер страницы.
     */
    public int getExpectedPageSize();

}

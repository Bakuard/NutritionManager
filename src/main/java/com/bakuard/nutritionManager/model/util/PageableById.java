package com.bakuard.nutritionManager.model.util;

import java.util.Objects;
import java.util.UUID;

/**
 * Используется для пагинации. Представляет собой запрос на получение страницы({@link Page}) данных. Содержит
 * данные о размере страниц и уникальном идентификаторе искомого объекта. На основе этих данных возвращается страница,
 * которая содержит объект с указанным идентификатором.<br/>
 * Объекты данного класса являются неизменяемыми.
 */
public class PageableById implements Pageable {

    /**
     * Создает и возвращает объект содержащий параметры пагинации. Созданный объект будет содержать в
     * точности те значения, которые были указаны при вызове данного метода.
     * @param expectedPageSize размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     *                 1. если страница является последней или первой и в обоих случаях оставшееся кол-во
     *                    элементов меньше указанного значения.<br/>
     *                 2. если заданный размер страницы меньше 1
     * @param searchedId уникальный идентификатор искомого объекта.
     * @return новый объект PageableById.
     * @throws NullPointerException если searchedId является null.
     */
    public static PageableById of(int expectedPageSize, UUID searchedId) {
        return new PageableById(expectedPageSize, searchedId);
    }

    private final int expectedPageSize;
    private final UUID searchedId;

    private PageableById(int expectedPageSize, UUID searchedId) {
        this.expectedPageSize = expectedPageSize;
        this.searchedId = Objects.requireNonNull(searchedId, "searchedId can't be null");
    }

    /**
     * Возвращает ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     * @return ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     */
    @Override
    public int getExpectedPageSize() {
        return expectedPageSize;
    }

    /**
     * Возвращает уникальный идентификатор искомого объекта.
     * @return уникальный идентификатор искомого объекта.
     */
    public UUID getSearchedId() {
        return searchedId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageableById that = (PageableById) o;
        return expectedPageSize == that.expectedPageSize && searchedId.equals(that.searchedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedPageSize, searchedId);
    }

    @Override
    public String toString() {
        return "PageableById{" +
                "expectedPageSize=" + expectedPageSize +
                ", searchedId=" + searchedId +
                '}';
    }

}

package com.bakuard.nutritionManager.model.util;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Используется для пагинации. Представляет собой запрос на получение страницы({@link Page}) данных. Содержит
 * информацию о номере получаемой страницы и размере (кол-ве элементов) каждой страницы, на которые разбивается
 * исходная выборка. Нумерация страниц начинается с 0. Минимальный возможный размер страницы равен 1.<br/>
 * Объекты данного класса являются неизменяемыми.
 */
public final class Pageable {

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable указывает на страницу,
     * которая должна содержать элемент с указанным порядковым номером (нумерация наченается с нуля).
     * Надо напомнить что выборка элементов осуществляется из упорядоченноо множества элементов,
     * правила сортировки для которого задаются отдельно.
     * @param expectedPageSize размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     *                 1. если страница является последней или первой и в обоих случаях оставшееся кол-во
     *                    элементов меньше указанного значения.<br/>
     *                 2. если заданный размер страницы меньше 1.
     * @param itemIndex порядковый номер элемента по которому определяется ожидаемый номер страницы.
     *                     Если переданное значение меньше 0, то в качестве итогового значения для
     *                     порядкового номера элемента будет взято 0. Фактический номер страницы, полученной
     *                     с помощью данного объекта Pageable, будет отличаться, если страница с указанным
     *                     номером выходит за границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable ofIndex(int expectedPageSize, int itemIndex) {
        return ofIndex(expectedPageSize, BigInteger.valueOf(itemIndex));
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable указывает на страницу,
     * которая должна содержать элемент с указанным порядковым номером (нумерация наченается с нуля).
     * Надо напомнить что выборка элементов осуществляется из упорядоченноо множества элементов,
     * правила сортировки для которого задаются отдельно.
     * @param expectedPageSize размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     *                 1. если страница является последней или первой и в обоих случаях оставшееся кол-во
     *                    элементов меньше указанного значения.<br/>
     *                 2. если заданный размер страницы меньше 1.
     * @param itemIndex порядковый номер элемента по которому определяется ожидаемый номер страницы.
     *                     Если переданное значение меньше 0, то в качестве итогового значения для
     *                     порядкового номера элемента будет взято 0. Фактический номер страницы, полученной
     *                     с помощью данного объекта Pageable, будет отличаться, если страница с указанным
     *                     номером выходит за границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable ofIndex(int expectedPageSize, BigInteger itemIndex) {
        expectedPageSize = Math.max(1, expectedPageSize);
        itemIndex = itemIndex.max(BigInteger.ZERO).
                divide(BigInteger.valueOf(expectedPageSize));

        return of(expectedPageSize, itemIndex);
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable будет содержать в точности те значения,
     * которые были указаны при вызове данного метода.
     * @param expectedPageSize размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     *                 1. если страница является последней или первой и в обоих случаях оставшееся кол-во
     *                    элементов меньше указанного значения.<br/>
     *                 2. если заданный размер страницы меньше 1
     * @param expectedPageNumber ожидаемый номер страницы. Фактический номер страницы, полученной с помощью данного
     *                           объекта Pageable, будет отличаться, если страница с указанным номером выходит за
     *                           границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable of(int expectedPageSize, int expectedPageNumber) {
        return of(expectedPageSize, BigInteger.valueOf(expectedPageNumber));
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable будет содержать в точности те значения,
     * которые были указаны при вызове данного метода.
     * @param expectedPageSize размер страницы. Фактический размер страницы может отличаться в след. случаях:<br/>
     *                 1. если страница является последней или первой и в обоих случаях оставшееся кол-во
     *                    элементов меньше указанного значения.<br/>
     *                 2. если заданный размер страницы меньше 1
     * @param expectedPageNumber ожидаемый номер страницы. Фактический номер страницы, полученной с помощью данного
     *                           объекта Pageable, будет отличаться, если страница с указанным номером выходит за
     *                           границы выборки.
     * @return новый объект Pageable.
     * @throws NullPointerException если expectedPageNumber равен null.
     */
    public static Pageable of(int expectedPageSize, BigInteger expectedPageNumber) {
        return new Pageable(expectedPageSize, expectedPageNumber);
    }

    /**
     * Создает и возвращает пустую страницу представляющую первую страницу пустой выборки. Номер данной страницы
     * будут равен 0, а максимальный ожидаемый размер 1.
     * @param <T> тип объектов из которых состоит выборка.
     * @return пустую страницы представляющую первую страницу пустой выборки.
     */
    public static <T>Page<T> firstEmptyPage() {
        return new Pageable(1, BigInteger.ZERO).
                createPageMetadata(BigInteger.ZERO, 1).
                createPage(List.of());
    }


    private final int expectedPageSize;
    private final BigInteger expectedPageNumber;

    private Pageable(int expectedPageSize, BigInteger expectedPageNumber) {
        this.expectedPageSize = expectedPageSize;
        this.expectedPageNumber = expectedPageNumber;
    }

    /**
     * Возвращает ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     * @return ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     */
    public int getExpectedPageSize() {
        return expectedPageSize;
    }

    /**
     * Возвращает ожидаемый номер страницы, которая будет полученна с помощью данного объекта Pageable.
     * @return ожидаемый номер страницы, которая будет полученна с помощью данного объекта Pageable.
     */
    public BigInteger getExpectedPageNumber() {
        return expectedPageNumber;
    }

    /**
     * Создает и возращает объект представляющий метаданные о исходной выборке и будущей страницы. Создание метаданых
     * страницы отделены от создания самой страницы (которая уже непосредственно содержит объекты из исходной
     * выборки), т.к. для получения непосредственно самих объектов для заполнения страницы могут заранее поднадобиться
     * её метаданные.
     * @param totalItems общее кол-во всех элементов содержащихся в исходной выборке для которой проводится пагинация.
     * @param maxPageSize максимально допустимый размер страницы. Фактический размер страницы не может быть больше
     *                    данного значения и при необходимости может быть урезан до данного значения.
     * @return возвращает метаданные исходной выборки и страницы.
     * @throws IllegalArgumentException если maxPageSize < 1
     */
    public Page.Metadata createPageMetadata(int totalItems, int maxPageSize) {
        return createPageMetadata(BigInteger.valueOf(totalItems), maxPageSize);
    }

    /**
     * Создает и возращает объект представляющий метаданные о исходной выборке и будущей страницы. Создание метаданых
     * страницы отделены от создания самой страницы (которая уже непосредственно содержит объекты из исходной
     * выборки), т.к. для получения непосредственно самих объектов для заполнения страницы могут заранее поднадобиться
     * её метаданные.
     * @param totalItems общее кол-во всех элементов содержащихся в исходной выборке для которой проводится пагинация.
     * @param maxPageSize максимально допустимый размер страницы. Фактический размер страницы не может быть больше
     *                    данного значения и при необходимости может быть урезан до данного значения.
     * @return возвращает метаданные исходной выборки и страницы.
     * @throws NullPointerException если totalItems равен null.
     * @throws IllegalArgumentException если totalItems меньше нуля или maxPageSize < 1.
     */
    public Page.Metadata createPageMetadata(BigInteger totalItems, int maxPageSize) {
        return new Page.Metadata(this, totalItems, maxPageSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pageable pageable = (Pageable) o;
        return expectedPageSize == pageable.expectedPageSize &&
                expectedPageNumber.equals(pageable.expectedPageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedPageSize, expectedPageNumber);
    }

    @Override
    public String toString() {
        return "Pageable{" +
                "expectedPageSize=" + expectedPageSize +
                ", expectedPageNumber=" + expectedPageNumber +
                '}';
    }

}

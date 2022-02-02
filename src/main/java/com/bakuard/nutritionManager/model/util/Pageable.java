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
     * которая должна содержать продукт с указанным порядковым номером (нумерация наченается с нуля).
     * Надо напомнить что выборка продуктов осуществляется из упорядоченноо множества продуктов,
     * правила сортировки для которого задаются отдельно.
     * @param expectedMaxPageSize максимальный размер страницы. Если переданное значение меньше 1, то
     *                            в качестве итогового значения будет взято 1.
     * @param productIndex порядковый номер продукта по которому определяется ожидаемый номер страницы.
     *                     Если переданное значение меньше 0, то в качестве итогового значения для
     *                     порядкового номера продукта будет взято 0. Фактический номер страницы, полученной
     *                     с помощью данного объекта Pageable, будет отличаться, если страница с указанным
     *                     номером выходит за границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable ofIndex(int expectedMaxPageSize, int productIndex) {
        return ofIndex(expectedMaxPageSize, BigInteger.valueOf(productIndex));
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable указывает на страницу,
     * которая должна содержать продукт с указанным порядковым номером (нумерация наченается с нуля).
     * Надо напомнить что выборка продуктов осуществляется из упорядоченноо множества продуктов,
     * правила сортировки для которого задаются отдельно.
     * @param expectedMaxPageSize максимальный размер страницы. Если переданное значение меньше 1, то
     *                            в качестве итогового значения будет взято 1.
     * @param productIndex порядковый номер продукта по которому определяется ожидаемый номер страницы.
     *                     Если переданное значение меньше 0, то в качестве итогового значения для
     *                     порядкового номера продукта будет взято 0. Фактический номер страницы, полученной
     *                     с помощью данного объекта Pageable, будет отличаться, если страница с указанным
     *                     номером выходит за границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable ofIndex(int expectedMaxPageSize, BigInteger productIndex) {
        int maxPageSize = Math.max(1, expectedMaxPageSize);
        productIndex = productIndex.max(BigInteger.ZERO);

        BigInteger expectedPageNumber = productIndex.divide(BigInteger.valueOf(maxPageSize));

        return of(maxPageSize, expectedPageNumber);
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable будет содержать в точности те значения,
     * которые были указаны при вызове данного метода.
     * @param expectedMaxPageSize максимальный размер страницы. Фактический размер страницы, полученной с помощью данного
     *                         объекта Pageable, будет меньше заданного значения, если она является последней или
     *                         первой и в обоих случаях общее кол-во элементов меньше указанного значения.
     * @param expectedPageNumber ожидаемый номер страницы. Фактический номер страницы, полученной с помощью данного
     *                           объекта Pageable, будет отличаться, если страница с указанным номером выходит за
     *                           границы выборки.
     * @return новый объект Pageable.
     */
    public static Pageable of(int expectedMaxPageSize, int expectedPageNumber) {
        return of(expectedMaxPageSize, BigInteger.valueOf(expectedPageNumber));
    }

    /**
     * Создает и возвращает новый объект Pageable. Созданный объект Pageable будет содержать в точности те значения,
     * которые были указаны при вызове данного метода.
     * @param expectedMaxPageSize максимальный размер страницы. Фактический размер страницы, полученной с помощью данного
     *                         объекта Pageable, будет меньше заданного значения, если она является последней или
     *                         первой и в обоих случаях общее кол-во элементов меньше указанного значения.
     * @param expectedPageNumber ожидаемый номер страницы. Фактический номер страницы, полученной с помощью данного
     *                           объекта Pageable, будет отличаться, если страница с указанным номером выходит за
     *                           границы выборки.
     * @return новый объект Pageable.
     * @throws NullPointerException если expectedPageNumber равен null.
     */
    public static Pageable of(int expectedMaxPageSize, BigInteger expectedPageNumber) {
        return new Pageable(expectedMaxPageSize, expectedPageNumber);
    }

    /**
     * Создает и возвращает пустую страницы представляющую первую страницу пустой выборки. Номер данной страницы
     * будут равен 0, а максимальный ожидаемый размер 1.
     * @param <T> тип объектов из которых состоит выборка.
     * @return пустую страницы представляющую первую страницу пустой выборки.
     */
    public static <T>Page<T> firstEmptyPage() {
        return new Pageable(1, BigInteger.ZERO).
                createPageMetadata(BigInteger.ZERO).
                createPage(List.of());
    }


    private final int expectedMaxPageSize;
    private final BigInteger expectedPageNumber;

    private Pageable(int expectedMaxPageSize, BigInteger expectedPageNumber) {
        this.expectedMaxPageSize = expectedMaxPageSize;
        this.expectedPageNumber = expectedPageNumber;
    }

    /**
     * Возвращает ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     * @return ожидаемый размер страницы, которая будет полученна с помощью данного объекта Pageable.
     */
    public int getExpectedMaxPageSize() {
        return expectedMaxPageSize;
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
     * @param totalItems общее кол-во всех элементов содержащихся в исходной выборке для которой провдится пагинация.
     * @return возвращает метаданные исходной выборки и страницы.
     */
    public Page.Info createPageMetadata(int totalItems) {
        return createPageMetadata(BigInteger.valueOf(totalItems));
    }

    /**
     * Создает и возращает объект представляющий метаданные о исходной выборке и будущей страницы. Создание метаданых
     * страницы отделены от создания самой страницы (которая уже непосредственно содержит объекты из исходной
     * выборки), т.к. для получения непосредственно самих объектов для заполнения страницы могут заранее поднадобиться
     * её метаданные.
     * @param totalItems общее кол-во всех элементов содержащихся в исходной выборке для которой провдится пагинация.
     * @return возвращает метаданные исходной выборки и страницы.
     * @throws NullPointerException если totalItems равен null.
     * @throws IllegalArgumentException если totalItems меньше нуля.
     */
    public Page.Info createPageMetadata(BigInteger totalItems) {
        return new Page.Info(this, totalItems);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pageable pageable = (Pageable) o;
        return expectedMaxPageSize == pageable.expectedMaxPageSize &&
                expectedPageNumber.equals(pageable.expectedPageNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedMaxPageSize, expectedPageNumber);
    }

    @Override
    public String toString() {
        return "Pageable{" +
                "expectedPageSize=" + expectedMaxPageSize +
                ", expectedPageNumber=" + expectedPageNumber +
                '}';
    }

}

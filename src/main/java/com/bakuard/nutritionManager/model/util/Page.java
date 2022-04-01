package com.bakuard.nutritionManager.model.util;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Используется для пагинации. Представляет собой страницу - одну из равных по размеру частей выборки.<br/>
 * Объекты данного класса являются неизменяемыми.
 * @param <T> типо объектов из которых состоит исходная выборка, к котоой применяется пагинация.
 */
public final class Page<T> {

    private final ImmutableList<T> content;
    private final Metadata metadata;

    private Page(List<T> content, Metadata metadata) {
        if(content.size() != metadata.getActualSize()) {
            throw new IllegalStateException(
                    "content size must be equal page actual size. contentSize=" + content.size() +
                            ", actualSize=" + metadata.getActualSize()
            );
        }

        this.content = ImmutableList.copyOf(Objects.requireNonNull(content, "content can't be null"));
        this.metadata = metadata;
    }

    /**
     * Возвращает список всех элементов страницы доступный только для чтения.
     * @return список всех элементов страницы доступный только для чтения.
     */
    public ImmutableList<T> getContent() {
        return content;
    }

    /**
     * Возвращает метаданные страницы (подробнее см. {@link Metadata}).
     * @return метаданные страницы.
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Возвращает элемет выборки по его порядковому номеру во всей выборке (глобальному индексу), по которой
     * проводится пагинация. Если элемент с указанным глобальным индексом, на момент создания данного объекта
     * Page, должен был находится на другой странице - метод вернет null.
     * @param globalIndex глобальный индекс элемента (порядковый номер элемента во всей выборке).
     * @return объект хранящийся на данной странице или null.
     * @throws IndexOutOfBoundsException если globalIndex < 0
     */
    public T get(int globalIndex) {
        return get(BigInteger.valueOf(globalIndex));
    }

    /**
     * Возвращает элемет выборки по его порядковому номеру во всей выборке (глобальному индексу), по которой
     * проводится пагинация. Если элемент с указанным глобальным индексом, на момент создания данного объекта
     * Page, должен был находится на другой странице - метод вернет null.
     * @param globalIndex глобальный индекс элемента (порядковый номер элемента во всей выборке).
     * @return объект хранящийся на данной странице или null.
     * @throws IndexOutOfBoundsException если globalIndex < 0
     */
    public T get(BigInteger globalIndex) {
        if(globalIndex.signum() < 0)
            throw new IndexOutOfBoundsException("globalIndex can't be less then zero. Actual value = " + globalIndex);

        BigInteger offset = metadata.getOffset();
        BigInteger topLine = offset.add(BigInteger.valueOf(metadata.getActualSize()));

        T result = null;

        if(globalIndex.compareTo(offset) >= 0 && globalIndex.compareTo(topLine) < 0) {
            int index = globalIndex.subtract(offset).intValueExact();
            result = content.get(index);
        }

        return result;
    }

    /**
     * Воздает и возвращает новый объект страницы являющейся результатом преобразования всех элементов исходной
     * страницы в элементы указанного типа.
     * @param converter функция выполняющая преобразование каждого отдельного элемента.
     * @param <U> тип элементов возвращаемой страницы.
     * @return новый объект страницы.
     */
    public <U>Page<U> map(Function<T, U> converter) {
        return new Page<>(
                content.stream().map(converter).toList(),
                metadata
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return content.equals(page.content) &&
                metadata.equals(page.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, metadata);
    }

    @Override
    public String toString() {
        return "Page{" +
                "content=" + content +
                ", info=" + metadata +
                '}';
    }


    /**
     * Объекты данного класса содержат метаданные о всей исходной выборке и конкретной странице определяемой
     * {@link Pageable}.
     */
    public static class Metadata {

        private static final int minPageSize = 1;


        private final Pageable pageable;
        private final BigInteger totalItems;
        private final BigInteger commonPageSize;
        private final BigInteger maxPageNumber;
        private final int actualSize;
        private final BigInteger actualNumber;

        Metadata(Pageable pageable, BigInteger totalItems, int maxPageSize) {
            if(maxPageSize < 1)
                throw new IllegalArgumentException("maxPageSize must be greater or equal 1");

            this.pageable = pageable;

            this.totalItems = Objects.requireNonNull(totalItems, "totalItems can't be null");
            if(totalItems.signum() < 0)
                throw new IllegalArgumentException("totalItems can't be negative. totalItems = " + totalItems);

            commonPageSize = BigInteger.valueOf(
                    Math.min(Math.max(pageable.getExpectedPageSize(), minPageSize), maxPageSize)
            );

            maxPageNumber = totalItems.
                    subtract(BigInteger.ONE).
                    divide(commonPageSize).
                    max(BigInteger.ZERO);

            actualNumber = pageable.getExpectedPageNumber().
                    max(BigInteger.ZERO).
                    min(maxPageNumber);

            actualSize = totalItems.
                    subtract(getOffset()).
                    min(commonPageSize).
                    intValue();
        }

        /**
         * Возвращает объект {@link Pageable} на основе которого была получена данная страница.
         * @return объект {@link Pageable} на основе которого быда получена данная страница.
         */
        public Pageable getPageable() {
            return pageable;
        }

        /**
         * Возвращает смещение перед первым элементом страницы, которая будет получена с помощью данного объекта.
         * Смещение представляет собой кол-во элементов общей выборки (к которой применяется пагинация),
         * которое находится перед первым элементом страницы получаемой с помощью данного объекта. Если исходная
         * выборка пуста или страница является первой - метод вернет 0.
         * @return смещение перед первым элементом страницы.
         */
        public BigInteger getOffset() {
            return commonPageSize.multiply(actualNumber);
        }

        /**
         * Фактический номер страницы. Фактический номер страницы равен ожидаемому номеру страницы указанному в
         * {@link Pageable} за исключением следующих случаев:<br/>
         * 1. Если ожидаемый номер страницы указанный в {@link Pageable} меньше нуля, то фактический номер страницы
         *    будет равен нулю.<br/>
         * 3. Если искомая выборка не пуста и ожидаемый номер страницы указанный в {@link Pageable} больше или равен
         *    {@link #getTotalPages()}, то фактический номер страницы будет равен {@link #getTotalPages()} минус 1.
         * 2. Если искомая выборка пуста - то фактический номер страницы будет равен 0.<br/>
         * @return фактический номер страницы.
         */
        public BigInteger getActualNumber() {
            return actualNumber;
        }

        /**
         * Фактический размер страницы. Фактический размер страницы равен ожидаемому размеру страницы указанному в
         * {@link Pageable} за исключением следующих случаев:<br/>
         * 1. Если ожидаемый размер страницы указанный в {@link Pageable} меньше или равен нулю, то фактический размер
         *    страницы будет равен 1.<br/>
         * 2. Если искомая выборка пуста - то фактический размер страницы будет равен 0.<br/>
         * 3. Если ожидаемый размер страницы указанный в {@link Pageable} больше 200 или больше оставшегося кол-ва
         *    элементов в выборке, то фактический размер страницы будет равен наименьшему из этих двух значений.
         * @return фактический размер страницы.
         */
        public int getActualSize() {
            return actualSize;
        }

        /**
         * Возвращает общее кол-во всех элементов исходной выборки для которой выполняется пагинация.
         * @return общее кол-во всех элементов исходной выборки для которой выполняется пагинация.
         */
        public BigInteger getTotalItems() {
            return totalItems;
        }

        /**
         * Возвращает кол-во всех не пустых страниц, на которые можно разбить исходную выборку. Если общее кол-во
         * элементов в исходной выборке равно 0, то метод также вернет 0.
         * @return кол-во всех не пустых страниц.
         */
        public BigInteger getTotalPages() {
            return totalItems.signum() == 0 ? BigInteger.ZERO : maxPageNumber.add(BigInteger.ONE);
        }

        /**
         * Проверяет - является ли страница пустой.
         * @return true - если страница пустая, иначе - false.
         */
        public boolean isEmpty() {
            return actualSize == 0;
        }

        /**
         * Проверяет - является ли страница первой в выборке.
         * @return true - если страница является первой в выборке, иначе - false.
         */
        public boolean isFirst() {
            return actualNumber.signum() == 0;
        }

        /**
         * Проверяет - является ли страница последней в выборке.
         * @return true - если страница является последней в выборке, иначе - false.
         */
        public boolean isLast() {
            return actualNumber.equals(maxPageNumber);
        }

        /**
         * Создает и возвращает новый объект страницы, который содержит фактические данные из исходной выборки.
         * @param content объекты которые будет содержать создаваемый объект страницы.
         * @param <T> тип объектов из которых состоит выборка.
         * @return новый объект страницы.
         * @throws NullPointerException если  content равен null.
         * @throws IllegalArgumentException если размер передаваемого списка не равен {@link #getActualSize()}.
         */
        public <T>Page<T> createPage(List<T> content) {
            return new Page<>(content, this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metadata metadata = (Metadata) o;
            return actualSize == metadata.actualSize &&
                    totalItems.equals(metadata.totalItems) &&
                    actualNumber.equals(metadata.actualNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalItems, actualSize, actualNumber);
        }

        @Override
        public String toString() {
            return "Info{" +
                    "pageable=" + pageable +
                    ", offset=" + getOffset() +
                    ", totalItems=" + totalItems +
                    ", commonPageSize=" + commonPageSize +
                    ", maxPageNumber=" + maxPageNumber +
                    ", actualSize=" + actualSize +
                    ", actualNumber=" + actualNumber +
                    '}';
        }

    }

}

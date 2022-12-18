package com.bakuard.nutritionManager.model.util;

import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Используется для пагинации. Представляет собой страницу - одну из равных по размеру частей выборки.<br/>
 * Объекты данного класса являются неизменяемыми.
 */
public final class Page<T> {

    /**
     * Создает и возвращает пустую страницу, представляющую первую страницу пустой выборки. Номер данной страницы
     * будут равен 0, а максимальный ожидаемый размер 1.
     * @param <T> тип объектов из которых состоит выборка.
     * @return пустую страницы представляющую первую страницу пустой выборки.
     */
    public static <T> Page<T> empty() {
        return new Page.Metadata(
                BigInteger.ZERO,
                1,
                BigInteger.ZERO,
                1
        ).createPage(List.of());
    }


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
     * Возвращает элемент выборки по его порядковому номеру во всей выборке (глобальному индексу), по которой
     * проводится пагинация. Особые случаи: <br/>
     * 1. Если элемент с указанным глобальным индексом, на момент создания данного объекта Page, должен
     *    находится на другой странице - метод вернет пустой Optional. <br/>
     * 2. Если исходная выборка, по которой проводится пагинация, является пустой - вернет пустой Optional. <br/>
     * 3. Если в выборке не существует элемента с таким индексом (индекс выходит за допустимый диапазон) -
     *    вернет пустой Optional. <br/>
     * @param globalIndex глобальный индекс элемента (порядковый номер элемента во всей выборке).
     * @return объект хранящийся на данной странице или пустой Optional.
     * @throws IndexOutOfBoundsException если globalIndex < 0
     */
    public Optional<T> getByGlobalIndex(int globalIndex) {
        return getByGlobalIndex(BigInteger.valueOf(globalIndex));
    }

    /**
     * Возвращает элемент выборки по его порядковому номеру во всей выборке (глобальному индексу), по которой
     * проводится пагинация. Особые случаи: <br/>
     * 1. Если элемент с указанным глобальным индексом, на момент создания данного объекта Page, должен
     *    находится на другой странице - метод вернет пустой Optional. <br/>
     * 2. Если исходная выборка, по которой проводится пагинация, является пустой - вернет пустой Optional. <br/>
     * 3. Если в выборке не существует элемента с таким индексом (индекс выходит за допустимый диапазон) -
     *    вернет пустой Optional. <br/>
     * @param globalIndex глобальный индекс элемента (порядковый номер элемента во всей выборке).
     * @return объект хранящийся на данной странице или пустой Optional.
     * @throws NullPointerException если globalIndex является null.
     */
    public Optional<T> getByGlobalIndex(BigInteger globalIndex) {
        Objects.requireNonNull(globalIndex, "globalIndex can't be null");

        T result = null;

        if(!metadata.isEmpty()) {
            BigInteger offset = metadata.getOffset();
            BigInteger topLine = offset.add(BigInteger.valueOf(metadata.getActualSize()));

            if(globalIndex.compareTo(offset) >= 0 && globalIndex.compareTo(topLine) < 0) {
                int index = globalIndex.subtract(offset).intValueExact();
                result = content.get(index);
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * Возвращает глобальный индекс первого из элементов находящихся на данной странице и удовлетворяющего
     * заданному ограничению predicate. Если среди элементов этой страницы ни один не соответствует заданному
     * ограничению - возвращает пустой Optional. <br/>
     * Глобальный индекс элемента - это его порядковый номер (нумерация начинается с нуля) во всей выборке,
     * по которой проводится пагинация.
     * @param predicate ограничения задающие
     * @return глобальный индекс элемента удовлетворяющего заданному ограничению.
     * @throws NullPointerException если predicate равен null.
     */
    public Optional<BigInteger> getGlobalIndexFor(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate can't be null.");

        int index = 0;
        while(index < content.size() && !predicate.test(content.get(index))) ++index;

        BigInteger result = null;
        if(index < content.size()) result = metadata.getOffset().add(BigInteger.valueOf(index));

        return Optional.ofNullable(result);
    }

    /**
     * Создает и возвращает новый объект страницы являющейся результатом преобразования всех элементов исходной
     * страницы в элементы указанного типа.
     * @param mapper функция выполняющая преобразование каждого отдельного элемента.
     * @param <U> тип элементов возвращаемой страницы.
     * @return новый объект страницы.
     */
    public <U>Page<U> map(Function<T, U> mapper) {
        return new Page<>(
                content.stream().map(mapper).toList(),
                metadata
        );
    }

    /**
     * Создает и возвращает новый объект страницы являющейся результатом преобразования всех элементов исходной
     * страницы в элементы указанного типа.
     * @param mapper функция выполняющая преобразование каждого отдельного элемента. В качестве входных данных
     *               она получает объект, требующий преобразования, и его глобальный индекс в выборке по которой
     *               выполнялась пагинация.
     * @param <U> тип элементов возвращаемой страницы.
     * @return новый объект страницы.
     */
    public <U>Page<U> map(BiFunction<T, BigInteger, U> mapper) {
        BigInteger firstIndex = metadata.getOffset();

        return new Page<>(
                IntStream.range(0, content.size()).
                        mapToObj(i -> {
                            BigInteger index = firstIndex.add(BigInteger.valueOf(i));
                            T item = content.get(i);
                            return mapper.apply(item, index);
                        }).
                        toList(),
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
     * {@link PageableByNumber}.
     */
    public static class Metadata {

        private static final int minPageSize = 1;


        private final BigInteger totalItems;
        private final BigInteger offset;
        private final BigInteger maxPageNumber;
        private final int actualSize;
        private final BigInteger actualNumber;

        Metadata(BigInteger expectedPageNumber,
                 int expectedPageSize,
                 BigInteger totalItems,
                 int maxPageSize) {
            if(maxPageSize < 1)
                throw new IllegalArgumentException("maxPageSize must be greater or equal 1");

            this.totalItems = Objects.requireNonNull(totalItems, "totalItems can't be null");
            if(totalItems.signum() < 0)
                throw new IllegalArgumentException("totalItems can't be negative. totalItems = " + totalItems);

            BigInteger commonPageSize = BigInteger.valueOf(
                    Math.min(Math.max(expectedPageSize, minPageSize), maxPageSize)
            );

            maxPageNumber = totalItems.
                    subtract(BigInteger.ONE).
                    divide(commonPageSize).
                    max(BigInteger.ZERO);

            actualNumber = expectedPageNumber.
                    max(BigInteger.ZERO).
                    min(maxPageNumber);

            offset = commonPageSize.multiply(actualNumber);

            actualSize = totalItems.
                    subtract(offset).
                    min(commonPageSize).
                    intValue();
        }

        /**
         * Возвращает смещение перед первым элементом страницы, которая будет получена с помощью данного объекта.
         * Смещение представляет собой кол-во элементов общей выборки (к которой применяется пагинация),
         * находящееся перед первым элементом страницы получаемой с помощью данного объекта. Если исходная
         * выборка пуста или страница является первой - метод вернет 0.
         * @return смещение перед первым элементом страницы.
         */
        public BigInteger getOffset() {
            return offset;
        }

        /**
         * Фактический номер страницы. Фактический номер страницы равен ожидаемому номеру страницы указанному в
         * {@link PageableByNumber} за исключением следующих случаев:<br/>
         * 1. Если ожидаемый номер страницы указанный в {@link PageableByNumber} меньше нуля, то фактический номер страницы
         *    будет равен нулю.<br/>
         * 3. Если искомая выборка не пуста и ожидаемый номер страницы указанный в {@link PageableByNumber} больше или равен
         *    {@link #getTotalPages()}, то фактический номер страницы будет равен {@link #getTotalPages()} минус 1.
         * 2. Если искомая выборка пуста - то фактический номер страницы будет равен 0.<br/>
         * @return фактический номер страницы.
         */
        public BigInteger getActualNumber() {
            return actualNumber;
        }

        /**
         * Фактический размер страницы. Фактический размер страницы равен ожидаемому размеру страницы указанному в
         * {@link PageableByNumber} за исключением следующих случаев:<br/>
         * 1. Если ожидаемый размер страницы указанный в {@link PageableByNumber} меньше или равен нулю, то фактический размер
         *    страницы будет равен 1.<br/>
         * 2. Если искомая выборка пуста - то фактический размер страницы будет равен 0.<br/>
         * 3. Если ожидаемый размер страницы указанный в {@link PageableByNumber} больше 200 или больше оставшегося кол-ва
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
            return totalItems.signum() == 0;
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
         * @param content объекты, которые будет содержать создаваемый объект страницы.
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
            return "Metadata{" +
                    "offset=" + offset +
                    ", totalItems=" + totalItems +
                    ", maxPageNumber=" + maxPageNumber +
                    ", actualSize=" + actualSize +
                    ", actualNumber=" + actualNumber +
                    '}';
        }

    }

}

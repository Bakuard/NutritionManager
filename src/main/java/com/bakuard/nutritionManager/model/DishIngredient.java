package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.ProductCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductSumCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductsNumberCriteria;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.ProductSort;
import com.bakuard.nutritionManager.model.filters.SortDirection;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

/**
 * Ингредиент блюда. В качестве ингредиента можно задать не только один конкретный продукт, а множетсво
 * взаимозаменяемых продуктов, каждый из которых можно использовать в качестве данного ингредиента блюда.
 * Указанное множество продуктов упорядоченно по цене в порядке возрастания.
 */
public class DishIngredient {

    private final String name;
    private final Filter filter;
    private final BigDecimal quantity;

    private final ProductRepository repository;
    private final User user;
    private final AppConfigData config;
    private final ProductSort sort;

    /**
     * Создает новый ингредиент блюда.
     * @param name наименование ингредиента.
     * @param filter ограничение задающее множество взаимозаменяемых продуктов, каждый из которых может
     *               выступать в качестве данного ингредиента.
     * @param quantity кол-во данного ингредиента необходимого для приготовления одной порции блюда.
     * @param repository репозиторий продуктов.
     * @param user пользователь, которому принадлежит блюдо, для которого создается данный ингредиент.
     * @param config общие данные конфигурации приложения.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. Если хотя бы один из параметров имеет значение null.<br/>
     *              2. Если имя ингредиента не содержит ни одного отображаемого символа.<br/>
     *              3. Если указанное кол-во ингредиента не является положительным числом.
     */
    public DishIngredient(String name,
                          Filter filter,
                          BigDecimal quantity,
                          ProductRepository repository,
                          User user,
                          AppConfigData config) {
        Checker.of(getClass(), "constructor").
                nullValue("name", name).
                nullValue("filter", filter).
                nullValue("quantity", quantity).
                nullValue("repository", repository).
                nullValue("user", user).
                nullValue("config", config).
                notPositiveValue("quantity", quantity).
                blankValue("name", name).
                checkWithValidateException("Fail to create dish ingredient");

        this.name = name;
        this.filter = filter;
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.repository = repository;
        this.user = user;
        this.config = config;

        sort = new ProductSort(ProductSort.Parameter.PRICE, SortDirection.ASCENDING);
    }

    /**
     * Возвращает наименование данного ингредиента.
     * @return наименование данного ингредиента.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает фильтр продуктов.
     * @return фильтр продуктов.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Возвращает кол-во данного ингредиента необходимого для приготовления указанного кол-ва порций блюда.
     * @param servingNumber кол-во порций блюда.
     * @return кол-во данного ингредиента необходимого для приготовления указанного кол-ва порций блюда.
     * @throws ValidateException если указанное значение null или не является положительным.
     */
    public BigDecimal getNecessaryQuantity(BigDecimal servingNumber) {
        Checker.of(getClass(), "getNecessaryQuantity").
                nullValue("servingNumber", servingNumber).
                notPositiveValue("servingNumber", servingNumber).
                checkWithValidateException("Fail to get necessary ingredient quantity.");

        return quantity.multiply(servingNumber);
    }

    /**
     * Для любого указанного продукта, из множества взаимозаменямых продуктов данного ингредиента, вычисляет
     * и возвращает - в каком кол-ве его необходимо докупить (именно "докупить", а не "купить", т.к. искомый
     * продукт может уже иметься в некотором кол-ве у пользователя) для блюда в указанном кол-ве порций.
     * Если указанный productIndex больше или равен кол-ву всех продуктов соответствующих данному ингрдиенту,
     * то метод вернет результат для последнего продукта.<br/>
     * Если в БД нет ни одного продукта удовлетворяющего ограничению {@link #getFilter()} данного ингредиента,
     * то метод вернет пустой Optional.
     * @param productIndex порядковый номер продукта из множества взаимозаменяемых продуктов данного ингредиента.
     * @param servingNumber кол-во порций блюда.
     * @return кол-во "упаковок" докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. servingNumber является null.<br/>
     *              2. productIndex < 0 <br/>
     *              3. servingNumber <= 0
     */
    public Optional<BigDecimal> getLackQuantity(int productIndex, BigDecimal servingNumber) {
        Checker.of(getClass(), "getLackQuantity").
                nullValue("servingNumber", servingNumber).
                negativeValue("productIndex", productIndex).
                notPositiveValue("servingNumber", servingNumber).
                checkWithValidateException("Fail to get product lack quantity");

        return getProductByIndex(productIndex).
                map(product -> {
                    BigDecimal lackQuantity = getNecessaryQuantity(servingNumber).
                            subtract(product.getQuantity()).
                            max(BigDecimal.ZERO);

                    if(lackQuantity.signum() > 0) {
                        lackQuantity = lackQuantity.
                                divide(product.getContext().getPackingSize(), config.getMathContext()).
                                setScale(0, RoundingMode.UP);
                    }

                    return lackQuantity;
                });
    }

    /**
     * Возвращает общую цену за недостающее кол-во "упаковок" докупаемого продукта.<br/>
     * Если в БД нет ни одного продукта удовлетворяющего ограничению {@link #getFilter()} данного ингредиента,
     * то метод вернет пустой Optional.
     * @param productIndex порядковый номер продукта из множества взаимозаменяемых продуктов данного ингредиента.
     * @param servingNumber кол-во порций блюда.
     * @return общую цену за недостающее кол-во докупаемого продукта.
     */
    public Optional<BigDecimal> getLackQuantityPrice(int productIndex, BigDecimal servingNumber) {
        return getProductByIndex(productIndex).
                map(product ->
                    getLackQuantity(productIndex, servingNumber).get().
                            multiply(product.getContext().getPrice(), config.getMathContext())
                );
    }

    /**
     * Возвращает продукт из множества всех взаимозаменяемых продуктов для данноо ингредиента по его
     * порядковому номеру (т.е. индексу. Индексация начинается с нуля). Множество продуктов для данного
     * ингредиента упорядоченно по цене.<br/>
     * Если в БД нет ни одного продукта удовлетворяющего ограничению {@link #getFilter()} данного ингредиента,
     * то метод вернет пустой Optional.
     * @param productIndex порядковому номер продукта.
     * @return продукт по его порядковому номеру.
     */
    public Optional<Product> getProductByIndex(int productIndex) {
        Product product = repository.getProducts(
                ProductCriteria.of(Pageable.ofIndex(5, productIndex), user).
                        setProductSort(sort)
        ).get(productIndex);

        return Optional.ofNullable(product);
    }

    /**
     * Возвращает кол-во всех продуктов которые могут использоваться в качестве данноо ингредиента.
     * @return кол-во всех продуктов которые могут использоваться в качестве данноо ингредиента.
     */
    public int getProductsNumber() {
        return repository.getProductsNumber(
                ProductsNumberCriteria.of(user).setFilter(filter)
        );
    }

    /**
     * Возвращает суммарную цену всех продуктов которые могут использоваться в качестве данного ингредиента.
     * Используется при расчете средней арифметической цены блюда, в которое входит данный ингредиент.<br/>
     * Если в БД нет ни одного продукта удовлетворяющего ограничению {@link #getFilter()} данного ингредиента,
     * то метод вернет пустой Optional.
     * @return суммарную цену всех продуктов которые могут использоваться в качестве данноо ингредиента.
     */
    public Optional<BigDecimal> getProductsPriceSum() {
        return repository.getProductsSum(ProductSumCriteria.of(user, filter));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return name.equals(that.name) &&
                filter.equals(that.filter) &&
                quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filter, quantity);
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "name='" + name + '\'' +
                ", filter=" + filter +
                ", quantity=" + quantity +
                '}';
    }


    /**
     * Реализация паттерна "Builder" для ингредиентов блюд ({@link DishIngredient}).
     */
    public static class Builder implements AbstractBuilder<DishIngredient> {

        private String name;
        private Filter filter;
        private BigDecimal quantity;
        private ProductRepository repository;
        private User user;
        private AppConfigData config;

        public Builder() {

        }

        /**
         * Устанавливает наименование для создаваемого ингредиента.
         * @param name наименование для создаваемого ингредиента.
         * @return этот же объект.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Устанавливает фильтр - ограничение задающее множество взаимозаменяемых продуктов, каждый из
         * которых можно использовать в качестве создаваемого ингредиента.
         * @param filter ограничение задающее множество взаимозаменяемых продуктов
         * @return этот же объект.
         */
        public Builder setFilter(Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Задает кол-во создаваемого ингредиента необходимого для приготовления одной порции блюда.
         * @param quantity кол-во создаваемого ингредиента необходимого для приготовления одной порции блюда.
         * @return этот же объект.
         */
        public Builder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * Задает ссылку на репозиторий продуктов.
         * @param repository репозиторий продуктов.
         * @return этот же объект.
         */
        public Builder setRepository(ProductRepository repository) {
            this.repository = repository;
            return this;
        }

        /**
         * Устаналвивает ссылку на пользователя - владельца блюда для котороо создается данный ингредиент.
         * @param user владелец блюда для котороо создается данный ингредиент.
         * @return этот же объект.
         */
        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        /**
         * Устанавливает конфигурациооные данные всего приложения.
         * @param config конфигурациооные данные всего приложения.
         * @return этот же объект.
         */
        public Builder setConfig(AppConfigData config) {
            this.config = config;
            return this;
        }

        @Override
        public DishIngredient tryBuild() throws ValidateException {
            return new DishIngredient(
                    name,
                    filter,
                    quantity,
                    repository,
                    user,
                    config
            );
        }

    }

}

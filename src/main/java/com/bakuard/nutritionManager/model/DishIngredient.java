package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Ингредиент блюда. В качестве ингредиента можно задать не только один конкретный продукт, а множетсво
 * взаимозаменяемых продуктов, каждый из которых можно использовать в качестве данного ингредиента блюда.
 * Указанное множество продуктов упорядоченно по цене в порядке возрастания.
 */
public class DishIngredient {

    private final String name;
    private final Filter filter;
    private final BigDecimal quantity;
    private final AppConfigData config;

    /**
     * Конструктор копирования. Выполняет глубокое копирование.
     * @param other копируемый ингредиент.
     */
    public DishIngredient(DishIngredient other) {
        this.name = other.name;
        this.filter = other.filter;
        this.quantity = other.quantity;
        this.config = other.config;
    }

    /**
     * Создает новый ингредиент блюда.
     * @param name наименование ингредиента.
     * @param filter ограничение задающее множество взаимозаменяемых продуктов, каждый из которых может
     *               выступать в качестве данного ингредиента.
     * @param quantity кол-во данного ингредиента необходимого для приготовления одной порции блюда.
     * @param config общие конфигурационные данные приложения
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. Если хотя бы один из параметров имеет значение null.<br/>
     *              2. Если имя ингредиента не содержит ни одного отображаемого символа.<br/>
     *              3. Если указанное кол-во ингредиента не является положительным числом.
     */
    public DishIngredient(String name,
                          Filter filter,
                          BigDecimal quantity,
                          AppConfigData config) {
        Validator.check(
                Rule.of("DishIngredient.name").notNull(name).and(v -> v.notBlank(name)),
                Rule.of("DishIngredient.filter").notNull(filter),
                Rule.of("DishIngredient.quantity").notNull(quantity).and(v -> v.positiveValue(quantity)),
                Rule.of("DishIngredient.config").notNull(config)
        );

        this.name = name;
        this.filter = filter;
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.config = config;
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
        Validator.check(
                Rule.of("DishIngredient.servingNumber").notNull(servingNumber).
                        and(v -> v.positiveValue(servingNumber))
        );

        return quantity.multiply(servingNumber, config.getMathContext());
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
         * Устанавливает конфигурациооные данные всего приложения.
         * @param config конфигурациооные данные всего приложения.
         * @return этот же объект.
         */
        public Builder setConfig(AppConfigData config) {
            this.config = config;
            return this;
        }

        /**
         * Проверяет - имеют ли все соответсвующие поля данного объекта указанные значения.
         * @param name наименование ингредиента.
         * @param filter ограничение задающее множество взаимозаменяемых продуктов.
         * @param quantity кол-во создаваемого ингредиента необходимого для приготовления одной порции блюда.
         * @return return - если описанное условие выполняется, иначе - false.
         */
        public boolean contains(String name, Filter filter, BigDecimal quantity) {
            return Objects.equals(name, this.name) &&
                    Objects.equals(filter, this.filter) &&
                    Objects.equals(quantity, this.quantity);
        }

        @Override
        public DishIngredient tryBuild() throws ValidateException {
            return new DishIngredient(
                    name,
                    filter,
                    quantity,
                    config
            );
        }

    }

}

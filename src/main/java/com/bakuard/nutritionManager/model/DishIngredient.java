package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.*;

/**
 * Ингредиент блюда. В качестве ингредиента можно задать не только один конкретный продукт, а множество
 * взаимозаменяемых продуктов, каждый из которых можно использовать в качестве данного ингредиента блюда.
 * Указанное множество продуктов упорядоченно по цене в порядке возрастания.
 */
public class DishIngredient implements Entity<DishIngredient> {

    private final UUID id;
    private final String name;
    private final Filter filter;
    private final BigDecimal quantity;
    private final ConfigData config;

    /**
     * Конструктор копирования. Выполняет глубокое копирование.
     * @param other копируемый ингредиент.
     */
    public DishIngredient(DishIngredient other) {
        this.id = other.id;
        this.name = other.name;
        this.filter = other.filter;
        this.quantity = other.quantity;
        this.config = other.config;
    }

    /**
     * Создает новый ингредиент блюда.
     * @param id уникальный идентификатор ингредиента блюда.
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
    public DishIngredient(UUID id,
                          String name,
                          Filter filter,
                          BigDecimal quantity,
                          ConfigData config) {
        Validator.check(
                "DishIngredient.id", notNull(id),
                "DishIngredient.name", notNull(name).and(() -> notBlank(name)),
                "DishIngredient.filter", notNull(filter),
                "DishIngredient.quantity", notNull(quantity).and(() -> positiveValue(quantity)),
                "DishIngredient.config", notNull(config)
        );

        this.id = id;
        this.name = name;
        this.filter = filter;
        this.quantity = quantity.setScale(config.decimal().numberScale(), config.decimal().roundingMode());
        this.config = config;
    }

    /**
     * Возвращает уникальный идентификатор ингредиента блюда.
     * @return уникальный идентификатор ингредиента блюда.
     */
    @Override
    public UUID getId() {
        return id;
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
                "DishIngredient.servingNumber",
                notNull(servingNumber).and(() -> positiveValue(servingNumber))
        );

        return quantity.multiply(servingNumber, config.decimal().mathContext());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredient that = (DishIngredient) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", filter=" + filter +
                ", quantity=" + quantity +
                '}';
    }


    /**
     * Реализация паттерна "Builder" для ингредиентов блюд ({@link DishIngredient}).
     */
    public static class Builder implements AbstractBuilder<DishIngredient> {

        private UUID id;
        private String name;
        private Filter filter;
        private BigDecimal quantity;
        private ConfigData config;

        public Builder() {

        }

        /**
         * Генерирует и устанавливает уникальный идентификатор для создаваемого ингредиента.
         * @return этот же объект.
         */
        public Builder generateId() {
            id = UUID.randomUUID();
            return this;
        }

        /**
         * Устанавливает уникальный идентификатор для создаваемого ингредиента.
         * @param id уникальный идентификатор для создаваемого ингредиента.
         * @return этот же объект.
         */
        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        /**
         * Устанавливает уникальный идентификатор для создаваемого ингредиента. Если аргумент имеет значение
         * null - генерирует, а затем устанавливает уникальный идентификатор для создаваемого ингредиента.
         * @param id уникальный идентификатор для создаваемого ингредиента.
         * @return этот же объект.
         */
        public Builder setOrGenerateId(UUID id) {
             this.id = id == null ? UUID.randomUUID() : id;
             return this;
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
         * Устанавливает конфигурационные данные всего приложения.
         * @param config конфигурационные данные всего приложения.
         * @return этот же объект.
         */
        public Builder setConfig(ConfigData config) {
            this.config = config;
            return this;
        }

        /**
         * Проверяет - имеют ли все соответствующие поля данного объекта указанные значения.
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
                    id,
                    name,
                    filter,
                    quantity,
                    config
            );
        }

    }

}

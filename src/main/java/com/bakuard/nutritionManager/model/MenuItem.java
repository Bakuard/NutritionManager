package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.notNull;
import static com.bakuard.nutritionManager.validation.Rule.positiveValue;

/**
 * Элемент меню. Представляет собой конкретное блюдо и кол-во, в котором это блюдо входит в меню.
 */
public class MenuItem implements Entity<MenuItem> {

    private final UUID id;
    private final Dish dish;
    private final BigDecimal quantity;
    private final ConfigData config;

    public MenuItem(MenuItem other) {
        id = other.id;
        dish = other.dish;
        quantity = other.quantity;
        config = other.config;
    }

    private MenuItem(UUID id,
                     Dish dish,
                     BigDecimal quantity,
                     ConfigData config) {
        Validator.check(
                "MenuItem.id", notNull(id),
                "MenuItem.dish", notNull(dish),
                "MenuItem.quantity", notNull(quantity).and(() -> positiveValue(quantity)),
                "MenuItem.config", notNull(config)
        );

        this.id = id;
        this.dish = dish;
        this.quantity = quantity.setScale(config.decimal().numberScale(), config.decimal().roundingMode());
        this.config = config;
    }

    private MenuItem(UUID id,
                     String dishName,
                     BigDecimal quantity,
                     ConfigData config,
                     DishRepository repository,
                     UUID userId) {
        Validator.check(
                "MenuItem.id", notNull(id),
                "MenuItem.dishName", notNull(dishName),
                "MenuItem.quantity", notNull(quantity).and(() -> positiveValue(quantity)),
                "MenuItem.config", notNull(config),
                "MenuItem.repository", notNull(repository),
                "MenuItem.userId", notNull(userId)
        );

        this.id = id;
        this.quantity = quantity.setScale(config.decimal().numberScale(), config.decimal().roundingMode());
        this.config = config;
        this.dish = repository.tryGetByName(userId, dishName);
    }

    /**
     * Возвращает уникальный идентификатор элемента меню.
     * @return уникальный идентификатор элемента меню.
     */
    @Override
    public UUID getId() {
        return id;
    }

    public String getDishName() {
        return dish.getName();
    }

    public Dish getDish() {
        return dish;
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menuNumber) {
        return quantity.multiply(menuNumber, config.decimal().mathContext());
    }

    public ConfigData getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem other = (MenuItem) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", dish={id=" + dish.getId() + ", name=" + dish.getName() + '}' +
                ", quantity=" + quantity +
                '}';
    }


    public static class LoadBuilder implements AbstractBuilder<MenuItem> {

        private UUID id;
        private Dish dish;
        private BigDecimal quantity;
        private ConfigData config;

        public LoadBuilder() {

        }

        public LoadBuilder generateId() {
            id = UUID.randomUUID();
            return this;
        }

        public LoadBuilder setId(UUID id) {
            this.id = id;
            return this;
        }

        public LoadBuilder setDish(Dish dish) {
            this.dish = dish;
            return this;
        }

        public LoadBuilder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public LoadBuilder setConfig(ConfigData config) {
            this.config = config;
            return this;
        }

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(id, dish, quantity, config);
        }

    }


    public static class Builder implements AbstractBuilder<MenuItem> {

        private UUID id;
        private String dishName;
        private BigDecimal quantity;
        private ConfigData config;
        private DishRepository repository;
        private UUID userId;

        public Builder() {

        }

        public Builder generateId() {
            id = UUID.randomUUID();
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setOrGenerateId(UUID id) {
            this.id = id == null ? UUID.randomUUID() : id;
            return this;
        }

        public Builder setDishName(String dishName) {
            this.dishName = dishName;
            return this;
        }

        public Builder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setConfig(ConfigData config) {
            this.config = config;
            return this;
        }

        public Builder setRepository(DishRepository repository) {
            this.repository = repository;
            return this;
        }

        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(id, dishName, quantity, config, repository, userId);
        }

    }

}

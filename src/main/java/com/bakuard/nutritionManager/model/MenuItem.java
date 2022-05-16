package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MenuItem {

    private final Dish dish;
    private final BigDecimal quantity;
    private final AppConfigData config;

    public MenuItem(MenuItem other) {
        dish = other.dish;
        quantity = other.quantity;
        config = other.config;
    }

    private MenuItem(Dish dish,
                     BigDecimal quantity,
                     AppConfigData config) {
        Validator.check(
                Rule.of("MenuItem.dish").notNull(dish),
                Rule.of("MenuItem.quantity").notNull(quantity).and(r -> r.positiveValue(quantity)),
                Rule.of("MenuItem.config").notNull(config)
        );

        this.dish = dish;
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.config = config;
    }

    private MenuItem(String dishName,
                     BigDecimal quantity,
                     AppConfigData config,
                     DishRepository repository,
                     UUID userId) {
        Validator.check(
                Rule.of("MenuItem.dishName").notNull(dishName),
                Rule.of("MenuItem.quantity").notNull(quantity).and(r -> r.positiveValue(quantity)),
                Rule.of("MenuItem.config").notNull(config),
                Rule.of("MenuItem.repository").notNull(repository),
                Rule.of("MenuItem.userId").notNull(userId)
        );

        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.config = config;
        this.dish = repository.tryGetByName(userId, dishName);
    }

    public String getDishName() {
        return dish.getName();
    }

    public Dish getDish() {
        return dish;
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menuNumber) {
        return quantity.multiply(menuNumber, config.getMathContext());
    }

    public AppConfigData getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return dish.equals(menuItem.dish) &&
                quantity.equals(menuItem.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dish, quantity);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "dish={id=" + dish.getId() + ", name=" + dish.getName() + '}' +
                ", quantity=" + quantity +
                '}';
    }


    public static class LoadBuilder implements AbstractBuilder<MenuItem> {

        private Dish dish;
        private BigDecimal quantity;
        private AppConfigData config;

        public LoadBuilder() {

        }

        public LoadBuilder setDish(Dish dish) {
            this.dish = dish;
            return this;
        }

        public LoadBuilder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public LoadBuilder setConfig(AppConfigData config) {
            this.config = config;
            return this;
        }

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(dish, quantity, config);
        }

    }


    public static class Builder implements AbstractBuilder<MenuItem> {

        private String dishName;
        private BigDecimal quantity;
        private AppConfigData config;
        private DishRepository repository;
        private UUID userId;

        public Builder() {

        }

        public Builder setDishName(String dishName) {
            this.dishName = dishName;
            return this;
        }

        public Builder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setConfig(AppConfigData config) {
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
            return new MenuItem(dishName, quantity, config, repository, userId);
        }

    }

}

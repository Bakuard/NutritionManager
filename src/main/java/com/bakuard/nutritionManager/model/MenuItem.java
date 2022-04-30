package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;

public class MenuItem {

    private final String dishName;
    private final Supplier<Dish> dish;
    private final BigDecimal quantity;
    private final AppConfigData config;

    public MenuItem(MenuItem other) {
        dishName = other.dishName;
        dish = other.dish;
        quantity = other.quantity;
        config = other.config;
    }

    private MenuItem(String dishName,
                     Supplier<Dish> dish,
                     BigDecimal quantity,
                     AppConfigData config) {
        Validator.check(
                Rule.of("MenuItem.dishName").notNull(dishName).and(r -> r.notBlank(dishName)),
                Rule.of("MenuItem.dish").notNull(dish),
                Rule.of("MenuItem.quantity").notNull(quantity).and(r -> r.positiveValue(quantity)),
                Rule.of("MenuItem.config").notNull(config)
        );

        this.dishName = dishName;
        this.dish = dish;
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.config = config;
    }

    public String getDishName() {
        return dishName;
    }

    public Dish getDish() {
        return dish.get();
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menuNumber) {
        return quantity.multiply(menuNumber, config.getMathContext());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return dishName.equals(menuItem.dishName) &&
                getDish().equals(menuItem.getDish()) &&
                quantity.equals(menuItem.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishName, getDish(), quantity);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "dishName='" + dishName + '\'' +
                ", dishId=" + getDish().getId() +
                ", quantity=" + quantity +
                '}';
    }


    public static class Builder implements AbstractBuilder<MenuItem> {

        private String dishName;
        private Supplier<Dish> dish;
        private BigDecimal quantity;
        private AppConfigData config;

        public Builder() {

        }

        public Builder setDishName(String dishName) {
            this.dishName = dishName;
            return this;
        }

        public Builder setDish(Supplier<Dish> dish) {
            this.dish = dish;
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

        public boolean containsDish(String dishName) {
            return dishName.equals(this.dishName);
        }

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(dishName, dish, quantity, config);
        }

    }

}

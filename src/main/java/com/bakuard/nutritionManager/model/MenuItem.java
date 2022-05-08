package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.Objects;

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


    public static class Builder implements AbstractBuilder<MenuItem> {

        private Dish dish;
        private BigDecimal quantity;
        private AppConfigData config;

        public Builder() {

        }

        public Builder setDish(Dish dish) {
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

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(dish, quantity, config);
        }

    }

}

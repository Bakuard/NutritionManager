package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.function.Supplier;

public class MenuItem {

    private final String dishName;
    private final Supplier<Dish> dish;
    private final BigDecimal quantity;

    public MenuItem(MenuItem other) {
        dishName = other.dishName;
        dish = other.dish;
        quantity = other.quantity;
    }

    private MenuItem(String dishName,
                     Supplier<Dish> dish,
                     BigDecimal quantity) {
        Validator.check(
                Rule.of("MenuItem.dishName").notNull(dishName).and(r -> r.notBlank(dishName)),
                Rule.of("MenuItem.dish").notNull(dish),
                Rule.of("MenuItem.quantity").notNull(quantity).and(r -> r.positiveValue(quantity))
        );

        this.dishName = dishName;
        this.dish = dish;
        this.quantity = quantity;
    }

    public String getDishName() {
        return dishName;
    }

    public Dish getDish() {
        return dish.get();
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menuNumber) {
        return quantity.multiply(menuNumber);
    }


    public static class Builder implements Entity.Builder<MenuItem> {

        private String dishName;
        private Supplier<Dish> dish;
        private BigDecimal quantity;

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

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(dishName, dish, quantity);
        }

    }

}

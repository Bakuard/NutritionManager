package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.util.List;

public class MenuItem {

    private final String dishName;
    private final BigDecimal quantity;
    private final AppConfigData config;

    public MenuItem(MenuItem other) {
        dishName = other.dishName;
        quantity = other.quantity;
        config = other.config;
    }

    public MenuItem(String dishName,
                    BigDecimal quantity,
                    AppConfigData config) {
        Validator.check(
                Rule.of("MenuItem.dishName").notNull(dishName).and(r -> r.notBlank(dishName)),
                Rule.of("MenuItem.quantity").notNull(quantity).and(r -> r.positiveValue(quantity)),
                Rule.of("MenuItem.config").notNull(config)
        );

        this.dishName = dishName;
        this.quantity = quantity;
        this.config = config;
    }

    public String getDishName() {
        return dishName;
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menuNumber) {
        return quantity.multiply(menuNumber, config.getMathContext());
    }


    public static class Builder implements Entity.Builder<MenuItem> {

        private String dishName;
        private BigDecimal quantity;
        private AppConfigData config;

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

        @Override
        public MenuItem tryBuild() throws ValidateException {
            return new MenuItem(dishName, quantity, config);
        }

    }

}

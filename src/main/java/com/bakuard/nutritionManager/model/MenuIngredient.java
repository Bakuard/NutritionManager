package com.bakuard.nutritionManager.model;

import java.math.BigDecimal;
import java.util.List;

public class MenuIngredient {

    private final Product product;
    private final BigDecimal quantity;
    private final List<Dish> dishes;

    MenuIngredient(Product product, BigDecimal quantity, List<Dish> dishes) {
        this.product = product;
        this.quantity = quantity;
        this.dishes = dishes;
    }

    public Product getProduct() {
        return null;
    }

    public BigDecimal getNecessaryQuantity(BigDecimal menusNumber) {
        return quantity.multiply(menusNumber);
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public BigDecimal getActualQuantity(BigDecimal menusNumber) {
        return null;
    }

    public BigDecimal getPrice(BigDecimal menusNumber) {
        return null;
    }

}

package com.bakuard.nutritionManager.dto.menus;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MenuIngredientRequest {

    private UUID dishId;
    private BigDecimal quantity;

    public MenuIngredientRequest() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuIngredientRequest that = (MenuIngredientRequest) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, quantity);
    }

    @Override
    public String toString() {
        return "MenuIngredientRequest{" +
                "dishId=" + dishId +
                ", quantity=" + quantity +
                '}';
    }

}

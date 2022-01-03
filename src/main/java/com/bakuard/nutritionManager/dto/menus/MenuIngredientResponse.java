package com.bakuard.nutritionManager.dto.menus;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MenuIngredientResponse {

    private UUID dishId;
    private String dishName;
    private BigDecimal quantity;

    public MenuIngredientResponse() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
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
        MenuIngredientResponse that = (MenuIngredientResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, dishName, quantity);
    }

    @Override
    public String toString() {
        return "MenuIngredientResponse{" +
                "dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                ", quantity=" + quantity +
                '}';
    }

}

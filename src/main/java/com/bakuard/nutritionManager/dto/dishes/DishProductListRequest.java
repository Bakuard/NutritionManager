package com.bakuard.nutritionManager.dto.dishes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DishProductListRequest {

    private UUID dishId;
    private BigDecimal servingNumber;
    private Map<String, Integer> ingredients;

    public DishProductListRequest() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public void setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<String, Integer> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductListRequest that = (DishProductListRequest) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, servingNumber, ingredients);
    }

    @Override
    public String toString() {
        return "DishProductListRequest{" +
                "dishId=" + dishId +
                ", servingNumber=" + servingNumber +
                ", ingredients=" + ingredients +
                '}';
    }

}

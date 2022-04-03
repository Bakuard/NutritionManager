package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Данные ингредиента блюда")
public class DishIngredientRequestResponse {

    @Schema(description = "Кол-во данного ингредиента необходимого для приготовления одной порции блюда")
    private BigDecimal quantity;
    @Schema(description = "Фильтр задающий множество взаимозаменяемых продуктов для данного ингредиента")
    private DishIngredientFilterRequestResponse filter;

    public DishIngredientRequestResponse() {

    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public DishIngredientFilterRequestResponse getFilter() {
        return filter;
    }

    public void setFilter(DishIngredientFilterRequestResponse filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredientRequestResponse that = (DishIngredientRequestResponse) o;
        return Objects.equals(quantity, that.quantity) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, filter);
    }

    @Override
    public String toString() {
        return "DishIngredientRequest{" +
                "quantity=" + quantity +
                ", filter=" + filter +
                '}';
    }

}

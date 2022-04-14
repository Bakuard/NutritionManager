package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Данные ингредиента блюда")
public class DishIngredientRequestResponse {

    @Schema(description = "Порядковый номер ингредиента в списке ингредиентов блюда.")
    private int index;
    @Schema(description = "Кол-во данного ингредиента необходимого для приготовления одной порции блюда. Не может быть null.")
    private BigDecimal quantity;
    @Schema(description = "Фильтр задающий множество взаимозаменяемых продуктов для данного ингредиента. Не может быть null.")
    private DishIngredientFilterRequestResponse filter;

    public DishIngredientRequestResponse() {

    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
        return index == that.index &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, quantity, filter);
    }

    @Override
    public String toString() {
        return "DishIngredientRequestResponse{" +
                "index=" + index +
                ", quantity=" + quantity +
                ", filter=" + filter +
                '}';
    }

}
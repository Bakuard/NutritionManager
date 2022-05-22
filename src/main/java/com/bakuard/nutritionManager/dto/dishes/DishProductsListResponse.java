package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Возвращаемый список продуктов кажддого ингредиента блюда.")
public class DishProductsListResponse {

    @Schema(description = "Уникальный идентфикатор блюда.")
    private UUID dishId;
    @Schema(description = "Наименование блюда")
    private String dishName;
    @Schema(description = "Кол-во порций блюда на которое рассчитывается список докупаемых продуктов.")
    private BigDecimal servingNumber;
    @Schema(description = """
            Каждый элемент списка - перечень всех продуктов для конкретного ингредиента блюда. Особые случаи: <br/>
            1. Если ни одному ингредиенту блюда не соответствует ни один продукт - возвращает пустой список. <br/>
            """)
    private List<DishIngredientForListResponse> categories;

    public DishProductsListResponse() {

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

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public void setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
    }

    public List<DishIngredientForListResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<DishIngredientForListResponse> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListResponse that = (DishProductsListResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(categories, that.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, dishName, servingNumber, categories);
    }

    @Override
    public String toString() {
        return "DishProductsListResponse{" +
                "dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                ", servingNumber=" + servingNumber +
                ", categories=" + categories +
                '}';
    }

}

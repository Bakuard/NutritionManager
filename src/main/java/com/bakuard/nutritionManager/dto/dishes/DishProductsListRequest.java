package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на получение списка докупаемых продуктов для блюда")
public class DishProductsListRequest {

    @Schema(description = "Уникальный идентфикатор блюда")
    private UUID dishId;
    @Schema(description = "Кол-во порций блюда на которое рассчитывается список докупаемых продуктов")
    private BigDecimal servingNumber;
    @Schema(description = """
            Для каждого ингредиента блюда выбирается один из соответствующих ему продуктов для составления
             списка докупаемых продуктов. Задаваемый здесь ассоциатвный массив указывает - для какого
             ингредиента какой продукт отображать. Ключом в ассоциатвном массиве является порядковый номер
             ингредиента (начинается с 0), а значением порядковый номер одного из продуктов соответствующего
             ингредиента (начинается с 0).
            """)
    private Map<Integer, Integer> ingredients;

    public DishProductsListRequest() {

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

    public Map<Integer, Integer> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<Integer, Integer> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListRequest that = (DishProductsListRequest) o;
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

package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.products.ProductAsDishIngredientResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Возвращаемый список продуктов блюда")
public class DishProductsListResponse {

    @Schema(description = "Уникальный идентфикатор блюда")
    private UUID dishId;
    @Schema(description = "Кол-во порций блюда на которое рассчитывается список докупаемых продуктов")
    private BigDecimal servingNumber;
    @Schema(description = """
            Общая стоимость всех докупаемых продуктов с учетом кол-ва порций блюда. Если блюдо не содержит ни
             одного ингредиента или всем ингредиентам блюда не соответствует ни одни продукт - принимает значение
             null.
            """)
    private BigDecimal totalPrice;
    @Schema(description = """
            Список докупаемых продуктов блюда. Индекс каждого продукта в этом списке соответствует
             ингредиенту блюда с таким же индексом. Если в запросе на получение списка докупаемых
             продуктов не запрашивался продукт для некоторого ингредиента или этому ингредиенту не
             соответствует ни один продукт - то в соответствующей позиции данного списка будет null.
            """)
    private List<ProductAsDishIngredientResponse> ingredients;

    public DishProductsListResponse() {

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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<ProductAsDishIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<ProductAsDishIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListResponse that = (DishProductsListResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, servingNumber, totalPrice, ingredients);
    }

    @Override
    public String toString() {
        return "DishProductsListResponse{" +
                "dishId=" + dishId +
                ", servingNumber=" + servingNumber +
                ", totalPrice=" + totalPrice +
                ", ingredients=" + ingredients +
                '}';
    }

}

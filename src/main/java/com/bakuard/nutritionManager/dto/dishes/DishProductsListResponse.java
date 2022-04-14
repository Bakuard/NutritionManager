package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.model.util.Page;

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
             одного ингредиента или всем ингредиентам блюда не соответствует ни один продукт - принимает значение
             null.
            """)
    private BigDecimal totalPrice;
    @Schema(description = """
            Каждый элемент этого списка представляет собой все возможные докупаемые продукты для ингредиента блюда,
             индекс которого равен порядковому номеру соответствующего элемента в списке.
            """)
    private List<Page<ProductAsDishIngredientResponse>> products;

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

    public List<Page<ProductAsDishIngredientResponse>> getProducts() {
        return products;
    }

    public void setProducts(List<Page<ProductAsDishIngredientResponse>> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListResponse that = (DishProductsListResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, servingNumber, totalPrice, products);
    }

    @Override
    public String toString() {
        return "DishProductsListResponse{" +
                "dishId=" + dishId +
                ", servingNumber=" + servingNumber +
                ", totalPrice=" + totalPrice +
                ", ingredients=" + products +
                '}';
    }

}

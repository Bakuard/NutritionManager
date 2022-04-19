package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Содержит данные о кол-ве указанного продукта необходимом для приготовления указнного блюда
         входящего в состав некоторого меню (с учетом кол-ва порций этого блюда).
        """)
public class DishOfMenuProductListItemResponse {

    @Schema(description = "Уникальный идентификатор блюда.")
    private UUID dishId;
    @Schema(description = "Наименование блюда.")
    private String dishName;
    @Schema(description = "Уникальный идентификатор продукта в формате UUID")
    private UUID productId;
    @Schema(description = """
            Необходимое кол-во указанного продукта для приготовления указанного блюда
             (с учетом кол-ва порций этого блюда).
            """)
    private BigDecimal necessaryProductQuantity;

    public DishOfMenuProductListItemResponse() {

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

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getNecessaryProductQuantity() {
        return necessaryProductQuantity;
    }

    public void setNecessaryProductQuantity(BigDecimal necessaryProductQuantity) {
        this.necessaryProductQuantity = necessaryProductQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishOfMenuProductListItemResponse that = (DishOfMenuProductListItemResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(necessaryProductQuantity, that.necessaryProductQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, dishName, productId, necessaryProductQuantity);
    }

    @Override
    public String toString() {
        return "DishOfMenuProductListItemResponse{" +
                "dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                ", productId=" + productId +
                ", necessaryProductQuantity=" + necessaryProductQuantity +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Краткие данные о блюде входящем в состав некоторого меню.
        """)
public class DishOfMenuProductListResponse {

    @Schema(description = "Уникальный идентификатор блюда.")
    private UUID dishId;
    @Schema(description = "Наименование блюда.")
    private String dishName;
    @Schema(description = "Общее кол-во порций данного блюда (с учетом кол-ва меню, в которое входит данное блюдо).")
    private BigDecimal servingNumber;

    public DishOfMenuProductListResponse() {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishOfMenuProductListResponse that = (DishOfMenuProductListResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(servingNumber, that.servingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, dishName, servingNumber);
    }

    @Override
    public String toString() {
        return "DishOfMenuProductListResponse{" +
                "dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                ", servingNumber=" + servingNumber +
                '}';
    }

}

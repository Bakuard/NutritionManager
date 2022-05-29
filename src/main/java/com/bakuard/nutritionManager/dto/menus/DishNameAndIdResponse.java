package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Содержит наименование и идентификатор некоторого блюда")
public class DishNameAndIdResponse {

    @Schema(description = "Уникальный идентификатор блюда")
    private UUID dishId;
    @Schema(description = "Наименование блюда")
    private String dishName;

    public DishNameAndIdResponse() {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishNameAndIdResponse that = (DishNameAndIdResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(dishName, that.dishName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, dishName);
    }

    @Override
    public String toString() {
        return "DishNameAndIdResponse{" +
                "dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                '}';
    }

}

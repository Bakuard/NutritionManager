package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные о блюде входящем в состав меню.")
public class ItemResponse {

    @Schema(description = "Уникальный идентификатор элемента меню.")
    private UUID id;
    @Schema(description = "Наименование блюда.")
    private String dishName;
    @Schema(description = "Кол-во порций указанного блюда.")
    private BigDecimal servingNumber;
    @Schema(description = "Порядковый номер (индекс) блюда среди всех блюд конкретного меню. Нумерация начинается с 0.")
    private int itemIndex;

    public ItemResponse() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemResponse that = (ItemResponse) o;
        return itemIndex == that.itemIndex &&
                Objects.equals(id, that.id) &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(servingNumber, that.servingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dishName, servingNumber, itemIndex);
    }

    @Override
    public String toString() {
        return "ItemRequestResponse{" +
                "id=" + id +
                ", dishName='" + dishName + '\'' +
                ", servingNumber=" + servingNumber +
                ", itemIndex=" + itemIndex +
                '}';
    }

}

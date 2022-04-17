package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Данные о блюде входящем в состав меню")
public class MenuItemRequestResponse {

    @Schema(description = "Наименование блюда. Не может быть null.")
    private String dishName;
    @Schema(description = "Кол-во порций указанного блюда. Не может быть null. Должно быть больше нуля.")
    private BigDecimal servingNumber;

    public MenuItemRequestResponse() {

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
        MenuItemRequestResponse that = (MenuItemRequestResponse) o;
        return Objects.equals(dishName, that.dishName) &&
                Objects.equals(servingNumber, that.servingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishName, servingNumber);
    }

    @Override
    public String toString() {
        return "MenuItemRequest{" +
                "dishName='" + dishName + '\'' +
                ", quantity=" + servingNumber +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Данные о блюде добавляемом в состав меню.")
public class ItemAddRequest {

    @Schema(description = """
            Наименование блюда. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Среди блюд пользователя должно быть блюдо с таким наименованием. <br/>
            3. Значение должно содержать как минимум один отображаемый символ. <br/>
            """)
    private String dishName;
    @Schema(description = """
            Кол-во порций указанного блюда. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Должно быть больше нуля. <br/>
            """)
    private BigDecimal servingNumber;
    @Schema(description = """
            Порядковый номер (индекс) блюда среди всех блюд конкретного меню. Нумерация начинается с 0.
            """)
    private int itemIndex;

    public ItemAddRequest() {

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
        ItemAddRequest that = (ItemAddRequest) o;
        return itemIndex == that.itemIndex &&
                Objects.equals(dishName, that.dishName) &&
                Objects.equals(servingNumber, that.servingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishName, servingNumber, itemIndex);
    }

    @Override
    public String toString() {
        return "ItemAddRequest{" +
                "dishName='" + dishName + '\'' +
                ", servingNumber=" + servingNumber +
                ", itemIndex=" + itemIndex +
                '}';
    }

}

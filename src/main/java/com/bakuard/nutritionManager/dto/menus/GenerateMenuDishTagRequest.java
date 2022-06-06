package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = """
        Ограничения на кол-во блюд с указанным тегом. Используется при генерации меню.
        """)
public class GenerateMenuDishTagRequest {

    @Schema(description = """
            Тег блюда. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Должно существовать хотя бы одно блюдо с указанным тегом. <br/>
            """)
    private String dishTag;
    @Schema(description = """
            Ограничения на кол-во блюд с указанным тегом ("больше или равно" или "меньше или равно").
             Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Может принимать значения только из указанного множества.
            """,
            allowableValues = {"lessOrEqual", "greaterOrEqual"})
    private String condition;
    @Schema(description = """
            Кол-во блюд с указанным тегом. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Не может быть отрицательным числом.
            """)
    private BigDecimal quantity;

    public GenerateMenuDishTagRequest() {

    }

    public String getDishTag() {
        return dishTag;
    }

    public void setDishTag(String dishTag) {
        this.dishTag = dishTag;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerateMenuDishTagRequest that = (GenerateMenuDishTagRequest) o;
        return Objects.equals(dishTag, that.dishTag) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishTag, condition, quantity);
    }

    @Override
    public String toString() {
        return "GenerateMenuDishTagRequest{" +
                "dishTag='" + dishTag + '\'' +
                ", condition='" + condition + '\'' +
                ", quantity=" + quantity +
                '}';
    }

}

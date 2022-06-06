package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = """
        Ограничения на кол-во продуктов определенной категории. Используется при генерации меню.
        """)
public class GenerateMenuProductRequest {

    @Schema(description = """
            Категория продукта. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Должен существовать хотя бы один продукт с указанной категорией. <br/>
            """)
    private String productCategory;
    @Schema(description = """
            Ограничения на кол-во продуктов указанной категории ("больше или равно" или "меньше или равно").
             Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Может принимать значения только из указанного множества. <br/>
            """,
            allowableValues = {"lessOrEqual", "greaterOrEqual"})
    private String condition;
    @Schema(description = """
            Кол-во продукта указанной категории. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Не может быть отрицательным числом. <br/>
            """)
    private BigDecimal quantity;

    public GenerateMenuProductRequest() {

    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
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
        GenerateMenuProductRequest that = (GenerateMenuProductRequest) o;
        return Objects.equals(productCategory, that.productCategory) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCategory, condition, quantity);
    }

    @Override
    public String toString() {
        return "GenerateMenuProductRequest{" +
                "productCategory='" + productCategory + '\'' +
                ", condition='" + condition + '\'' +
                ", quantity=" + quantity +
                '}';
    }

}

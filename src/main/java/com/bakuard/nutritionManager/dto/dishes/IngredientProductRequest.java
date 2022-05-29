package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Используется для выбора конкретного продукта относящегося к конкретному ингредиенту блюда. Эти данные
         применяются при формировании списка докупаемых продуктов к блюду.
        """)
public class IngredientProductRequest {

    @Schema(description = """
            Индекс ингредиента блюда. Особые случаи: <br/>
            1. Если значение выходит за диаопозон [0, кол-во ингредиентов - 1] - ограничение задаваемое с помощью
               данного объекта будет проигнорировано.
            """)
    private int ingredientIndex;
    @Schema(description = """
            Индекс одного из продуктов соответствующих указанному ингредиенту блюда.Особые случаи: <br/>
            1. Если значение меньше 0 - ограничение задаваемое с помощью данного объекта будет проигнорировано. <br/>
            2. Если значение больше или равно кол-ву всех продуктов соответствующих указанному ингредиенту -
               ограничение задаваемое с помощью данного объекта будет проигнорировано.
            """)
    private int productIndex;

    public IngredientProductRequest() {

    }

    public int getIngredientIndex() {
        return ingredientIndex;
    }

    public void setIngredientIndex(int ingredientIndex) {
        this.ingredientIndex = ingredientIndex;
    }

    public int getProductIndex() {
        return productIndex;
    }

    public void setProductIndex(int productIndex) {
        this.productIndex = productIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientProductRequest that = (IngredientProductRequest) o;
        return ingredientIndex == that.ingredientIndex &&
                productIndex == that.productIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientIndex, productIndex);
    }

    @Override
    public String toString() {
        return "DishIngredientProductRequest{" +
                "ingredientIndex=" + ingredientIndex +
                ", productIndex=" + productIndex +
                '}';
    }

}

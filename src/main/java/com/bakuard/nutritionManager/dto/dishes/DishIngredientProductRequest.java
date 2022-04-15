package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Используется для выбора конкретного продукта относящегося к конкретному ингредиенту блюда. Эти данные
         применяются при формировании списка докупаемых продуктов к блюду.
        """)
public class DishIngredientProductRequest {

    @Schema(description = """
            Индекс ингредиента блюда. Принимает значение в диаопозне [0, кол-во ингредиентов - 1]. Допускаются
             значения принадлежащие только указанному диапозону.
            """)
    private int ingredientIndex;
    @Schema(description = """
            Индекс одного из продуктов соответствующих указанному ингредиенту блюда. Должен принимать
             значения от 0 и выше. Если индекс продукта имеет значение равное или большее кол-ву всех
             продуктов подзодящих для указанного ингрдеиента, то в качесетве продуктоа для этого индекса будет
             выбран продукт с наибольшей ценой.
            """)
    private int productIndex;

    public DishIngredientProductRequest() {

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
        DishIngredientProductRequest that = (DishIngredientProductRequest) o;
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

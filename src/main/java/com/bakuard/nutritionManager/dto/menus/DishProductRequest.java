package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Используется для выбора конкретного продукта относящегося к конкретному ингредиенту определенного
        блюда. Эти данные применяются при формировании списка докупаемых продуктов к меню.
        """)
public class DishProductRequest {

    @Schema(description = """
            Наименования выбранного блюда. Особые случаи: <br/>
            1. Если среди блюд этого меню нет блюда с таким наименованием - ограничение задаваемое с
               помощью данного объекта будет проигнорировано.
            """)
    private String dishName;
    @Schema(description = """
            Индекс ингредиента блюда. Особые случаи: <br/>
            1. Если значение выходит за диаопозон [0, кол-во ингредиентов этого блюда - 1] - ограничение задаваемое
               с помощью данного объекта будет проигнорировано.
            """)
    private int ingredientIndex;
    @Schema(description = """
            Индекс одного из продуктов соответствующих указанному ингредиенту блюда.Особые случаи: <br/>
            1. Если значение меньше 0 - ограничение задаваемое с помощью данного объекта будет проигнорировано. <br/>
            2. Если значение больше или равно кол-ву всех продуктов соответствующих указанному ингредиенту -
               ограничение задаваемое с помощью данного объекта будет проигнорировано.
            """)
    private int productIndex;

    public DishProductRequest() {

    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
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
        DishProductRequest that = (DishProductRequest) o;
        return ingredientIndex == that.ingredientIndex &&
                productIndex == that.productIndex &&
                Objects.equals(dishName, that.dishName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishName, ingredientIndex, productIndex);
    }

    @Override
    public String toString() {
        return "DishProductRequest{" +
                "dishName='" + dishName + '\'' +
                ", ingredientIndex=" + ingredientIndex +
                ", productIndex=" + productIndex +
                '}';
    }

}

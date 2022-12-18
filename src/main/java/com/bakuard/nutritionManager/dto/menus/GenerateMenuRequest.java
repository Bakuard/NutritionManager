package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Schema(description = """
        Параметры задаваемые для генерируемого меню.
        """)
public class GenerateMenuRequest {

    @Schema(description = """
            Наименование меню. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Среди меню пользователя не должно быть меню с таким именем. <br/>
            """)
    private String menuName;
    @Schema(description = """
            Минимально необходимое кол-во примеров пищи на одного человека. Ограничения: <br/>
            1. Должно быть больше нуля. <br/>
            """)
    private int minMealsNumber;
    @Schema(description = """
            Кол-во порций блюд в одном приеме пищи. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Должно быть больше нуля. <br/>
            """)
    private BigDecimal servingNumberPerMeal;
    @Schema(description = """
            Ограничения на кол-во продуктов в составе генерируемого меню. Список может быть пустым
            или иметь значение null.
            """)
    private List<GenerateMenuProductRequest> productConstraints;
    @Schema(description = """
            Ограничения на кол-во блюд с определенным тегом в составе генерируемого меню. Список может быть
            пустым или иметь значение null.
            """)
    private List<GenerateMenuDishTagRequest> dishTagConstraints;

    public GenerateMenuRequest() {

    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getMinMealsNumber() {
        return minMealsNumber;
    }

    public void setMinMealsNumber(int minMealsNumber) {
        this.minMealsNumber = minMealsNumber;
    }

    public BigDecimal getServingNumberPerMeal() {
        return servingNumberPerMeal;
    }

    public void setServingNumberPerMeal(BigDecimal servingNumberPerMeal) {
        this.servingNumberPerMeal = servingNumberPerMeal;
    }

    public Optional<List<GenerateMenuProductRequest>> getProductConstraints() {
        return Optional.ofNullable(productConstraints);
    }

    public void setProductConstraints(List<GenerateMenuProductRequest> productConstraints) {
        this.productConstraints = productConstraints;
    }

    public Optional<List<GenerateMenuDishTagRequest>> getDishTagConstraints() {
        return Optional.ofNullable(dishTagConstraints);
    }

    public void setDishTagConstraints(List<GenerateMenuDishTagRequest> dishTagConstraints) {
        this.dishTagConstraints = dishTagConstraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerateMenuRequest that = (GenerateMenuRequest) o;
        return minMealsNumber == that.minMealsNumber &&
                Objects.equals(menuName, that.menuName) &&
                Objects.equals(servingNumberPerMeal, that.servingNumberPerMeal) &&
                Objects.equals(productConstraints, that.productConstraints) &&
                Objects.equals(dishTagConstraints, that.dishTagConstraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuName, minMealsNumber, servingNumberPerMeal,
                productConstraints, dishTagConstraints);
    }

    @Override
    public String toString() {
        return "GenerateMenuRequest{" +
                "menuName='" + menuName + '\'' +
                ", minMealsNumber=" + minMealsNumber +
                ", servingNumberPerMeal=" + servingNumberPerMeal +
                ", productConstraints=" + productConstraints +
                ", dishTagConstraints=" + dishTagConstraints +
                '}';
    }

}

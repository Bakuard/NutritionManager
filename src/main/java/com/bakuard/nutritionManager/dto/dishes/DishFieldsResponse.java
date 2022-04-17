package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.FieldResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные о тегах, единицах измерения кол-ва и наименованиях всех блюд конкретного пользователя
         (без дубликатов).
        """)
public class DishFieldsResponse {

    @Schema(description = "Единицы измерения кол-ва всех блюд пользователя.")
    private List<FieldResponse> dishUnits;
    @Schema(description = "Все теги всех блюд пользователя.")
    private List<FieldResponse> dishTags;
    @Schema(description = "Наименования всех блюд пользователя.")
    private List<FieldResponse> dishNames;
    @Schema(description = "Категории всех продуктов пользователя.")
    private List<FieldResponse> productCategories;

    public DishFieldsResponse() {

    }

    public List<FieldResponse> getDishUnits() {
        return dishUnits;
    }

    public void setDishUnits(List<FieldResponse> dishUnits) {
        this.dishUnits = dishUnits;
    }

    public List<FieldResponse> getDishTags() {
        return dishTags;
    }

    public void setDishTags(List<FieldResponse> dishTags) {
        this.dishTags = dishTags;
    }

    public List<FieldResponse> getDishNames() {
        return dishNames;
    }

    public void setDishNames(List<FieldResponse> dishNames) {
        this.dishNames = dishNames;
    }

    public List<FieldResponse> getProductCategories() {
        return productCategories;
    }

    public void setProductCategories(List<FieldResponse> productCategories) {
        this.productCategories = productCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishFieldsResponse response = (DishFieldsResponse) o;
        return Objects.equals(dishUnits, response.dishUnits) &&
                Objects.equals(dishTags, response.dishTags) &&
                Objects.equals(dishNames, response.dishNames) &&
                Objects.equals(productCategories, response.productCategories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishUnits, dishTags, dishNames, productCategories);
    }

    @Override
    public String toString() {
        return "DishFieldsResponse{" +
                "dishUnits=" + dishUnits +
                ", dishTags=" + dishTags +
                ", dishNames=" + dishNames +
                ", productCategories=" + productCategories +
                '}';
    }

}

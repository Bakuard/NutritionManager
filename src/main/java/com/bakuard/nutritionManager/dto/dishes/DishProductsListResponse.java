package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.products.ProductAsDishIngredientResponse;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.SortedMap;

public class DishProductsListResponse {

    private BigDecimal servingNumber;
    private BigDecimal totalPrice;
    private SortedMap<String, ProductAsDishIngredientResponse> ingredients;

    public DishProductsListResponse() {

    }

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public void setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public SortedMap<String, ProductAsDishIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(SortedMap<String, ProductAsDishIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListResponse that = (DishProductsListResponse) o;
        return Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servingNumber, totalPrice, ingredients);
    }

    @Override
    public String toString() {
        return "DishProductsListResponse{" +
                "servingNumber=" + servingNumber +
                ", totalPrice=" + totalPrice +
                ", ingredients=" + ingredients +
                '}';
    }

}

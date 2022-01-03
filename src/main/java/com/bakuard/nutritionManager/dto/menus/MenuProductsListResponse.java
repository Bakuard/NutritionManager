package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.dishes.DishForMenuProductsListResponse;
import com.bakuard.nutritionManager.dto.products.ProductAsMenuIngredientResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MenuProductsListResponse {

    private BigDecimal servingNumber;
    private BigDecimal totalPrice;
    private List<DishForMenuProductsListResponse> dishes;
    private List<ProductAsMenuIngredientResponse> ingredients;

    public MenuProductsListResponse() {

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

    public List<DishForMenuProductsListResponse> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishForMenuProductsListResponse> dishes) {
        this.dishes = dishes;
    }

    public List<ProductAsMenuIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<ProductAsMenuIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuProductsListResponse that = (MenuProductsListResponse) o;
        return Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(dishes, that.dishes) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servingNumber, totalPrice, dishes, ingredients);
    }

    @Override
    public String toString() {
        return "MenuProductsListResponse{" +
                "servingNumber=" + servingNumber +
                ", totalPrice=" + totalPrice +
                ", dishes=" + dishes +
                ", ingredients=" + ingredients +
                '}';
    }

}

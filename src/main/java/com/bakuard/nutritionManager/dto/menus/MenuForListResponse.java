package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MenuForListResponse {

    private UUID id;
    private UserResponse user;
    private String imagePath;
    private String name;
    private BigDecimal averagePrice;
    private List<MenuIngredientResponse> ingredients;

    public MenuForListResponse() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public List<MenuIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<MenuIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuForListResponse that = (MenuForListResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(name, that.name) &&
                Objects.equals(averagePrice, that.averagePrice) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, imagePath, name, averagePrice, ingredients);
    }

    @Override
    public String toString() {
        return "MenuForListResponse{" +
                "id=" + id +
                ", user=" + user +
                ", imagePath='" + imagePath + '\'' +
                ", name='" + name + '\'' +
                ", averagePrice=" + averagePrice +
                ", ingredients=" + ingredients +
                '}';
    }

}

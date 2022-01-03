package com.bakuard.nutritionManager.dto.menus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MenuRequest {

    private UUID id;
    private UUID userId;
    private String imagePath;
    private String name;
    private String description;
    private List<MenuIngredientRequest> ingredients;

    public MenuRequest() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MenuIngredientRequest> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<MenuIngredientRequest> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuRequest that = (MenuRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, imagePath, name, description, ingredients);
    }

    @Override
    public String toString() {
        return "MenuRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", imagePath='" + imagePath + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }

}

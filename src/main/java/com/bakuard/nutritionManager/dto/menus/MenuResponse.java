package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MenuResponse {

    private UUID id;
    private UserResponse user;
    private String imagePath;
    private String name;
    private String description;
    private List<MenuIngredientResponse> ingredients;

    public MenuResponse() {

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        MenuResponse that = (MenuResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, imagePath, name, description, ingredients);
    }

    @Override
    public String toString() {
        return "MenuResponse{" +
                "id=" + id +
                ", user=" + user +
                ", imagePath='" + imagePath + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ingredient=" + ingredients +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DishResponse {

    private UUID id;
    private UserResponse user;
    private String name;
    private BigDecimal servingSize;
    private String unit;
    private String description;
    private String imagePath;
    private List<DishIngredientRequestAndResponse> ingredients;
    private List<String> tags;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getServingSize() {
        return servingSize;
    }

    public void setServingSize(BigDecimal servingSize) {
        this.servingSize = servingSize;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<DishIngredientRequestAndResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredientRequestAndResponse> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishResponse that = (DishResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(name, that.name) &&
                Objects.equals(servingSize, that.servingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, name, servingSize, unit,
                description, imagePath, ingredients, tags);
    }

    @Override
    public String toString() {
        return "DishResponse{" +
                "id=" + id +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }

}

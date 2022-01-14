package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DishRequest {

    private UUID id;
    private UUID userId;
    private String name;
    private BigDecimal servingSize;
    private String unit;
    private String description;
    private String imagePath;
    private List<DishIngredientRequestAndResponse> ingredients;
    private List<TagRequestAndResponse> tags;

    public DishRequest() {

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

    public List<TagRequestAndResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagRequestAndResponse> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishRequest that = (DishRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
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
        return Objects.hash(id, userId, name, servingSize, unit,
                description, imagePath, ingredients, tags);
    }

    @Override
    public String toString() {
        return "DishRequest{" +
                "id=" + id +
                ", userId=" + userId +
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

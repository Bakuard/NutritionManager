package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Данные используемые в запросе обновления блюда.
        """)
public class DishUpdateRequest {

    @Schema(description = "Уникальный идентфикатор блюда")
    private UUID id;
    @Schema(description = "Наименование блюда")
    private String name;
    @Schema(description = "Размер одной порции блюда")
    private BigDecimal servingSize;
    @Schema(description = "Единица измерения кол-ва блюда")
    private String unit;
    @Schema(description = "Описание блюда")
    private String description;
    @Schema(description = "Путь к изображению данного блюда")
    private String imageUrl;
    @Schema(description = "Ингредиенты блюда")
    private List<DishIngredientRequestResponse> ingredients;
    @Schema(description = "Теги блюда")
    private List<String> tags;

    public DishUpdateRequest() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<DishIngredientRequestResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredientRequestResponse> ingredients) {
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
        DishUpdateRequest that = (DishUpdateRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(servingSize, that.servingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, servingSize, unit,
                description, imageUrl, ingredients, tags);
    }

    @Override
    public String toString() {
        return "DishRequest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }

}

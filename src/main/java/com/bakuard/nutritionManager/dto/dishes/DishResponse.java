package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Возвращаемые подробные данные о блюде.")
public class DishResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Всегда имеет значение Dish.")
    private String type;
    @Schema(description = "Уникальный идентфикатор блюда.")
    private UUID id;
    @Schema(description = "Данные пользователя, которому принадлежит блюдо.")
    private UserResponse user;
    @Schema(description = "Наименование блюда.")
    private String name;
    @Schema(description = "Размер одной порции блюда.")
    private BigDecimal servingSize;
    @Schema(description = "Единица измерения кол-ва блюда.")
    private String unit;
    @Schema(description = "Описание блюда.")
    private String description;
    @Schema(description = "Путь к изображению данного блюда.")
    private URL imageUrl;
    @Schema(description = "Ингредиенты блюда")
    private List<DishIngredientRequestResponse> ingredients;
    @Schema(description = "Теги блюда. Если блюдо не содержит тегов - данный список будет пустым.")
    private List<String> tags;

    public DishResponse() {
        type = "Dish";
    }

    public String getType() {
        return type;
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

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
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
        DishResponse that = (DishResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
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
        return Objects.hash(id, user, name, servingSize, unit,
                description, imageUrl, ingredients, tags);
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
                ", imageUrl='" + imageUrl + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }

}

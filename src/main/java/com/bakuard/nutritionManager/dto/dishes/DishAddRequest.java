package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные используемые в запросе на добавление блюда. Не допускается, чтобы у пользователя были два
         и более блюда с одинаковыми наименованиями.
        """)
public class DishAddRequest {

    @Schema(description = """
            Наименование блюда. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Должно содержать как минимум один отображаемый символ. <br/>
            """)
    private String name;
    @Schema(description = """
            Размер одной порции блюда. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Должен быть больше нуля. <br/>
            """)
    private BigDecimal servingSize;
    @Schema(description = """
            Единица измерения кол-ва блюда. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Должна содержать как минимум один отображаемый символ. <br/>
            """)
    private String unit;
    @Schema(description = "Описание блюда")
    private String description;
    @Schema(description = """
            Путь к изображению данного блюда. Ограничения:<br/>
            1. Должен быть null ИЛИ представлять собой корректный URl адресс.<br/>
            """)
    private String imageUrl;
    @Schema(description = """
            Ингредиенты блюда. Допускается пустой список ингредиентов. Ограничения:<br/>
            1. Список ингредиентов не должен быть null. <br/>
            2. Каждый элемент списка ингредиентов не должен быть null. <br/>
            3. Наименования всех ингрдеиентов должны быть уникальны. <br/>
            """)
    private List<DishIngredientRequestResponse> ingredients;
    @Schema(description = """
            Теги указанные для данного блюда. Допускается пустой список тегов. Ограничения:<br/>
            1. Список тегов не может быть null.<br/>
            2. Все теги должны быть уникальны.<br/>
            3. Теги не могут иметь значение null.<br/>
            4. Теги обязаны содержать как минимум один отображаемый символ.<br/>
            """)
    private List<String> tags;

    public DishAddRequest() {

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
        DishAddRequest that = (DishAddRequest) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(servingSize, that.servingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, servingSize, unit, description, imageUrl, ingredients, tags);
    }

    @Override
    public String toString() {
        return "DishAddRequest{" +
                "name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }

}

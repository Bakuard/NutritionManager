package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Фильтр задающий множество взаимозаменяемых продуктов для ингредиента блюда")
public class DishIngredientFilterRequestResponse {

    @Schema(description = """
            Категория продуктов. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Должна содержать как минимум один отображаемый символ. <br/>
            """)
    private String category;
    @Schema(description = """
            Список магазинов продуктов. Продукты соответствующие данному фильтру должны иметь один из
             указанных магазинов в этом списке. Если нет ограничений на магазины и подойдет любой, то
             данный параметр должен иметь значение null либо быть пустым массивом. Ограничения:<br/>
            1. Каждый элемент списка не должен быть null. <br/>
            2. Каждый элемент списка должен содержать как минимум один отображаемый символ. <br/>
            """)
    private List<String> shops;
    @Schema(description = """
            Список магазинов продуктов. Продукты соответствующие данному фильтру должны иметь один из
             указанных сортов в этом списке. Если нет ограничений на сорта и подойдет любой, то
             данный параметр должен иметь значение null либо быть пустым массивом. Ограничения:<br/>
            1. Каждый элемент списка не должен быть null. <br/>
            2. Каждый элемент списка должен содержать как минимум один отображаемый символ. <br/>
            """)
    private List<String> grades;
    @Schema(description = """
            Список магазинов продуктов. Продукты соответствующие данному фильтру должны иметь один из
             указанных производителей в этом списке. Если нет ограничений на производителей и подойдет любой,
             то данный параметр должен иметь значение null либо быть пустым массивом. Ограничения:<br/>
            1. Каждый элемент списка не должен быть null. <br/>
            2. Каждый элемент списка должен содержать как минимум один отображаемый символ. <br/>
            """)
    private List<String> manufacturers;
    @Schema(description = """
            Список магазинов продуктов. Продукты соответствующие данному фильтру должны имет как минимум
             ВСЕ указанные теги в этом списке. Если нет ограничений на теги и подойдут любые, то данный параметр
             должен иметь значение null либо быть пустым массивом. Ограничения:<br/>
            1. Каждый элемент списка не должен быть null. <br/>
            2. Каждый элемент списка должен содержать как минимум один отображаемый символ. <br/>
            """)
    private List<String> tags;

    public DishIngredientFilterRequestResponse() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getShops() {
        return shops;
    }

    public void setShops(List<String> shops) {
        this.shops = shops;
    }

    public List<String> getGrades() {
        return grades;
    }

    public void setGrades(List<String> grades) {
        this.grades = grades;
    }

    public List<String> getManufacturers() {
        return manufacturers;
    }

    public void setManufacturers(List<String> manufacturers) {
        this.manufacturers = manufacturers;
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
        DishIngredientFilterRequestResponse that = (DishIngredientFilterRequestResponse) o;
        return Objects.equals(category, that.category) &&
                Objects.equals(shops, that.shops) &&
                Objects.equals(grades, that.grades) &&
                Objects.equals(manufacturers, that.manufacturers) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, shops, grades, manufacturers, tags);
    }

    @Override
    public String toString() {
        return "DishIngredientFilterRequestResponse{" +
                "category='" + category + '\'' +
                ", shops=" + shops +
                ", grades=" + grades +
                ", manufacturers=" + manufacturers +
                ", tags=" + tags +
                '}';
    }

}

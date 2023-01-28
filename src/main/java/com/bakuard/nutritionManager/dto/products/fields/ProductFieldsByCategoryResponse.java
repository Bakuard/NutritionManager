package com.bakuard.nutritionManager.dto.products.fields;

import com.bakuard.nutritionManager.dto.FieldResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Все теги, производители, сорта и магазины относящиеся к продукту определенной категории
        конкретного пользователя.
        """)
public class ProductFieldsByCategoryResponse {

    @Schema(description = """
            Категория продуктов с которой связаны значения всех остальных полей продукта.
            """)
    private String category;
    @Schema(description = """
            Все теги всех продуктов одной категории определенного пользователя.
             Если ни для одного продукта этой категории не используется ни один тег -
             данный список будет пустым.
            """)
    private List<FieldResponse> tags;
    @Schema(description = """
            Все производители всех продуктов одной категории определенного пользователя.
             Если у пользователя нет ни одного продукта этой категории - данный список будет пустым.
            """)
    private List<FieldResponse> manufacturers;
    @Schema(description = """
            Все сорта всех продуктов одной категории определенного пользователя.
             Если у пользователя нет ни одного продукта этой категории - данный список будет пустым.
            """)
    private List<FieldResponse> grades;
    @Schema(description = """
            Все магазины всех продуктов одной категории определенного пользователя.
             Если у пользователя нет ни одного продукта этой категории - данный список будет пустым.
            """)
    private List<FieldResponse> shops;

    public ProductFieldsByCategoryResponse() {

    }

    public String getCategory() {
        return category;
    }

    public ProductFieldsByCategoryResponse setCategory(String category) {
        this.category = category;
        return this;
    }

    public List<FieldResponse> getTags() {
        return tags;
    }

    public ProductFieldsByCategoryResponse setTags(List<FieldResponse> tags) {
        this.tags = tags;
        return this;
    }

    public List<FieldResponse> getManufacturers() {
        return manufacturers;
    }

    public ProductFieldsByCategoryResponse setManufacturers(List<FieldResponse> manufacturers) {
        this.manufacturers = manufacturers;
        return this;
    }

    public List<FieldResponse> getGrades() {
        return grades;
    }

    public ProductFieldsByCategoryResponse setGrades(List<FieldResponse> grades) {
        this.grades = grades;
        return this;
    }

    public List<FieldResponse> getShops() {
        return shops;
    }

    public ProductFieldsByCategoryResponse setShops(List<FieldResponse> shops) {
        this.shops = shops;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFieldsByCategoryResponse that = (ProductFieldsByCategoryResponse) o;
        return Objects.equals(category, that.category) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(manufacturers, that.manufacturers) &&
                Objects.equals(grades, that.grades) &&
                Objects.equals(shops, that.shops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, tags, manufacturers, grades, shops);
    }

    @Override
    public String toString() {
        return "ProductCategoryResponse{" +
                "category='" + category + '\'' +
                ", tags=" + tags +
                ", manufacturers=" + manufacturers +
                ", grades=" + grades +
                ", shops=" + shops +
                '}';
    }

}

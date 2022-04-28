package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.FieldResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Все теги, категории, производители, сорта и магазины всех продуктов конкретного пользователя.
        """)
public class ProductFieldsResponse {

    @Schema(description = """
            Все теги всех продуктов пользователя. Если ни для оодного продукта не используется ни один тег -
             данный список будет пустым.
            """)
    private List<FieldResponse> tags;
    @Schema(description = """
            Все категории всех продуктов пользователя. Если у пользователя нет ни одного продукта - данный
             список будет пустым.
            """)
    private List<FieldResponse> categories;
    @Schema(description = """
            Все производители всех продуктов пользователя. Если у пользователя нет ни одного продукта - данный
             список будет пустым.
            """)
    private List<FieldResponse> manufacturers;
    @Schema(description = """
            Все сорта всех продуктов пользователя. Если у пользователя нет ни одного продукта - данный
             список будет пустым.
            """)
    private List<FieldResponse> varieties;
    @Schema(description = """
            Все магазины всех продуктов пользователя. Если у пользователя нет ни одного продукта - данный
             список будет пустым.
            """)
    private List<FieldResponse> shops;

    public ProductFieldsResponse() {

    }

    public List<FieldResponse> getTags() {
        return tags;
    }

    public void setTags(List<FieldResponse> tags) {
        this.tags = tags;
    }

    public List<FieldResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<FieldResponse> categories) {
        this.categories = categories;
    }

    public List<FieldResponse> getManufacturers() {
        return manufacturers;
    }

    public void setManufacturers(List<FieldResponse> manufacturers) {
        this.manufacturers = manufacturers;
    }

    public List<FieldResponse> getVarieties() {
        return varieties;
    }

    public void setGrades(List<FieldResponse> grades) {
        this.varieties = grades;
    }

    public List<FieldResponse> getShops() {
        return shops;
    }

    public void setShops(List<FieldResponse> shops) {
        this.shops = shops;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFieldsResponse that = (ProductFieldsResponse) o;
        return Objects.equals(tags, that.tags) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(manufacturers, that.manufacturers) &&
                Objects.equals(varieties, that.varieties) &&
                Objects.equals(shops, that.shops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, categories, manufacturers, varieties, shops);
    }

    @Override
    public String toString() {
        return "ProductFieldsResponse{" +
                "tags=" + tags +
                ", categories=" + categories +
                ", manufacturers=" + manufacturers +
                ", grades=" + varieties +
                ", shops=" + shops +
                '}';
    }

}

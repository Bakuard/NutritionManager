package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.model.Tag;

import java.util.List;
import java.util.Objects;

public class ProductFieldsResponse {

    private List<Tag> tags;
    private List<String> categories;
    private List<String> manufacturers;
    private List<String> varieties;
    private List<String> shops;

    public ProductFieldsResponse() {

    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getManufacturers() {
        return manufacturers;
    }

    public void setManufacturers(List<String> manufacturers) {
        this.manufacturers = manufacturers;
    }

    public List<String> getVarieties() {
        return varieties;
    }

    public void setVarieties(List<String> varieties) {
        this.varieties = varieties;
    }

    public List<String> getShops() {
        return shops;
    }

    public void setShops(List<String> shops) {
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
                ", varieties=" + varieties +
                ", shops=" + shops +
                '}';
    }

}

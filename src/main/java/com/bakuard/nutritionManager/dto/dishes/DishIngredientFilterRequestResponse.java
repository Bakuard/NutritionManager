package com.bakuard.nutritionManager.dto.dishes;

import java.util.List;
import java.util.Objects;

public class DishIngredientFilterRequestResponse {

    private String category;
    private List<String> shops;
    private List<String> varieties;
    private List<String> manufacturers;
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

    public List<String> getVarieties() {
        return varieties;
    }

    public void setVarieties(List<String> varieties) {
        this.varieties = varieties;
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
                Objects.equals(varieties, that.varieties) &&
                Objects.equals(manufacturers, that.manufacturers) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, shops, varieties, manufacturers, tags);
    }

    @Override
    public String toString() {
        return "DishIngredientFilterRequestResponse{" +
                "category='" + category + '\'' +
                ", shops=" + shops +
                ", varieties=" + varieties +
                ", manufacturers=" + manufacturers +
                ", tags=" + tags +
                '}';
    }

}

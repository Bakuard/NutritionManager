package com.bakuard.nutritionManager.dto.dishes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DishForListResponse {

    private String imagePath;
    private String name;
    private BigDecimal servingSize;
    private String unit;
    private BigDecimal averagePrice;
    private List<String> tags;

    public DishForListResponse() {

    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
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
        DishForListResponse that = (DishForListResponse) o;
        return Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(name, that.name) &&
                Objects.equals(servingSize, that.servingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(averagePrice, that.averagePrice) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imagePath, name, servingSize, unit, averagePrice, tags);
    }

    @Override
    public String toString() {
        return "DishForListResponse{" +
                "imagePath='" + imagePath + '\'' +
                ", name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", averagePrice=" + averagePrice +
                ", tags=" + tags +
                '}';
    }

}

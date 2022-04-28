package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Возвращаемые частичные данные о блюде используемые в списке блюд.")
public class DishForListResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Имеет значение Dish.")
    private String type;
    @Schema(description = "Уникальный идентфикатор блюда.")
    private UUID id;
    @Schema(description = "Путь к изображению данного блюда.")
    private URL imageUrl;
    @Schema(description = "Наименование блюда.")
    private String name;
    @Schema(description = "Размер одной порции блюда.")
    private BigDecimal servingSize;
    @Schema(description = "Единица измерения кол-ва блюда.")
    private String unit;
    @Schema(description = "Среднеарифметическая цена блюда.")
    private BigDecimal averagePrice;
    @Schema(description = "Теги блюда. Если блюдо не содержит тегов - данный список будет пустым.")
    private List<String> tags;

    public DishForListResponse() {
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

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
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
        DishForListResponse response = (DishForListResponse) o;
        return Objects.equals(id, response.id) &&
                Objects.equals(imageUrl, response.imageUrl) &&
                Objects.equals(name, response.name) &&
                Objects.equals(servingSize, response.servingSize) &&
                Objects.equals(unit, response.unit) &&
                Objects.equals(averagePrice, response.averagePrice) &&
                Objects.equals(tags, response.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, name, servingSize, unit, averagePrice, tags);
    }

    @Override
    public String toString() {
        return "DishForListResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", imageUrl=" + imageUrl +
                ", name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", averagePrice=" + averagePrice +
                ", tags=" + tags +
                '}';
    }

}

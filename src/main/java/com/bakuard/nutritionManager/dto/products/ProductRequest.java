package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductRequest {

    private UUID id;
    private UUID userId;
    private String category;
    private String shop;
    private String variety;
    private String manufacturer;
    private BigDecimal price;
    private BigDecimal packingSize;
    private String unit;
    private BigDecimal quantity;
    private String description;
    private String imagePath;
    private List<TagRequestAndResponse> tags;

    public ProductRequest() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPackingSize() {
        return packingSize;
    }

    public void setPackingSize(BigDecimal packingSize) {
        this.packingSize = packingSize;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<TagRequestAndResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagRequestAndResponse> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductRequest that = (ProductRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(category, that.category) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(variety, that.variety) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(price, that.price) &&
                Objects.equals(packingSize, that.packingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, category, shop, variety, manufacturer,
                price, packingSize, unit, quantity, description, imagePath, tags);
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", variety='" + variety + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", price=" + price +
                ", packingSize=" + packingSize +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", tags=" + tags +
                '}';
    }

}

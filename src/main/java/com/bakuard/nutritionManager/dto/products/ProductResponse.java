package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;
import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductResponse {

    private String type;
    private UUID id;
    private UserResponse user;
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

    public ProductResponse() {
        type = "Product";
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
        ProductResponse response = (ProductResponse) o;
        return Objects.equals(type, response.type) &&
                Objects.equals(id, response.id) &&
                Objects.equals(user, response.user) &&
                Objects.equals(category, response.category) &&
                Objects.equals(shop, response.shop) &&
                Objects.equals(variety, response.variety) &&
                Objects.equals(manufacturer, response.manufacturer) &&
                Objects.equals(price, response.price) &&
                Objects.equals(packingSize, response.packingSize) &&
                Objects.equals(unit, response.unit) &&
                Objects.equals(quantity, response.quantity) &&
                Objects.equals(description, response.description) &&
                Objects.equals(imagePath, response.imagePath) &&
                Objects.equals(tags, response.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, user, category, shop, variety, manufacturer,
                price, packingSize, unit, quantity, description, imagePath, tags);
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
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

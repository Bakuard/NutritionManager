package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;
import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductAsMenuIngredientResponse {

    private String type;
    private UUID id;
    private UserResponse user;
    private String imagePath;
    private String category;
    private String shop;
    private String variety;
    private String manufacturer;
    private BigDecimal price;
    private BigDecimal packingSize;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal necessaryQuantity;
    private BigDecimal lackQuantity;
    private BigDecimal lackQuantityPrice;
    private List<String> dishes;
    private List<TagRequestAndResponse> tags;

    public ProductAsMenuIngredientResponse() {
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public BigDecimal getNecessaryQuantity() {
        return necessaryQuantity;
    }

    public void setNecessaryQuantity(BigDecimal necessaryQuantity) {
        this.necessaryQuantity = necessaryQuantity;
    }

    public BigDecimal getLackQuantity() {
        return lackQuantity;
    }

    public void setLackQuantity(BigDecimal lackQuantity) {
        this.lackQuantity = lackQuantity;
    }

    public BigDecimal getLackQuantityPrice() {
        return lackQuantityPrice;
    }

    public void setLackQuantityPrice(BigDecimal lackQuantityPrice) {
        this.lackQuantityPrice = lackQuantityPrice;
    }

    public List<String> getDishes() {
        return dishes;
    }

    public void setDishes(List<String> dishes) {
        this.dishes = dishes;
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
        ProductAsMenuIngredientResponse that = (ProductAsMenuIngredientResponse) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(category, that.category) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(variety, that.variety) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(price, that.price) &&
                Objects.equals(packingSize, that.packingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(necessaryQuantity, that.necessaryQuantity) &&
                Objects.equals(lackQuantity, that.lackQuantity) &&
                Objects.equals(lackQuantityPrice, that.lackQuantityPrice) &&
                Objects.equals(dishes, that.dishes) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, user, imagePath, category, shop,
                variety, manufacturer, price, packingSize, unit, quantity, necessaryQuantity,
                lackQuantity, lackQuantityPrice, dishes, tags);
    }

    @Override
    public String toString() {
        return "ProductAsMenuIngredientResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
                ", imagePath='" + imagePath + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", variety='" + variety + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", price=" + price +
                ", packingSize=" + packingSize +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", necessaryQuantity=" + necessaryQuantity +
                ", lackQuantity=" + lackQuantity +
                ", lackQuantityPrice=" + lackQuantityPrice +
                ", dishes=" + dishes +
                ", tags=" + tags +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductAsDishIngredientResponse {

    private String type;
    private UUID id;
    private UserResponse user;
    private String imagePath;
    private String category;
    private String shop;
    private String grade;
    private String manufacturer;
    private BigDecimal price;
    private BigDecimal packingSize;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal necessaryQuantity;
    private BigDecimal lackQuantity;
    private BigDecimal lackQuantityPrice;
    private List<String> tags;
    private int productsTotalNumber;

    public ProductAsDishIngredientResponse() {
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

    public void setType(String type) {
        this.type = type;
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getProductsTotalNumber() {
        return productsTotalNumber;
    }

    public void setProductsTotalNumber(int productsTotalNumber) {
        this.productsTotalNumber = productsTotalNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAsDishIngredientResponse that = (ProductAsDishIngredientResponse) o;
        return productsTotalNumber == that.productsTotalNumber &&
                Objects.equals(type, that.type) &&
                Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(imagePath, that.imagePath) &&
                Objects.equals(category, that.category) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(price, that.price) &&
                Objects.equals(packingSize, that.packingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(necessaryQuantity, that.necessaryQuantity) &&
                Objects.equals(lackQuantity, that.lackQuantity) &&
                Objects.equals(lackQuantityPrice, that.lackQuantityPrice) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, user, imagePath, category, shop, grade,
                manufacturer, price, packingSize, unit, quantity, necessaryQuantity,
                lackQuantity, lackQuantityPrice, tags, productsTotalNumber);
    }

    @Override
    public String toString() {
        return "ProductAsDishIngredientResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
                ", imagePath='" + imagePath + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", grade='" + grade + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", price=" + price +
                ", packingSize=" + packingSize +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", necessaryQuantity=" + necessaryQuantity +
                ", lackQuantity=" + lackQuantity +
                ", lackQuantityPrice=" + lackQuantityPrice +
                ", tags=" + tags +
                ",productsTotalNumber=" + productsTotalNumber +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Содержит данные продукта входящего в список докупаемых продуктов блюда")
public class ProductAsDishIngredientResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Имеет значение Product.")
    private String type;
    @Schema(description = "Уникальный идентификатор продукта в формате UUID")
    private UUID id;
    @Schema(description = "Учетные данные и ID пользователя к которому относится данный продукт")
    private UserResponse user;
    @Schema(description = "Путь к изображению данного продукта")
    private URL imageUrl;
    @Schema(description = "Категория к которой относится продукт", example = "Помидор")
    private String category;
    @Schema(description = "Один из магазинов в котором можно приобрести продукты указанной категории")
    private String shop;
    @Schema(description = "Один из сортов продуктов указанной категории")
    private String grade;
    @Schema(description = "Один из производителей продуктов указанной категории")
    private String manufacturer;
    @Schema(description = "Цена прдукта указанной категории, сорта, производителя и продающегося в указанном магазине")
    private BigDecimal price;
    @Schema(description = "Размер упаковки")
    private BigDecimal packingSize;
    @Schema(description = "Единица измерения кол-ва продукта")
    private String unit;
    @Schema(description = "Кол-во продукта имеющегося в наличии у пользователя")
    private BigDecimal quantity;
    @Schema(description = "Кол-во продукта необходимого для приготовления заданноо кол-ва порций блюда")
    private BigDecimal necessaryQuantity;
    @Schema(description = """
            Недостающее кол-во продукта (с учетом уже имеющегося у пользователя в наличии) необходимого
             для приготовления указанного кол-ва порций
            """)
    private BigDecimal lackQuantity;
    @Schema(description = """
            Общая стоимость недостающего кол-ва продукта (с учетом уже имеющегося у пользователя в наличии)
             необзодимого для приготовления указанного кол-ва порций
            """)
    private BigDecimal lackQuantityPrice;
    @Schema(description = "Теги указаныне для данного продукта")
    private List<String> tags;
    @Schema(description = "Общее кол-во всех взаимозаменяемых продуктов связанных с данным ингредиентом")
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

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
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
                Objects.equals(imageUrl, that.imageUrl) &&
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
        return Objects.hash(type, id, user, imageUrl, category, shop, grade,
                manufacturer, price, packingSize, unit, quantity, necessaryQuantity,
                lackQuantity, lackQuantityPrice, tags, productsTotalNumber);
    }

    @Override
    public String toString() {
        return "ProductAsDishIngredientResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
                ", imagePath='" + imageUrl + '\'' +
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

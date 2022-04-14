package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Содержит данные продукта входящего в список докупаемых продуктов меню")
public class ProductAsMenuItemResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Имеет значение Product.")
    private String type;
    @Schema(description = "Уникальный идентификатор продукта в формате UUID")
    private UUID id;
    @Schema(description = "Учетные данные и ID пользователя к которому относится данный продукт")
    private UserResponse user;
    @Schema(description = "Путь к изображению данного продукта")
    private String imageUrl;
    @Schema(description = "Категория к которой относится продукт")
    private String category;
    @Schema(description = "Один из магазинов в котором можно приобрести продукты указанной категории")
    private String shop;
    @Schema(description = "Один из сортов продуктов указанной категории")
    private String grade;
    @Schema(description = "Размер упаковки")
    private BigDecimal packingSize;
    @Schema(description = "Единица измерения кол-ва продукта")
    private String unit;
    @Schema(description = "Один из производителей продуктов указанной категории")
    private String manufacturer;
    @Schema(description = "Кол-во продукта имеющегося в наличии у пользователя")
    private BigDecimal quantity;
    @Schema(description = "Теги указаныне для данного продукта")
    private List<String> tags;
    @Schema(description = """
            Кол-во продукта необходимого для приготовления указанных блюд данного меню, с учетом
             кол-ва порций каждого блюда.
            """)
    private BigDecimal necessaryQuantity;
    @Schema(description = """
            Недостающее кол-во данного продукта (учитывая кол-во, которое уже есть у пользователя)
             необходимое для приготовления указанных блюд данного меню, с учетом кол-ва порций каждого
             блюда.
            """)
    private BigDecimal lackQuantity;
    @Schema(description = "Общая стоимость для недостающего кол-ва данного продукта.")
    private BigDecimal lackQuantityPrice;
    @Schema(description = """
            Список блюд, где для каждого блюда указанно - какое кол-во данного продукта необходимо
             для приготовления всех порций конкретного блюда входящего в состав данного меню.
            """)
    private List<DishOfMenuProductListItemResponse> dishes;

    public ProductAsMenuItemResponse() {
        type = "Product";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public List<DishOfMenuProductListItemResponse> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishOfMenuProductListItemResponse> dishes) {
        this.dishes = dishes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAsMenuItemResponse that = (ProductAsMenuItemResponse) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(category, that.category) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(packingSize, that.packingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(necessaryQuantity, that.necessaryQuantity) &&
                Objects.equals(lackQuantity, that.lackQuantity) &&
                Objects.equals(lackQuantityPrice, that.lackQuantityPrice) &&
                Objects.equals(dishes, that.dishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, user, imageUrl, category, shop,
                grade, packingSize, unit, manufacturer, quantity, tags,
                necessaryQuantity, lackQuantity, lackQuantityPrice, dishes);
    }

    @Override
    public String toString() {
        return "ProductAsMenuItemResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
                ", imageUrl='" + imageUrl + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", grade='" + grade + '\'' +
                ", packingSize=" + packingSize +
                ", unit='" + unit + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", quantity=" + quantity +
                ", tags=" + tags +
                ", necessaryQuantity=" + necessaryQuantity +
                ", lackQuantity=" + lackQuantity +
                ", lackQuantityPrice=" + lackQuantityPrice +
                ", dishes=" + dishes +
                '}';
    }

}

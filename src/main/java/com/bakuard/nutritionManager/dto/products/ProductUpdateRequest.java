package com.bakuard.nutritionManager.dto.products;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Данные используемые в запросе на обновление продукта.
        """)
public class ProductUpdateRequest {

    @Schema(description = "Уникальный идентификатор продукта в формате UUID. Не может быть null.")
    private UUID id;
    @Schema(description = "Категория к которой относится продукт. Не может быть null.", example = "Помидор")
    private String category;
    @Schema(description = "Один из магазинов в котором можно приобрести продукты указанной категории. Не может быть null.")
    private String shop;
    @Schema(description = "Один из сортов продуктов указанной категории. Не может быть null.")
    private String grade;
    @Schema(description = "Один из производителей продуктов указанной категории. Не может быть null.")
    private String manufacturer;
    @Schema(description = "Цена продукта указанной категории, сорта, производителя и продающегося в указанном магазине. Не может быть null.")
    private BigDecimal price;
    @Schema(description = "Размер упаковки. Не может быть null.")
    private BigDecimal packingSize;
    @Schema(description = "Единица измерения кол-ва продукта. Не может быть null.")
    private String unit;
    @Schema(description = "Кол-во продукта имеющегося в наличии у пользователя. Не может быть null.")
    private BigDecimal quantity;
    @Schema(description = "Описание продукта")
    private String description;
    @Schema(description = "Путь к изображению данного продукта")
    private String imageUrl;
    @Schema(description = """
            Теги указаныне для данного продукта. Не может быть null. Допускается пустой список тегов.
             Ограничения для тегов: все теги должны быть уникальны, теги не могут иметь значение null и
             обязаны содержать как минимум один отображаемый символ.
            """)
    private List<String> tags;

    public ProductUpdateRequest() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        ProductUpdateRequest that = (ProductUpdateRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(category, that.category) &&
                Objects.equals(shop, that.shop) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(manufacturer, that.manufacturer) &&
                Objects.equals(price, that.price) &&
                Objects.equals(packingSize, that.packingSize) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(description, that.description) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, shop, grade, manufacturer,
                price, packingSize, unit, quantity, description, imageUrl, tags);
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", grade='" + grade + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", price=" + price +
                ", packingSize=" + packingSize +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", tags=" + tags +
                '}';
    }

}

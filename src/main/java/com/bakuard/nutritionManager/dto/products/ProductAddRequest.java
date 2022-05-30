package com.bakuard.nutritionManager.dto.products;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Schema(description = """
        Данные используемые в запросе на добавление продукта. Не допускается, чтобы один пользователь
        имел продукты у которых совпадают значение ВСЕХ следующих полей: категория, магазин, сорт, производитель,
        единица измерения кол-ва, цена, размер упаковки и теги.
        """)
public class ProductAddRequest {

    @Schema(description = """
            Категория к которой относится продукт. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должна содержать как минимум один отображаемый символ.<br/>
            """, example = "Помидор")
    private String category;
    @Schema(description = """
            Один из магазинов в котором можно приобрести продукты указанной категории. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должен содержать как минимум один отображаемый символ.<br/>
            """)
    private String shop;
    @Schema(description = """
            Один из сортов продуктов указанной категории. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должен содержать как минимум один отображаемый символ.<br/>
            """)
    private String grade;
    @Schema(description = """
            Один из производителей продуктов указанной категории. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должен содержать как минимум один отображаемый символ.<br/>
            """)
    private String manufacturer;
    @Schema(description = """
            Цена продукта указанной категории, сорта, производителя и продающегося в указанном магазине. Ограичения:<br/>
            1. Не может быть null.<br/>
            2. Должна быть больше нуля.<br/>
            """)
    private BigDecimal price;
    @Schema(description = """
            Размер упаковки. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должен быть больше нуля.<br/>
            """)
    private BigDecimal packingSize;
    @Schema(description = """
            Единица измерения кол-ва продукта. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Должна содержать как минимум один отображаемый символ.<br/>
            """)
    private String unit;
    @Schema(description = """
            Кол-во продукта имеющегося в наличии у пользователя. Ограничения:<br/>
            1. Не может быть null.<br/>
            2. Не должно быть отрицательным числом.<br/>
            """)
    private BigDecimal quantity;
    @Schema(description = "Описание продукта")
    private String description;
    @Schema(description = """
            Путь к изображению данного продукта. Ограничения:<br/>
            1. Должен быть null ИЛИ представлять собой корректный URl адресс.<br/>
            """)
    private String imageUrl;
    @Schema(description = """
            Теги указанные для данного продукта. Допускается пустой список тегов. Ограничения:<br/>
            1. Список тегов не может быть null.<br/>
            2. Все теги должны быть уникальны.<br/>
            3. Теги не могут иметь значение null.<br/>
            4. Теги обязаны содержать как минимум один отображаемый символ.<br/>
            """)
    private List<String> tags;

    public ProductAddRequest() {

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

    public Optional<List<String>> getTags() {
        return Optional.ofNullable(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAddRequest that = (ProductAddRequest) o;
        return Objects.equals(category, that.category) &&
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
        return Objects.hash(category, shop, grade, manufacturer,
                price, packingSize, unit, quantity, description, imageUrl, tags);
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
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

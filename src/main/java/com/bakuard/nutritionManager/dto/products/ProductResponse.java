package com.bakuard.nutritionManager.dto.products;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Возвращаемые данные о продукте
        """)
public class ProductResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Всегда имеет значение Product.")
    private String type;
    @Schema(description = "Уникальный идентификатор продукта в формате UUID.")
    private UUID id;
    @Schema(description = "Учетные данные и ID пользователя к которому относится данный продукт.")
    private UserResponse user;
    @Schema(description = "Категория к которой относится продукт.", example = "Помидор")
    private String category;
    @Schema(description = "Один из магазинов в котором можно приобрести продукты указанной категории.")
    private String shop;
    @Schema(description = "Один из сортов продуктов указанной категории.")
    private String grade;
    @Schema(description = "Один из производителей продуктов указанной категории.")
    private String manufacturer;
    @Schema(description = "Цена прдукта указанной категории, сорта, производителя и продающегося в указанном магазине.")
    private BigDecimal price;
    @Schema(description = "Размер упаковки.")
    private BigDecimal packingSize;
    @Schema(description = "Единица измерения кол-ва продукта.")
    private String unit;
    @Schema(description = "Кол-во продукта имеющегося в наличии у пользователя.")
    private BigDecimal quantity;
    @Schema(description = "Описание продукта.")
    private String description;
    @Schema(description = "Путь к изображению данного продукта.")
    private URL imageUrl;
    @Schema(description = "Теги указанные для данного продукта. Если продукт не содержит тегов - данный список будет пустым.")
    private List<String> tags;

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

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
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
        ProductResponse response = (ProductResponse) o;
        return Objects.equals(type, response.type) &&
                Objects.equals(id, response.id) &&
                Objects.equals(user, response.user) &&
                Objects.equals(category, response.category) &&
                Objects.equals(shop, response.shop) &&
                Objects.equals(grade, response.grade) &&
                Objects.equals(manufacturer, response.manufacturer) &&
                Objects.equals(price, response.price) &&
                Objects.equals(packingSize, response.packingSize) &&
                Objects.equals(unit, response.unit) &&
                Objects.equals(quantity, response.quantity) &&
                Objects.equals(description, response.description) &&
                Objects.equals(imageUrl, response.imageUrl) &&
                Objects.equals(tags, response.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, user, category, shop, grade, manufacturer,
                price, packingSize, unit, quantity, description, imageUrl, tags);
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "type='" + type + '\'' +
                ", id=" + id +
                ", user=" + user +
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

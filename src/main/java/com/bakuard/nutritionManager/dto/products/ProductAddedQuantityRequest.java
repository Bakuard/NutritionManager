package com.bakuard.nutritionManager.dto.products;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Указывает на сколько нужно увеличить кол-во указанного продукта имеющегося в наличии у указанного
        пользователя.
        """)
public class ProductAddedQuantityRequest {

    @Schema(description = "Уникальный идентификатор продукта в формате UUID. Не может быть null.")
    private UUID productId;
    @Schema(description = "Добавляемое кол-во продукта. Не может быть null. Должно быть больше или равно нулю.")
    private BigDecimal addedQuantity;

    public ProductAddedQuantityRequest() {

    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getAddedQuantity() {
        return addedQuantity;
    }

    public void setAddedQuantity(BigDecimal addedQuantity) {
        this.addedQuantity = addedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAddedQuantityRequest that = (ProductAddedQuantityRequest) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(addedQuantity, that.addedQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, addedQuantity);
    }

    @Override
    public String toString() {
        return "ProductChangeQuantityRequest{" +
                "id=" + productId +
                ", newQuantity=" + addedQuantity +
                '}';
    }

}

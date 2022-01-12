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

    @Schema(description = "Уникальный идентификатор продукта в формате UUID")
    private UUID productId;
    @Schema(description = "Уникальный идентификатор пользователя в формате UUID")
    private UUID userId;
    @Schema(description = "Добавляемое кол-во продукта")
    private BigDecimal addedQuantity;

    public ProductAddedQuantityRequest() {

    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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
                Objects.equals(userId, that.userId) &&
                Objects.equals(addedQuantity, that.addedQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, userId, addedQuantity);
    }

    @Override
    public String toString() {
        return "ProductChangeQuantityRequest{" +
                "id=" + productId +
                ", userId=" + userId +
                ", newQuantity=" + addedQuantity +
                '}';
    }

}

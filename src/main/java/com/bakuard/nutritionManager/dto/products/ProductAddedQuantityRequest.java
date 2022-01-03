package com.bakuard.nutritionManager.dto.products;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ProductAddedQuantityRequest {

    private UUID productId;
    private UUID userId;
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

package com.bakuard.nutritionManager.dto.products;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Указывает на сколько нужно уменьшить кол-во указанного продукта имеющегося в наличии у указанного
        пользователя.
        """)
public class ProductTakeQuantityRequest {

    @Schema(description = """
            Уникальный идентификатор продукта в формате UUID. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Продукт с таким идентификатором должен существовать в БД. <br/>
            """)
    private UUID productId;
    @Schema(description = """
            Отнимаемое кол-во продукта. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Не должно быть отрицательным. <br/>
            """)
    private BigDecimal takeQuantity;

    public ProductTakeQuantityRequest() {

    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getTakeQuantity() {
        return takeQuantity;
    }

    public void setTakeQuantity(BigDecimal takeQuantity) {
        this.takeQuantity = takeQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductTakeQuantityRequest that = (ProductTakeQuantityRequest) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(takeQuantity, that.takeQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, takeQuantity);
    }

    @Override
    public String toString() {
        return "ProductTakeQuantityRequest{" +
                "id=" + productId +
                ", takeQuantity=" + takeQuantity +
                '}';
    }

}

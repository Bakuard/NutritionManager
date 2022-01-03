package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.dishes.DishForMenuProductsListRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MenuProductsListRequest {

    private BigDecimal servingNumber;
    private BigDecimal totalPrice;
    private List<DishForMenuProductsListRequest> productIndexes;

    public MenuProductsListRequest() {

    }

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public void setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<DishForMenuProductsListRequest> getProductIndexes() {
        return productIndexes;
    }

    public void setProductIndexes(List<DishForMenuProductsListRequest> productIndexes) {
        this.productIndexes = productIndexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuProductsListRequest that = (MenuProductsListRequest) o;
        return Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(productIndexes, that.productIndexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servingNumber, totalPrice, productIndexes);
    }

    @Override
    public String toString() {
        return "MenuProductsListRequest{" +
                "servingNumber=" + servingNumber +
                ", totalPrice=" + totalPrice +
                ", productIndexes=" + productIndexes +
                '}';
    }

}

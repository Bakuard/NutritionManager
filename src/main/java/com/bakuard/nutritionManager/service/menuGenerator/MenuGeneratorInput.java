package com.bakuard.nutritionManager.service.menuGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuGeneratorInput {

    public record ProductConstraint(String category, String condition, BigDecimal quantity) {}

    public record DishConstraint(String dishTag, String condition, BigDecimal quantity) {}

    private String menuName;
    private BigDecimal maxPrice;
    private BigDecimal servingNumber;
    private final List<ProductConstraint> productConstraints;
    private final List<DishConstraint> dishConstraints;

    private MenuGeneratorInput() {
        productConstraints = new ArrayList<>();
        dishConstraints = new ArrayList<>();
    }

    public MenuGeneratorInput setMenuName(String menuName) {
        this.menuName = menuName;
        return this;
    }

    public MenuGeneratorInput setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public MenuGeneratorInput setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
        return this;
    }

    public MenuGeneratorInput addProductConstraint(String category, String condition, BigDecimal quantity) {
        productConstraints.add(new ProductConstraint(category, condition, quantity));
        return this;
    }

    public MenuGeneratorInput addDishConstraint(String dishTag, String condition, BigDecimal quantity) {
        dishConstraints.add(new DishConstraint(dishTag, condition, quantity));
        return this;
    }

    public String getMenuName() {
        return menuName;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public List<ProductConstraint> getProductConstraints() {
        return productConstraints;
    }

    public List<DishConstraint> getDishConstraints() {
        return dishConstraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuGeneratorInput that = (MenuGeneratorInput) o;
        return Objects.equals(menuName, that.menuName) &&
                Objects.equals(maxPrice, that.maxPrice) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(productConstraints, that.productConstraints) &&
                Objects.equals(dishConstraints, that.dishConstraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuName, maxPrice, servingNumber, productConstraints, dishConstraints);
    }

    @Override
    public String toString() {
        return "MenuGeneratorInput{" +
                "menuName='" + menuName + '\'' +
                ", maxPrice=" + maxPrice +
                ", servingNumber=" + servingNumber +
                ", productConstraints=" + productConstraints +
                ", dishConstraints=" + dishConstraints +
                '}';
    }


}

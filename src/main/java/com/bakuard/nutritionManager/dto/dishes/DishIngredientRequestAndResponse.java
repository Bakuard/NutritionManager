package com.bakuard.nutritionManager.dto.dishes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class DishIngredientRequestAndResponse {

    private String name;
    private BigDecimal quantity;
    private List<DishIngredientFilterRequestResponse> filter;

    public DishIngredientRequestAndResponse() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public List<DishIngredientFilterRequestResponse> getFilter() {
        return filter;
    }

    public void setFilter(List<DishIngredientFilterRequestResponse> filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredientRequestAndResponse that = (DishIngredientRequestAndResponse) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, filter);
    }

    @Override
    public String toString() {
        return "DishIngredientRequest{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", filter=" + filter +
                '}';
    }

}

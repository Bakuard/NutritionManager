package com.bakuard.nutritionManager.dto.dishes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DishForMenuProductsListResponse {

    private UUID dishId;
    private String name;
    private List<Integer> productIndexes;

    public DishForMenuProductsListResponse() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getProductIndexes() {
        return productIndexes;
    }

    public void setProductIndexes(List<Integer> productIndexes) {
        this.productIndexes = productIndexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishForMenuProductsListResponse that = (DishForMenuProductsListResponse) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(productIndexes, that.productIndexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, name, productIndexes);
    }

    @Override
    public String toString() {
        return "DishForMenuProductsListResponse{" +
                "dishId=" + dishId +
                ", name='" + name + '\'' +
                ", productIndexes=" + productIndexes +
                '}';
    }

}

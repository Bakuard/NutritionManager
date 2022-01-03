package com.bakuard.nutritionManager.dto.dishes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DishForMenuProductsListRequest {

    private UUID dishId;
    private List<Integer> indexes;

    public DishForMenuProductsListRequest() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Integer> indexes) {
        this.indexes = indexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishForMenuProductsListRequest that = (DishForMenuProductsListRequest) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(indexes, that.indexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, indexes);
    }

    @Override
    public String toString() {
        return "DishForMenuProductsListRequest{" +
                "dishId=" + dishId +
                ", indexes=" + indexes +
                '}';
    }

}

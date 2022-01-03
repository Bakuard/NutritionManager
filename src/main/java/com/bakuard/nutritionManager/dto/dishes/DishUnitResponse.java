package com.bakuard.nutritionManager.dto.dishes;

import java.util.Objects;
import java.util.UUID;

public class DishUnitResponse {

    private UUID id;
    private String name;

    public DishUnitResponse() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishUnitResponse that = (DishUnitResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "DishUnitResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}

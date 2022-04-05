package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.FieldResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные о тегах и единицах измерения кол-ва всех блюд конкретного пользователя (без дубликатов).
        """)
public class DishFieldsResponse {

    @Schema(description = "Единицы измерения кол-ва всех блюд пользователя")
    private List<FieldResponse> units;
    @Schema(description = "Все теги всех блюд пользователя")
    private List<FieldResponse> tags;

    public DishFieldsResponse() {

    }

    public List<FieldResponse> getUnits() {
        return units;
    }

    public void setUnits(List<FieldResponse> units) {
        this.units = units;
    }

    public List<FieldResponse> getTags() {
        return tags;
    }

    public void setTags(List<FieldResponse> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishFieldsResponse that = (DishFieldsResponse) o;
        return Objects.equals(units, that.units) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(units, tags);
    }

    @Override
    public String toString() {
        return "DishFieldsResponse{" +
                "units=" + units +
                ", tags=" + tags +
                '}';
    }

}

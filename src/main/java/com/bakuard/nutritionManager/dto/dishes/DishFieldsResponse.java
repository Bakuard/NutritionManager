package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.FieldResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные о тегах, единицах измерения кол-ва и наименованиях всех блюд конкретного пользователя
         (без дубликатов).
        """)
public class DishFieldsResponse {

    @Schema(description = "Единицы измерения кол-ва всех блюд пользователя")
    private List<FieldResponse> units;
    @Schema(description = "Все теги всех блюд пользователя")
    private List<FieldResponse> tags;
    @Schema(description = "Наименования всех блюд пользователя")
    private List<FieldResponse> names;

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

    public List<FieldResponse> getNames() {
        return names;
    }

    public void setNames(List<FieldResponse> names) {
        this.names = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishFieldsResponse response = (DishFieldsResponse) o;
        return Objects.equals(units, response.units) &&
                Objects.equals(tags, response.tags) &&
                Objects.equals(names, response.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(units, tags, names);
    }

    @Override
    public String toString() {
        return "DishFieldsResponse{" +
                "units=" + units +
                ", tags=" + tags +
                ", names=" + names +
                '}';
    }

}

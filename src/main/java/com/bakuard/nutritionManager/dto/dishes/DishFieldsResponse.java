package com.bakuard.nutritionManager.dto.dishes;

import com.bakuard.nutritionManager.dto.FieldResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные о наименованиях и единицах измерения кол-ва всех блюд конкретного пользователя (без дубликатов).
        """)
public class DishFieldsResponse {

    @Schema(description = "Наименования всех блюд пользователя")
    private List<FieldResponse> names;
    @Schema(description = "Все теги всех блюд пользователя")
    private List<FieldResponse> tags;

    public DishFieldsResponse() {

    }

    public List<FieldResponse> getNames() {
        return names;
    }

    public void setNames(List<FieldResponse> names) {
        this.names = names;
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
        return Objects.equals(names, that.names) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names, tags);
    }

    @Override
    public String toString() {
        return "DishFieldsResponse{" +
                "names=" + names +
                ", tags=" + tags +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Наименования и теги всех меню конкретного пользователя (без дубликатов).
        """)
public class MenuFieldsResponse {

    @Schema(description = "Наименования всех меню пользователя.")
    private List<String> names;
    @Schema(description = "Теги всех меню пользователя.")
    private List<String> tags;

    public MenuFieldsResponse() {

    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuFieldsResponse that = (MenuFieldsResponse) o;
        return Objects.equals(names, that.names) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names, tags);
    }

    @Override
    public String toString() {
        return "MenuFieldsResponse{" +
                "names=" + names +
                ", tags=" + tags +
                '}';
    }

}

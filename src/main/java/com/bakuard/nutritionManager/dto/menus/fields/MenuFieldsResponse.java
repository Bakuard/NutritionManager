package com.bakuard.nutritionManager.dto.menus.fields;

import com.bakuard.nutritionManager.dto.FieldResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Наименования и теги всех меню конкретного пользователя.
        """)
public class MenuFieldsResponse {

    @Schema(description = """
            Наименования всех меню пользователя. Если у пользователя нет ни одного меню - данный список будет
             пустым.
            """)
    private List<FieldResponse> menuNames;
    @Schema(description = """
            Теги всех меню пользователя. Если ни одного меню этого пользователя нет ни одного тега - данный список
             будет пустым.
            """)
    private List<FieldResponse> menuTags;
    @Schema(description = """
            Наименования всех блюд пользователя. Если у пользователя нет ни одного блюда - данный список будет
             пустым.
            """)
    private List<FieldResponse> dishNames;

    public MenuFieldsResponse() {

    }

    public List<FieldResponse> getMenuNames() {
        return menuNames;
    }

    public void setMenuNames(List<FieldResponse> menuNames) {
        this.menuNames = menuNames;
    }

    public List<FieldResponse> getMenuTags() {
        return menuTags;
    }

    public void setMenuTags(List<FieldResponse> menuTags) {
        this.menuTags = menuTags;
    }

    public List<FieldResponse> getDishNames() {
        return dishNames;
    }

    public void setDishNames(List<FieldResponse> dishNames) {
        this.dishNames = dishNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuFieldsResponse that = (MenuFieldsResponse) o;
        return Objects.equals(menuNames, that.menuNames) &&
                Objects.equals(menuTags, that.menuTags) &&
                Objects.equals(dishNames, that.dishNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuNames, menuTags, dishNames);
    }

    @Override
    public String toString() {
        return "MenuFieldsResponse{" +
                "menuNames=" + menuNames +
                ", menuTags=" + menuTags +
                ", dishNames=" + dishNames +
                '}';
    }

}

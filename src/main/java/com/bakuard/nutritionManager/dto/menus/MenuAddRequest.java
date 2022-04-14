package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Данные используемые в запросе на добавление меню
        """)
public class MenuAddRequest {

    @Schema(description = "Наименование данного меню. Не может быть null.")
    private String name;
    @Schema(description = "Путь к изображению данного меню")
    private String imageUrl;
    @Schema(description = "Описание данного меню")
    private String description;
    @Schema(description = "Блюда входящие в данное меню. Не может быть null. Допускается пустой список блюд.")
    private List<MenuItemRequestResponse> items;
    @Schema(description = """
            Теги указанные для данного меню. Не может быть null. Допускается пустой список тегов.
             Ограничения для тегов: все теги должны быть уникальны, теги не могут иметь значение null и
             обязаны содержать как минимум один отображаемый символ.
            """)
    private List<String> tags;

    public MenuAddRequest() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MenuItemRequestResponse> getItems() {
        return items;
    }

    public void setItems(List<MenuItemRequestResponse> items) {
        this.items = items;
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
        MenuAddRequest that = (MenuAddRequest) o;
        return Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(items, that.items) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl, name, description, items, tags);
    }

    @Override
    public String toString() {
        return "MenuAddRequest{" +
                "imageUrl='" + imageUrl + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", items=" + items +
                ", tags=" + tags +
                '}';
    }

}

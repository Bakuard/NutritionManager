package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Данные используемые в запросе на обновление меню. Не допускаетс, чтобы у пользвователя было два и более
         меню с одинаковым наименованием.
        """)
public class MenuUpdateRequest {

    @Schema(description = """
            Уникальный идентификатор меню в формате UUID. Ограничения:<br/>
            1. Не может быть null.<br/>
            """)
    private UUID id;
    @Schema(description = """
            Наименование данного меню. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Должно содержать как минимум один отображаемый символ. <br/>
            """)
    private String name;
    @Schema(description = """
            Путь к изображению данного меню. Ограничения: <br/>
            1. Должен быть null ИЛИ представлять собой корректный URl адресс.<br/>
            """)
    private String imageUrl;
    @Schema(description = "Описание данного меню.")
    private String description;
    @Schema(description = """
            Блюда входящие в данное меню. Допускается пустой список блюд. Ограничения:<br/>
            1. Список не может быть null. <br/>
            2. Все элементы списка не должны быть null. <br/>
            3. Два и более элемента меню не могут относиться к одному и тому же блюду. <br/>
            """)
    private List<ItemUpdateRequest> items;
    @Schema(description = """
            Теги указанные для данного меню. Допускается пустой список тегов. Ограничения:<br/>
            1. Список тегов не может быть null.<br/>
            2. Все теги должны быть уникальны.<br/>
            3. Теги не могут иметь значение null.<br/>
            4. Теги обязаны содержать как минимум один отображаемый символ.<br/>
            """)
    private List<String> tags;

    public MenuUpdateRequest() {

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

    public List<ItemUpdateRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemUpdateRequest> items) {
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
        MenuUpdateRequest that = (MenuUpdateRequest) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(description, that.description) &&
                Objects.equals(items, that.items) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, imageUrl, description, items, tags);
    }

    @Override
    public String toString() {
        return "MenuUpdateRequest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", items=" + items +
                ", tags=" + tags +
                '}';
    }

}

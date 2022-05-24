package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.users.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Возвращаемые подробные данные о меню.
        """)
public class MenuResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Всегда имеет значение Menu.")
    private String type;
    @Schema(description = "Уникальный идентификатор меню.")
    private UUID id;
    @Schema(description = "Данные пользователя, которому принадлежит меню.")
    private UserResponse user;
    @Schema(description = "Наименование меню.")
    private String name;
    @Schema(description = "Путь к изображению данного меню.")
    private URL imageUrl;
    @Schema(description = "Описание данного меню.")
    private String description;
    @Schema(description = "Блюда входящие в состав данного меню.")
    private List<ItemResponse> items;
    @Schema(description = "Теги данного меню. Если для меню не заданно ни одного тега - данный список будет пустым.")
    private List<String> tags;

    public MenuResponse() {
        type = "Menu";
    }

    public String getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ItemResponse> getItems() {
        return items;
    }

    public void setItems(List<ItemResponse> items) {
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
        MenuResponse that = (MenuResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(name, that.name) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(description, that.description) &&
                Objects.equals(items, that.items) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, name, imageUrl, description, items, tags);
    }

    @Override
    public String toString() {
        return "MenuResponse{" +
                "id=" + id +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", items=" + items +
                ", tags=" + tags +
                '}';
    }

}

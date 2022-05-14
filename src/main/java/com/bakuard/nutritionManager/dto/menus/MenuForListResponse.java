package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.users.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Возвращаемые данные о меню используемые в списке меню.
        """)
public class MenuForListResponse {

    @Schema(description = "Поле указывающее тип данного объекта. Имеет значение Menu.")
    private String type;
    @Schema(description = "Уникальный идентификатор меню.")
    private UUID id;
    @Schema(description = "Данные пользователя, которому принадлежит меню.")
    private UserResponse user;
    @Schema(description = "Наименование меню.")
    private String name;
    @Schema(description = "Средняя цена меню.")
    private BigDecimal averagePrice;
    @Schema(description = "Путь к изображению данного меню.")
    private String imageUrl;
    @Schema(description = "Блюда входящие в состав данного меню.")
    private List<MenuItemRequestResponse> items;
    @Schema(description = "Теги данного меню. Если для меню не заданно ни одного тега - данный список будет пустым.")
    private List<String> tags;

    public MenuForListResponse() {
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

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        MenuForListResponse that = (MenuForListResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(user, that.user) &&
                Objects.equals(name, that.name) &&
                Objects.equals(averagePrice, that.averagePrice) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(items, that.items) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, name, averagePrice, imageUrl, items, tags);
    }

    @Override
    public String toString() {
        return "MenuForListResponse{" +
                "id=" + id +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", averagePrice=" + averagePrice +
                ", imageUrl='" + imageUrl + '\'' +
                ", items=" + items +
                ", tags=" + tags +
                '}';
    }

}

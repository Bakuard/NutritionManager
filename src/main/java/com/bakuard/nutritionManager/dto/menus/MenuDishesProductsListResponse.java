package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.dishes.DishProductsResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Возвращаемый список продуктов каждого ингредиента каждого блюд этого меню.
        """)
public class MenuDishesProductsListResponse {

    @Schema(description = "Уникальный идентификатор меню.")
    private UUID menuId;
    @Schema(description = "Наименование меню")
    private String menuName;
    @Schema(description = "Кол-во меню")
    private BigDecimal menuQuantity;
    @Schema(description = """
            Список продуктов для каждого ингредиента каждого блюда данного меню. Особые случаи: <br/>
            1. Если меню не содержит ни одного блюда - данный список будет пустым.
            """)
    private List<DishProductsResponse> dishesProducts;

    public MenuDishesProductsListResponse() {

    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public BigDecimal getMenuQuantity() {
        return menuQuantity;
    }

    public void setMenuQuantity(BigDecimal menuQuantity) {
        this.menuQuantity = menuQuantity;
    }

    public List<DishProductsResponse> getDishesProducts() {
        return dishesProducts;
    }

    public void setDishesProducts(List<DishProductsResponse> dishesProducts) {
        this.dishesProducts = dishesProducts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDishesProductsListResponse that = (MenuDishesProductsListResponse) o;
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(menuName, that.menuName) &&
                Objects.equals(menuQuantity, that.menuQuantity) &&
                Objects.equals(dishesProducts, that.dishesProducts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, menuName, menuQuantity, dishesProducts);
    }

    @Override
    public String toString() {
        return "MenuDishesProductsListResponse{" +
                "menuId=" + menuId +
                ", menuName='" + menuName + '\'' +
                ", menuQuantity=" + menuQuantity +
                ", dishesProducts=" + dishesProducts +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.menus;

import com.bakuard.nutritionManager.dto.dishes.DishProductsResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
        Возвращаемый список продуктов каждого ингредиента одного из блюд этого меню.
        """)
public class MenuDishProductsListResponse {

    @Schema(description = "Уникальный идентификатор меню.")
    private UUID menuId;
    @Schema(description = "Наименование меню")
    private String menuName;
    @Schema(description = "Кол-во меню")
    private BigDecimal menuQuantity;
    @Schema(description = """
            Список наименований всех блюд входящих в это меню и соответствующих уникльных идентификаторов
             этих блюд. Особые случаи: <br/>
            1. Если меню не содержит ни одного блюда - данный список будет пустым.
            """)
    private List<DishNameAndIdResponse> dishes;
    @Schema(description = """
            Список продуктов для каждого ингредиента выбранного блюда данного меню. Особые случаи: <br/>
            1. Если меню не содержит ни одного блюда - данное поле будет иметь значение null.
            """)
    private DishProductsResponse dishProducts;

    public MenuDishProductsListResponse() {

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

    public List<DishNameAndIdResponse> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishNameAndIdResponse> dishes) {
        this.dishes = dishes;
    }

    public DishProductsResponse getDishProducts() {
        return dishProducts;
    }

    public void setDishProducts(DishProductsResponse dishProducts) {
        this.dishProducts = dishProducts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuDishProductsListResponse that = (MenuDishProductsListResponse) o;
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(menuName, that.menuName) &&
                Objects.equals(menuQuantity, that.menuQuantity) &&
                Objects.equals(dishes, that.dishes) &&
                Objects.equals(dishProducts, that.dishProducts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, menuName, menuQuantity, dishes, dishProducts);
    }

    @Override
    public String toString() {
        return "MenuDishProductsListResponse{" +
                "menuId=" + menuId +
                ", menuName='" + menuName + '\'' +
                ", menuQuantity=" + menuQuantity +
                ", dishes=" + dishes +
                ", dishProducts=" + dishProducts +
                '}';
    }

}

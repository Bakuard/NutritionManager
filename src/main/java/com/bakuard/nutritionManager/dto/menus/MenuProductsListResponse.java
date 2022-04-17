package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MenuProductsListResponse {

    @Schema(description = "Уникальный идентфикатор меню. Не может быть null.")
    private UUID menuId;
    @Schema(description = "Кол-во данного меню на которое рассчитывается список докупаемых продуктов. Не может быть null.")
    private BigDecimal quantity;
    @Schema(description = """
            Общая стоимость всех докупаемых продуктов с учетом кол-ва порций меню. Если все блюда меню не
             содержат ни одного ингредиента или всем ингредиентам всех блюд не соответствует ни один продукт -
             принимает значение null.
            """)
    private BigDecimal totalPrice;
    @Schema(description = "Краткие данные о блюдах входящих в данное меню.")
    private List<DishOfMenuProductListResponse> dishNames;
    @Schema(description = """
            Каждый элемент списка содержит подробные данные о продукте необходимом для приготовления
             некоторых блюд из этого меню.
            """)
    private List<ProductAsMenuItemResponse> products;

    public MenuProductsListResponse() {

    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<DishOfMenuProductListResponse> getDishNames() {
        return dishNames;
    }

    public void setDishNames(List<DishOfMenuProductListResponse> dishNames) {
        this.dishNames = dishNames;
    }

    public List<ProductAsMenuItemResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductAsMenuItemResponse> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuProductsListResponse that = (MenuProductsListResponse) o;
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(totalPrice, that.totalPrice) &&
                Objects.equals(dishNames, that.dishNames) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, quantity, totalPrice, dishNames, products);
    }

    @Override
    public String toString() {
        return "MenuProductsListResponse{" +
                "menuId=" + menuId +
                ", number=" + quantity +
                ", totalPrice=" + totalPrice +
                ", dishNames=" + dishNames +
                ", ingredients=" + products +
                '}';
    }

}

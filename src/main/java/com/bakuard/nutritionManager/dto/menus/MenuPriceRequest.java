package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на получение списка продуктов для меню")
public class MenuPriceRequest {

    @Schema(description = "Уникальный идентфикатор меню. Не может быть null.")
    private UUID menuId;
    @Schema(description = "Кол-во данного меню на которое рассчитывается список докупаемых продуктов. Не может быть null.")
    private BigDecimal quantity;
    @Schema(description = """
            Каждый элемент этого списка указывает - какой продукт выбрать для конкретного ингредиента одного
             из блюд меню. Если для одного из ингредиентов блюда не указан продукт, то в качестве значения по
             умолчанию будет выбран самый дешевый из всех продуктов соответствующих данному ингредиенту данного
             блюда. Если для одного и того же ингредиента данного блюда указанно несколько продуктов, то будет
             выбран последний продукт указанный в данном списке. Данный список может быть пустым. Не должен
             принимать значение null.
            """)
    private List<DishProductRequest> products;

    public MenuPriceRequest() {

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

    public List<DishProductRequest> getProducts() {
        return products;
    }

    public void setProducts(List<DishProductRequest> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuPriceRequest that = (MenuPriceRequest) o;
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, quantity, products);
    }

    @Override
    public String toString() {
        return "MenuProductsListRequest{" +
                "menuId=" + menuId +
                ", quantity=" + quantity +
                ", products=" + products +
                '}';
    }

}

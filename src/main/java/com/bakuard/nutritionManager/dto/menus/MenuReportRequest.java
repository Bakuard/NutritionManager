package com.bakuard.nutritionManager.dto.menus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Schema(description = "Данные запроса на получение отчета по докупаемым продуктам к меню.")
public class MenuReportRequest {

    @Schema(description = """
            Уникальный идентификатор меню в формате UUID. Ограничения: <br/>
            1. Не может быть null. <br/>
            """)
    private UUID menuId;
    @Schema(description = """
            Кол-во меню для которого рассчитывается стоимость. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Значение должно быть больше 0. <br/>
            """)
    private BigDecimal menuNumber;
    @Schema(description = """
            Каждый элемент этого списка указывает - какой продукт выбрать для конкретного ингредиента одного
             из блюд меню. Данный список может быть пустым. <br/>
            Ограничения:<br/>
            1. Данный список не может быть null. <br/>
            2. Все значения этого списка не должны быть null. <br/>
            
            Особые случаи:<br/>
            1. Если для одного из ингредиентов блюда не указан продукт, то в качестве значения по
             умолчанию будет выбран самый дешевый из всех продуктов соответствующих данному ингредиенту данного
             блюда. <br/>
            2. Если для одного и того же ингредиента данного блюда указанно несколько продуктов, то будет
             выбран первый продукт указанный в данном списке. <br/>
            """)
    private List<DishProductRequest> products;

    public MenuReportRequest() {

    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public BigDecimal getMenuNumber() {
        return menuNumber;
    }

    public void setMenuNumber(BigDecimal menuNumber) {
        this.menuNumber = menuNumber;
    }

    public Optional<List<DishProductRequest>> getProducts() {
        return Optional.ofNullable(products);
    }

    public MenuReportRequest setProducts(List<DishProductRequest> products) {
        this.products = products;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuReportRequest that = (MenuReportRequest) o;
        return Objects.equals(menuId, that.menuId) &&
                Objects.equals(menuNumber, that.menuNumber) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, menuNumber, products);
    }

    @Override
    public String toString() {
        return "MenuProductsListRequest{" +
                "menuId=" + menuId +
                ", menuNumber=" + menuNumber +
                ", products=" + products +
                '}';
    }

}

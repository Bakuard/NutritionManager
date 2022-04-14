package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на получение списка продуктов для блюда")
public class DishProductsListRequest {

    @Schema(description = "Уникальный идентфикатор блюда. Не может быть null.")
    private UUID dishId;
    @Schema(description = "Кол-во порций блюда на которое рассчитывается список докупаемых продуктов. Не может быть null.")
    private BigDecimal servingNumber;
    @Schema(description = """
            Каждый элемент этого списка указывает - какой продукт выбрать для конкретного ингредиента блюда. Если
             для одного из ингредиентов блюда не указан продукт, то в качестве значения по умолчанию будет выбран
             самый дешевый из всех продуктов соответствующих данному ингредиенту. Если для одного и того же
             ингредиента указанно несколько продуктов, то будет выбран последний продукт указанный в данном списке.
             Данный список может быть пустым. Не должен принимать значение null.
            """)
    private List<DishIngredientProductRequest> products;

    public DishProductsListRequest() {

    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public BigDecimal getServingNumber() {
        return servingNumber;
    }

    public void setServingNumber(BigDecimal servingNumber) {
        this.servingNumber = servingNumber;
    }

    public List<DishIngredientProductRequest> getProducts() {
        return products;
    }

    public void setProducts(List<DishIngredientProductRequest> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishProductsListRequest that = (DishProductsListRequest) o;
        return Objects.equals(dishId, that.dishId) &&
                Objects.equals(servingNumber, that.servingNumber) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishId, servingNumber, products);
    }

    @Override
    public String toString() {
        return "DishProductListRequest{" +
                "dishId=" + dishId +
                ", servingNumber=" + servingNumber +
                ", ingredients=" + products +
                '}';
    }

}

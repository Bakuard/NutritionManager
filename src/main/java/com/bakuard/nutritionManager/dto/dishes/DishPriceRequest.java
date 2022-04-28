package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные запроса на получение стоимости блюда")
public class DishPriceRequest {

    @Schema(description = """
            Уникальный идентификатор блюда в формате UUID. Ограничения: <br/>
            1. Не может быть null. <br/>
            """)
    private UUID dishId;
    @Schema(description = """
            Кол-во порций блюда для которого рассчитывается стоимость. Ограничения: <br/>
            1. Не может быть null. <br/>
            2. Значение должно быть больше 0. <br/>
            """)
    private BigDecimal servingNumber;
    @Schema(description = """
            Каждый элемент этого списка указывает - какой продукт выбрать для конкретного ингредиента блюда.
             Данный список может быть пустым. <br/>
            Ограничения: <br/>
            1. Данный список не может быть null. <br/>
            2. Все значения этого списка не должны быть null. <br/>
            
            Особые случаи: <br/>
            1. Если для одного из ингредиентов блюда не указан продукт, то в качестве значения по умолчанию будет выбран
             самый дешевый из всех продуктов соответствующих данному ингредиенту. <br/>
            2. Если для одного и того же ингредиента указанно несколько продуктов, то будет выбран первый продукт
             указанный в данном списке. <br/>
            """)
    private List<DishIngredientProductRequest> products;

    public DishPriceRequest() {

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
        DishPriceRequest that = (DishPriceRequest) o;
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

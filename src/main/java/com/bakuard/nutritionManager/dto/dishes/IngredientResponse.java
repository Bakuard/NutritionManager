package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные ингредиента блюда")
public class IngredientResponse {

    @Schema(description = """
            Уникальный идентификатор ингредиента блюда.
            """)
    private UUID id;
    @Schema(description = """
            Порядковый номер ингредиента в списке ингредиентов блюда.
            """)
    private int index;
    @Schema(description = """
            Кол-во данного ингредиента необходимого для приготовления одной порции блюда. Ограничения:<br/>
            1. Не может быть null. <br/>
            2. Должен быть больше нуля. <br/>
            """)
    private BigDecimal quantity;
    @Schema(description = """
            Фильтр задающий множество взаимозаменяемых продуктов для данного ингредиента. Ограничения:<br/>
            1. Не может быть null. <br/>
            """)
    private IngredientFilterRequestResponse filter;

    public IngredientResponse() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public IngredientFilterRequestResponse getFilter() {
        return filter;
    }

    public void setFilter(IngredientFilterRequestResponse filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientResponse that = (IngredientResponse) o;
        return index == that.index &&
                Objects.equals(id, that.id) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, index, quantity, filter);
    }

    @Override
    public String toString() {
        return "IngredientResponse{" +
                "id=" + id +
                ", index=" + index +
                ", quantity=" + quantity +
                ", filter=" + filter +
                '}';
    }

}

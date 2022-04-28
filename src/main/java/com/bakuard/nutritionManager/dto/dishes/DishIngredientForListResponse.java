package com.bakuard.nutritionManager.dto.dishes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = """
        Содержит данные о всех продуктах соответствующих одному конкретному ингредиенту блюда.
        """)
public class DishIngredientForListResponse {

    @Schema(description = "Индекс текущего ингредиента.")
    private int ingredientIndex;
    @Schema(description = "Категория к которой относятся все продукты данного ингредиента.")
    private String productCategory;
    @Schema(description = """
            Список всех докупаемых продуктов данного ингредиента. Особые случаи: <br/>
            1. Если данному ингредиенту не соответстует ни один продукт, то данный список будет пустым. <br/>
            """)
    private List<ProductAsDishIngredientResponse> products;

    public DishIngredientForListResponse() {

    }

    public int getIngredientIndex() {
        return ingredientIndex;
    }

    public void setIngredientIndex(int ingredientIndex) {
        this.ingredientIndex = ingredientIndex;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public List<ProductAsDishIngredientResponse> getProducts() {
        return products;
    }

    public void setProducts(List<ProductAsDishIngredientResponse> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishIngredientForListResponse that = (DishIngredientForListResponse) o;
        return ingredientIndex == that.ingredientIndex &&
                Objects.equals(productCategory, that.productCategory) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientIndex, productCategory, products);
    }

    @Override
    public String toString() {
        return "DishIngredientForListResponse{" +
                "ingredientIndex=" + ingredientIndex +
                ", productCategory='" + productCategory + '\'' +
                ", products=" + products +
                '}';
    }

}

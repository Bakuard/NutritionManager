package com.bakuard.nutritionManager.service.menuGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Представляет собой набор ограничений используемый при подборе нового меню
 * (подробнее см. {@link MenuGeneratorService#generate(MenuGeneratorInput, UUID)}).
 */
public class MenuGeneratorInput {

    /**
     * Ограничение на минимальное или максимальное кол-во продуктов одной конкретной категории.
     * @param category категория продуктов.
     * @param condition отношение "больше" или "меньше".
     * @param quantity кол-во проудктов.
     */
    public record ProductConstraint(String category, String condition, BigDecimal quantity) {}

    /**
     * Ограничение на минимальное или максимальное кол-во блюд с указанным тегом.
     * @param dishTag тег блюда.
     * @param condition отношение "больше" или "меньше".
     * @param quantity кол-во блюда.
     */
    public record DishConstraint(String dishTag, String condition, BigDecimal quantity) {}

    private String menuName;
    private BigDecimal maxPrice;
    private int minMealsNumber;
    private BigDecimal servingNumberPerMeal;
    private final List<ProductConstraint> productConstraints;
    private final List<DishConstraint> dishConstraints;

    public MenuGeneratorInput() {
        productConstraints = new ArrayList<>();
        dishConstraints = new ArrayList<>();
    }

    /**
     * Устанавливает наименование для подбираемого меню.
     * @param menuName наименование для подбираемого меню.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput setMenuName(String menuName) {
        this.menuName = menuName;
        return this;
    }

    /**
     * Устанавливает максимально допустую стоимость меню, которая расчитвается как суммарная стоимость всех
     * продуктов необходимых для приготовелния блюд этого меню.
     * @param maxPrice максимально допустимая стоимость меню.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    /**
     * Устанавливает минимальное кол-во приемов пищи на одного человека.
     * @param minMealsNumber минимальное кол-во приемов пищи на одного человека.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput setMinMealsNumber(int minMealsNumber) {
        this.minMealsNumber = minMealsNumber;
        return this;
    }

    /**
     * Устанавливает кол-во порций в одном приеме пищи.
     * @param servingNumberPerMeal кол-во порций в одном приеме пищи.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput setServingNumberPerMeal(BigDecimal servingNumberPerMeal) {
        this.servingNumberPerMeal = servingNumberPerMeal;
        return this;
    }

    /**
     * Добавляет ограничение на минимальное или максимальное кол-во продуктов определенной категории.
     * Подробнее см. {@link ProductConstraint}.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput addProductConstraint(String category, String condition, BigDecimal quantity) {
        productConstraints.add(new ProductConstraint(category, condition, quantity));
        return this;
    }

    /**
     * Добавляет ограничение на минимальное или максимальное кол-во блюд с определенным тегом.
     * Подробнее см. {@link DishConstraint}.
     * @return ссылку на этот же объект.
     */
    public MenuGeneratorInput addDishConstraint(String dishTag, String condition, BigDecimal quantity) {
        dishConstraints.add(new DishConstraint(dishTag, condition, quantity));
        return this;
    }

    public String getMenuName() {
        return menuName;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public int getMinMealsNumber() {
        return minMealsNumber;
    }

    public BigDecimal getServingNumberPerMeal() {
        return servingNumberPerMeal;
    }

    public List<ProductConstraint> getProductConstraints() {
        return productConstraints;
    }

    public List<DishConstraint> getDishConstraints() {
        return dishConstraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuGeneratorInput that = (MenuGeneratorInput) o;
        return minMealsNumber == that.minMealsNumber &&
                Objects.equals(menuName, that.menuName) &&
                Objects.equals(maxPrice, that.maxPrice) &&
                Objects.equals(servingNumberPerMeal, that.servingNumberPerMeal) &&
                Objects.equals(productConstraints, that.productConstraints) &&
                Objects.equals(dishConstraints, that.dishConstraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuName, maxPrice, minMealsNumber, servingNumberPerMeal,
                productConstraints, dishConstraints);
    }

    @Override
    public String toString() {
        return "MenuGeneratorInput{" +
                "menuName='" + menuName + '\'' +
                ", maxPrice=" + maxPrice +
                ", minMealsNumber=" + minMealsNumber +
                ", servingNumberPerMeal=" + servingNumberPerMeal +
                ", productConstraints=" + productConstraints +
                ", dishConstraints=" + dishConstraints +
                '}';
    }

}

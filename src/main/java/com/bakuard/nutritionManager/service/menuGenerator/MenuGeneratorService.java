package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.validation.*;

public class MenuGeneratorService {

    public MenuGeneratorService() {

    }

    /**
     * Подбирает новое меню. При подборе нового меню используется критерий минимальной суммарной стоимости
     * продуктов необходимых для приготовления всех блюд этого меню. Входными данными для данного метода является
     * набор ограничений передаваемых этому методу в виде объекта input.
     * @param input набор ограничений для подбираемого меню (подробнее см. {@link MenuGeneratorInput}).
     * @return новое меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Метод {@link MenuGeneratorInput#getMenuName()} объекта input возвращает null. <br/>
     *         2. Метод {@link MenuGeneratorInput#getMenuName()} объекта input возвращает наименование уже
     *            существующего меню. <br/>
     *         3. Метод {@link MenuGeneratorInput#getMaxPrice()} объекта input возвращает null. <br/>
     *         4. Метод {@link MenuGeneratorInput#getMaxPrice()} объекта input возвращает отрицательное значение. <br/>
     *         5. Метод {@link MenuGeneratorInput#getMinMealsNumber()} объекта input возвращает null. <br/>
     *         5. Метод {@link MenuGeneratorInput#getMinMealsNumber()} объекта input возвращает ноль или
     *            отрицательное значение. <br/>
     *         6. Метод {@link MenuGeneratorInput#getServingNumberPerMeal()} объекта input возвращает null. <br/>
     *         7. Метод {@link MenuGeneratorInput#getServingNumberPerMeal()} объекта input возвращает ноль или
     *            отрицательное значение. <br/>
     *         8. Если метод {@link MenuGeneratorInput.ProductConstraint#category()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает null. <br/>
     *         9. Если метод {@link MenuGeneratorInput.ProductConstraint#category()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает несуществующую категорию
     *            продуктов. <br/>
     *         10. Если метод {@link MenuGeneratorInput.ProductConstraint#condition()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает null. <br/>
     *         11. Если метод {@link MenuGeneratorInput.ProductConstraint#condition()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает значение не принадлежащее
     *            множеству {"lessOrEqual", "greaterOrEqual"}. <br/>
     *         12. Если метод {@link MenuGeneratorInput.ProductConstraint#quantity()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает null. <br/>
     *         13. Если метод {@link MenuGeneratorInput.ProductConstraint#quantity()} одного из элементов списка
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает отрицательное значение. <br/>
     *         14. Если метод {@link MenuGeneratorInput.DishConstraint#dishTag()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает null. <br/>
     *         15. Если метод {@link MenuGeneratorInput.DishConstraint#dishTag()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает несуществующий тег. <br/>
     *         16. Если метод {@link MenuGeneratorInput.DishConstraint#condition()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает null. <br/>
     *         17. Если метод {@link MenuGeneratorInput.DishConstraint#condition()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает значение не принадлежащее
     *            множеству {"lessOrEqual", "greaterOrEqual"}. <br/>
     *         18. Если метод {@link MenuGeneratorInput.DishConstraint#quantity()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает null. <br/>
     *         19. Если метод {@link MenuGeneratorInput.DishConstraint#quantity()} одного из элементов списка
     *            {@link MenuGeneratorInput#getDishConstraints()} возвращает отрицательное значение. <br/>
     *         20. Если невозможно подобрать меню с заданными ограничениями. <br/>
     */
    public Menu generate(MenuGeneratorInput input) {
        return null;
    }
    
}

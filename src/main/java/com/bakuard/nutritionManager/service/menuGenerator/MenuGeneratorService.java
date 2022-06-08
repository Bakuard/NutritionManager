package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.filters.AnyFilter;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.*;

import java.math.BigDecimal;
import java.util.*;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class MenuGeneratorService {

    private DishRepository dishRepository;
    private MenuRepository menuRepository;

    public MenuGeneratorService(DishRepository dishRepository,
                                MenuRepository menuRepository) {
        this.dishRepository = dishRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * Подбирает новое меню. При подборе нового меню используется критерий минимальной суммарной стоимости
     * продуктов необходимых для приготовления всех блюд этого меню. Входными данными для данного метода является
     * набор ограничений передаваемых этому методу в виде объекта input. <br/><br/>
     * <strong>ВАЖНО!</strong> В задачи этого метода НЕ входит сохранение созданного им меню.
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
     *            {@link MenuGeneratorInput#getProductConstraints()} возвращает категорию продуктов, которая не
     *            используется ни для одного блюда. <br/>
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
     *         20. Если input равен null. <br/>
     *         21. Если у пользователя нет ни одного блюда. <br/>
     *         22. Если у всем ингредиентам всех блюд пользователя не соответствует ин один продукт. <br/>
     *         23. Если невозможно подобрать меню с заданными ограничениями. <br/>
     *         24. Если userId равен null. <br/>
     */
    public Menu generate(MenuGeneratorInput input, UUID userId) {
        //Валидация и подготовка входных данных
        List<Dish> allUserDishes = getAllUserDishes(userId);
        Validator.check("MenuGeneratorService.allUserDishes", notEmpty(allUserDishes));

        Map<Dish, BigDecimal> dishMinPrices = getDishMinPrices(allUserDishes);
        Validator.check("MenuGeneratorService.dishMinPrices", notEmpty(dishMinPrices));

        Validator.check(
                "MenuGeneratorService.input", notNull(input),
                "MenuGeneratorService.userId", notNull(userId)
        );
        Validator.check(
                "MenuGeneratorInput.menuName", notNull(input.getMenuName()).
                and(() -> isTrue(menuRepository.getMenusNumber(menuByName(userId, input.getMenuName())) == 0)),
                "MenuGeneratorInput.maxPrice", notNull(input.getMaxPrice()).
                        and(() -> notNegative(input.getMaxPrice())),
                "MenuGeneratorInput.minMeals", notNull(input.getMinMealsNumber()).
                        and(() -> positiveValue(input.getMinMealsNumber())),
                "MenuGeneratorInput.servingNumber", notNull(input.getServingNumberPerMeal()).
                        and(() -> positiveValue(input.getServingNumberPerMeal())),
                "MenuGeneratorInput.products", doesNotThrows(input.getProductConstraints(),
                        (pc) -> Validator.check(
                                "MenuGeneratorInput.product.category", notNull(pc.category()).
                                and(() -> anyMatch(allUserDishes, dish -> containsCategory(pc.category(), dish))),
                                "MenuGeneratorInput.product.condition", notNull(pc.condition()).
                                        and(() -> anyMatch(List.of("lessOrEqual", "greaterOrEqual"), pc.condition())),
                                "MenuGeneratorInput.product.quantity", notNull(pc.quantity()).
                                        and(() -> notNegative(pc.quantity()))
                        )),
                "MenuGeneratorInput.dishes", doesNotThrows(input.getDishConstraints(),
                        (dc) -> Validator.check(
                                "MenuGeneratorInput.dish.dishTag", notNull(dc.dishTag()).
                                and(() -> anyMatch(allUserDishes, dish -> containsTag(dish, dc.dishTag()))),
                                "MenuGeneratorInput.dish.condition", notNull(dc.condition()).
                                        and(() -> anyMatch(List.of("lessOrEqual", "greaterOrEqual"), dc.condition())),
                                "MenuGeneratorInput.dish.quantity", notNull(dc.quantity()).
                                        and(() -> notNegative(dc.quantity()))
                        ))
        );

        //Генерация меню


        return null;
    }


    private List<Dish> getAllUserDishes(UUID userId) {
        List<Dish> result = new ArrayList<>();

        PageableByNumber pageable = PageableByNumber.of(30, 0);
        Criteria criteria = new Criteria().
                setFilter(Filter.user(userId)).
                setSort(Sort.dishDefaultSort()).
                setPageable(pageable);

        Page<Dish> page = dishRepository.getDishes(criteria);
        while(!page.getMetadata().isLast()) {
            result.addAll(page.getContent());

            pageable = pageable.next();
            criteria.setPageable(pageable);
            page = dishRepository.getDishes(criteria);
        }
        result.addAll(page.getContent());

        return result;
    }

    private Map<Dish, BigDecimal> getDishMinPrices(List<Dish> allUserDishes) {
        final HashMap<Dish, BigDecimal> result = new HashMap<>();

        for(Dish dish : allUserDishes) {
            dish.getMinPrice().ifPresent(minPrice -> result.put(dish, minPrice));
        }

        return result;
    }

    private boolean containsCategory(String productCategory, Dish dish) {
        return dish.getIngredients().stream().
                anyMatch(ingredient ->
                        ((AnyFilter)ingredient.getFilter().findAny(Filter.Type.CATEGORY)).
                                getValues().
                                contains(productCategory)
                );
    }

    private boolean containsTag(Dish dish, String tag) {
        return dish.getTags().stream().anyMatch(t -> t.getValue().equals(tag));
    }

    private Criteria menuByName(UUID userId, String menuName) {
        return new Criteria().
                setFilter(
                        Filter.and(
                                Filter.user(userId),
                                Filter.anyMenu(menuName)
                        )
                );
    }

}

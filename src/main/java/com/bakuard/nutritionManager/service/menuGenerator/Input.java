package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.DishIngredient;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.filters.AnyFilter;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.bakuard.nutritionManager.validation.Rule.*;

/**
 * Представляет собой набор ограничений используемый при подборе нового меню
 * (подробнее см. {@link MenuGeneratorService#generate(Input)}).
 */
public class Input {

    /**
     * Данные о мнимальной стоимости каждого из блюд пользователя.
     * @param dish блюдо.
     * @param minPrice минимальная стиомость блюда.
     */
    public record DishMinPrice(Dish dish, BigDecimal minPrice) {}

    /**
     * Ограничение на минимальное или максимальное кол-во продуктов одной конкретной категории.
     * @param productCategory категория продуктов.
     * @param relation отношение "больше" или "меньше".
     * @param quantity кол-во проудктов.
     */
    public record ProductConstraint(String productCategory, Relationship relation, BigDecimal quantity) {}

    /**
     * Ограничение на минимальное или максимальное кол-во блюд с указанным тегом.
     * @param dishTag тег блюда.
     * @param relation отношение "больше" или "меньше".
     * @param quantity кол-во блюда.
     */
    public record DishTagConstraint(Tag dishTag, Relationship relation, BigDecimal quantity) {}

    /**
     * Данные о кол-во продукта указанной категории необходимого для приготвления одной порции указанного блюда.
     * @param dish блюдо.
     * @param productCategory категория продукта.
     * @param quantity кол-во продукта указанной категории.
     */
    public record ProductQuantity(Dish dish, String productCategory, BigDecimal quantity) {}

    /**
     * Указывает - содержит ли указанное блюдо указанный тег.
     * @param dish блюдо.
     * @param tag тег.
     * @param contain true - если блюдо содержит указанный тег, false - в противном случае.
     */
    public record DishTag(Dish dish, Tag tag, boolean contain) {}


    private ImmutableList<DishMinPrice> dishMinPrices;
    private ImmutableList<ProductConstraint> productConstraints;
    private ImmutableList<DishTagConstraint> dishTagConstraints;
    private ImmutableList<ProductQuantity> productQuantities;
    private ImmutableList<DishTag> dishTags;
    private BigDecimal minServingNumber;
    private BigDecimal maxPrice;
    private String generatedMenuName;

    private Input(List<ProductConstraintRaw> productConstraints,
                  List<DishConstraintRaw> dishTagConstraints,
                  int minMealsNumber,
                  BigDecimal servingNumberPerMeal,
                  BigDecimal maxPrice,
                  String generatedMenuName,
                  DishRepository dishRepository,
                  MenuRepository menuRepository,
                  UUID userId) {
        Validator.check(
                "Input.dishRepository", notNull(dishRepository),
                "Input.menuRepository", notNull(menuRepository),
                "Input.userId", notNull(userId)
        );

        dishMinPrices = ImmutableList.copyOf(getAllUserDishes(userId, dishRepository));
        Validator.check("Input.allUserDishes", notEmpty(dishMinPrices));

        Set<Tag> allTags = getAllDishesTag(dishMinPrices);
        Set<String> allProductCategories = getAllProductCategories(dishMinPrices);

        Validator.check(
                "Input.generatedMenuName", notNull(generatedMenuName).
                        and(() -> isTrue(menuRepository.getMenusNumber(menuByName(userId, generatedMenuName)) == 0)),
                "Input.maxPrice", notNull(maxPrice).
                        and(() -> notNegative(maxPrice)),
                "Input.minMeals", notNull(minMealsNumber).
                        and(() -> positiveValue(minMealsNumber)),
                "Input.servingNumberPerMeal", notNull(servingNumberPerMeal).
                        and(() -> positiveValue(servingNumberPerMeal)),
                "Input.products", doesNotThrows(productConstraints,
                        (pc) -> Validator.check(
                                "Input.product.category", notNull(pc.category()).
                                        and(() -> anyMatch(allProductCategories, pc.category())),
                                "Input.product.condition", notNull(pc.relation()).
                                        and(() -> anyMatch(List.of("lessOrEqual", "greaterOrEqual"), pc.relation())),
                                "Input.product.quantity", notNull(pc.quantity()).
                                        and(() -> notNegative(pc.quantity()))
                        )),
                "Input.dishes", doesNotThrows(dishTagConstraints,
                        (dc) -> Validator.check(
                                "Input.dish.dishTag", notNull(dc.dishTag()).
                                        and(() -> anyMatch(allTags, tag -> tag.getValue().equals(dc.dishTag()))),
                                "Input.dish.condition", notNull(dc.relation()).
                                        and(() -> anyMatch(List.of("lessOrEqual", "greaterOrEqual"), dc.relation())),
                                "Input.dish.quantity", notNull(dc.quantity()).
                                        and(() -> notNegative(dc.quantity()))
                        ))
        );

        this.generatedMenuName = generatedMenuName;
        this.maxPrice = maxPrice;
        minServingNumber = servingNumberPerMeal.add(BigDecimal.valueOf(minMealsNumber));
        this.productConstraints = productConstraints.stream().
                map(pc -> new Input.ProductConstraint(pc.category(), toRelation(pc.relation()), pc.quantity())).
                collect(ImmutableList.toImmutableList());
        this.dishTagConstraints = dishTagConstraints.stream().
                map(dtc -> new Input.DishTagConstraint(new Tag(dtc.dishTag()), toRelation(dtc.dishTag()), dtc.quantity())).
                collect(ImmutableList.toImmutableList());

    }

    public ImmutableList<DishMinPrice> getDishMinPrices() {
        return dishMinPrices;
    }

    public ImmutableList<ProductConstraint> getProductConstraints() {
        return productConstraints;
    }

    public ImmutableList<DishTagConstraint> getDishTagConstraints() {
        return dishTagConstraints;
    }

    public BigDecimal getMinServingNumber() {
        return minServingNumber;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public String getGeneratedMenuName() {
        return generatedMenuName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Input input = (Input) o;
        return dishMinPrices.equals(input.dishMinPrices) &&
                productConstraints.equals(input.productConstraints) &&
                dishTagConstraints.equals(input.dishTagConstraints) &&
                minServingNumber.equals(input.minServingNumber) &&
                maxPrice.equals(input.maxPrice) &&
                generatedMenuName.equals(input.generatedMenuName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishMinPrices, productConstraints, dishTagConstraints,
                minServingNumber, maxPrice, generatedMenuName);
    }

    @Override
    public String toString() {
        return "Input{" +
                "dishMinPrices=" + dishMinPrices +
                ", productConstraints=" + productConstraints +
                ", dishTagConstraints=" + dishTagConstraints +
                ", minServingNumber=" + minServingNumber +
                ", maxPrice=" + maxPrice +
                ", generatedMenuName='" + generatedMenuName + '\'' +
                '}';
    }

    
    private List<DishMinPrice> getAllUserDishes(UUID userId, DishRepository dishRepository) {
        List<DishMinPrice> result = new ArrayList<>();

        PageableByNumber pageable = PageableByNumber.of(30, 0);
        Criteria criteria = new Criteria().
                setFilter(Filter.user(userId)).
                setSort(Sort.dishDefaultSort()).
                setPageable(pageable);

        Page<Dish> page = dishRepository.getDishes(criteria);
        while(!page.getMetadata().isLast()) {
            for(Dish dish : page.getContent()) {
                dish.getMinPrice().ifPresent(minPrice -> result.add(new DishMinPrice(dish, minPrice)));
            }

            pageable = pageable.next();
            criteria.setPageable(pageable);
            page = dishRepository.getDishes(criteria);
        }
        for(Dish dish : page.getContent()) {
            dish.getMinPrice().ifPresent(minPrice -> result.add(new DishMinPrice(dish, minPrice)));
        }

        return result;
    }

    private Set<DishTag> getAllDishesTag(List<DishMinPrice> dishMinPrices) {
        return dishMinPrices.stream().
                flatMap(
                        dmp -> dmp.dish().getTags().stream().
                                map(t -> new DishTag(dmp.dish(), t))
                ).
    }

    private Set<String> getAllProductCategories(List<DishMinPrice> dishMinPrices) {
        return dishMinPrices.stream().
                flatMap(dmp -> dmp.dish().getIngredients().stream()).
                map(ingredient -> (AnyFilter)ingredient.getFilter().findAny(Filter.Type.CATEGORY)).
                filter(Objects::nonNull).
                flatMap(filter -> filter.getValues().stream()).
                collect(Collectors.toSet());
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

    private Relationship toRelation(String relation) {
        Relationship result = Relationship.GREATER_OR_EQUAL;
        if("lessOrEqual".equals(relation)) {
            result = Relationship.LESS_OR_EQUAL;
        }
        return result;
    }


    private record ProductConstraintRaw(String category, String relation, BigDecimal quantity) {}

    private record DishConstraintRaw(String dishTag, String relation, BigDecimal quantity) {}

    public static class Builder {

        private UUID userId;
        private String generatedMenuName;
        private BigDecimal maxPrice;
        private int minMealsNumber;
        private BigDecimal servingNumberPerMeal;
        private final List<ProductConstraintRaw> productConstraints;
        private final List<DishConstraintRaw> dishConstraints;

        public Builder() {
            productConstraints = new ArrayList<>();
            dishConstraints = new ArrayList<>();
        }

        /**
         * Устанавливает идентификатор пользователя из блюд которого будет генерирвоаться новое меню.
         * @param userId идентификатор пользователя.
         * @return ссылку на этот же объект.
         */
        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Устанавливает наименование для подбираемого меню.
         * @param generatedMenuName наименование для подбираемого меню.
         * @return ссылку на этот же объект.
         */
        public Builder setGeneratedMenuName(String generatedMenuName) {
            this.generatedMenuName = generatedMenuName;
            return this;
        }

        /**
         * Устанавливает максимально допустую стоимость меню, которая расчитвается как суммарная стоимость всех
         * продуктов необходимых для приготовелния блюд этого меню.
         * @param maxPrice максимально допустимая стоимость меню.
         * @return ссылку на этот же объект.
         */
        public Builder setMaxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        /**
         * Устанавливает минимальное кол-во приемов пищи на одного человека.
         * @param minMealsNumber минимальное кол-во приемов пищи на одного человека.
         * @return ссылку на этот же объект.
         */
        public Builder setMinMealsNumber(int minMealsNumber) {
            this.minMealsNumber = minMealsNumber;
            return this;
        }

        /**
         * Устанавливает кол-во порций в одном приеме пищи.
         * @param servingNumberPerMeal кол-во порций в одном приеме пищи.
         * @return ссылку на этот же объект.
         */
        public Builder setServingNumberPerMeal(BigDecimal servingNumberPerMeal) {
            this.servingNumberPerMeal = servingNumberPerMeal;
            return this;
        }

        /**
         * Добавляет ограничение на минимальное или максимальное кол-во продуктов определенной категории.
         * @return ссылку на этот же объект.
         */
        public Builder addProductConstraint(String category, String relation, BigDecimal quantity) {
            productConstraints.add(new ProductConstraintRaw(category, relation, quantity));
            return this;
        }

        /**
         * Добавляет ограничение на минимальное или максимальное кол-во блюд с определенным тегом.
         * @return ссылку на этот же объект.
         */
        public Builder addDishConstraint(String dishTag, String relation, BigDecimal quantity) {
            dishConstraints.add(new DishConstraintRaw(dishTag, relation, quantity));
            return this;
        }

        /**
         * Создает и возвращает набор входных данных для генерации нового меню.
         * @param dishRepository репозиторий блюд.
         * @param menuRepository репозиторий меню.
         * @return новое меню.
         * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
         *         1. Если generatedMenuName является null. <br/>
         *         2. Если Уже существует меню с тем же наименованием, что и значение generatedMenuName. <br/>
         *         3. Если maxPrice является null. <br/>
         *         4. Если maxPrice является отрицательным значением. <br/>
         *         5. Если minMealsNumber является null. <br/>
         *         5. Если minMealsNumber отрицательное значение или равен нулю. <br/>
         *         6. Если servingNumberPerMeal является null. <br/>
         *         7. Если servingNumberPerMeal отрицательное значение или равен нулю. <br/>
         *         8. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *            значения category было задано null. <br/>
         *         9. Если для одного из ограничений на кол-во продуктов определенной категории было задано
         *            значение category, которое не используется ни для одного блюда пользователя. <br/>
         *         10. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значения relation было задано null<br/>
         *         11. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значения relation было задано значение НЕ принадлежащее множеству
         *             {"lessOrEqual", "greaterOrEqual"}. <br/>
         *         12. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значение quantity было задано null. <br/>
         *         13. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значение quantity было задано отрицательное значение. <br/>
         *         14. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения dishTag было задано null. <br/>
         *         15. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения dishTag был задан несуществующий тег. <br/>
         *         16. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения relation было задано null<br/>
         *         17. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения relation было задано значение НЕ принадлежащее множеству
         *             {"lessOrEqual", "greaterOrEqual"}. <br/>
         *         18. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значение quantity было задано null. <br/>
         *         19. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значение quantity было задано отрицательное значение. <br/>
         *         20. Если у пользователя нет ни одного блюда. <br/>
         *         21. Если у всем ингредиентам всех блюд пользователя не соответствует ин один продукт. <br/>
         *         23. Если невозможно подобрать меню с заданными ограничениями. <br/>
         *         24. Если userId равен null. <br/>
         *         25. Если dishRepository равен null. <br/>
         *         26. Если menuRepository равен null. <br/>
         */
        public Input build(DishRepository dishRepository,
                           MenuRepository menuRepository) {
            return new Input(
                    productConstraints,
                    dishConstraints,
                    minMealsNumber,
                    servingNumberPerMeal,
                    maxPrice,
                    generatedMenuName,
                    dishRepository,
                    menuRepository,
                    userId
            );
        }

    }

}

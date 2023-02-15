package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
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

import static com.bakuard.nutritionManager.validation.Rule.*;

/**
 * Представляет собой набор ограничений используемый при подборе нового меню
 * (подробнее см. {@link MenuGeneratorService#generate(Input)}).
 */
public class Input {

    /**
     * Данные о минимальной стоимости каждого из блюд пользователя.
     * @param dish блюдо.
     * @param minPrice минимальная стоимость блюда.
     */
    public record DishMinPrice(Dish dish, BigDecimal minPrice) {}

    /**
     * Ограничение на минимальное или максимальное кол-во продуктов одной конкретной категории.
     * @param productCategory категория продуктов.
     * @param relation отношение "больше" или "меньше".
     * @param quantity кол-во продуктов.
     */
    public record ProductConstraint(String productCategory, Relationship relation, BigDecimal quantity) {}

    /**
     * Ограничение на минимальное или максимальное кол-во блюд с указанным тегом.
     * @param dishTag тег блюда.
     * @param relation отношение "больше" или "меньше".
     * @param quantity кол-во блюда.
     */
    public record DishTagConstraint(Tag dishTag, Relationship relation, BigDecimal quantity) {}


    private record ProductQuantity(Dish dish, String productCategory, BigDecimal quantity) {};

    private record ProductConstraintRaw(String category, String relation, BigDecimal quantity) {}

    private record DishTagConstraintRaw(String dishTag, String relation, BigDecimal quantity) {}


    private ImmutableList<DishMinPrice> dishMinPrices;
    private ImmutableList<ProductConstraint> productConstraints;
    private ImmutableList<DishTagConstraint> dishTagConstraints;

    private ImmutableList<ProductQuantity> productQuantities;
    private BigDecimal minServingNumber;
    private String generatedMenuName;
    private User user;

    private Input(List<ProductConstraintRaw> productConstraints,
                  List<DishTagConstraintRaw> dishTagConstraints,
                  int minMealsNumber,
                  BigDecimal servingNumberPerMeal,
                  String generatedMenuName,
                  DishRepository dishRepository,
                  MenuRepository menuRepository,
                  User user) {
        Validator.check(
                "Input.dishRepository", notNull(dishRepository),
                "Input.menuRepository", notNull(menuRepository),
                "Input.user", notNull(user)
        );

        this.dishMinPrices = ImmutableList.copyOf(getAllDishes(user.getId(), dishRepository));
        Validator.check("Input.allUserDishes", notEmpty(dishMinPrices));

        List<Tag> allTags = getAllDishesTag(dishMinPrices);
        List<String> allProductCategories = getAllProductCategories(dishMinPrices);

        Validator.check(
                "Input.generatedMenuName", notNull(generatedMenuName).
                        and(() -> notBlank(generatedMenuName)).
                        and(() -> isTrue(menuRepository.getMenusNumber(
                                menuByName(user.getId(), generatedMenuName)) == 0)),
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
        this.minServingNumber = servingNumberPerMeal.multiply(BigDecimal.valueOf(minMealsNumber));
        this.dishTagConstraints = getAllDishTagConstraints(dishTagConstraints, allTags);
        this.productQuantities = getProductQuantities(allProductCategories, dishMinPrices);
        this.productConstraints = getAllProductConstraints(productConstraints, allProductCategories);
        this.user = user;
    }

    /**
     * Возвращает данные о минимальной стоимости для каждого блюда пользователя.
     * @return данные о минимальной стоимости для каждого блюда пользователя.
     */
    public ImmutableList<DishMinPrice> getAllDishMinPrices() {
        return dishMinPrices;
    }

    /**
     * Возвращает ограничения на общее кол-во продуктов определенных категорий. Если при создании данного
     * объекта для некоторых категорий продуктов используемых в хотя бы одном из блюд не было задано ограничение,
     * то для данной категории продукта по умолчанию будет добавлено следующее ограничение: кол-во данного тега >= 0
     * @return список ограничений на общее кол-во продуктов определенных категорий.
     */
    public ImmutableList<ProductConstraint> getConstraintsByAllProducts() {
        return productConstraints;
    }

    /**
     * Возвращает ограничения на кол-во блюд с определенным тегом. Если при создании данного объекта для
     * некоторого тега не было задано ограничение, то для данного тега по умолчанию будет добавлено следующее
     * ограничение: кол-во данного тега >= 0
     * @return список ограничений на кол-во блюд с определенным тегом.
     */
    public ImmutableList<DishTagConstraint> getConstraintsByAllDishTags() {
        return dishTagConstraints;
    }

    /**
     * Возвращает кол-во продукта указанной категории необходимого для приготовления одной порции
     * указанного блюда. Если для указанного блюда не нужны продукты указанной категории - возвращает 0.
     * @param dish блюдо.
     * @param productCategory категория продуктов.
     * @return кол-во продукта указанной категории.
     */
    public BigDecimal getQuantity(Dish dish, String productCategory) {
        return productQuantities.stream().
                filter(pq -> pq.dish().equals(dish) && pq.productCategory().equals(productCategory)).
                findFirst().
                map(ProductQuantity::quantity).
                orElse(BigDecimal.ZERO);
    }

    /**
     * Проверяет - содержит ли указанное блюдо указанный тег. Если это так - возвращает true, иначе - false.
     * @param dish блюдо.
     * @param tag тег.
     * @return true - если указанное блюдо содержит указанный тег, иначе - false.
     */
    public boolean hasTag(Dish dish, Tag tag) {
        return dish.contains(tag);
    }

    /**
     * Возвращает минимально необходимое общее кол-во всех блюд для генерируемого меню. Рассчитывается как
     * произведение минимально необходимого кол-ва приемов пищи на одного человека и минимального кол-ва порций
     * блюд в одном приеме пищи.
     * @return минимально необходимое общее кол-во всех блюд для генерируемого меню.
     */
    public BigDecimal getMinServingNumber() {
        return minServingNumber;
    }

    /**
     * Возвращает наименование для генерируемого меню.
     * @return наименование для генерируемого меню.
     */
    public String getGeneratedMenuName() {
        return generatedMenuName;
    }

    /**
     * Возвращает пользователя из данных которого составлены данные ограничения.
     * @return пользователь.
     */
    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Input input = (Input) o;
        return dishMinPrices.equals(input.dishMinPrices) &&
                productConstraints.equals(input.productConstraints) &&
                dishTagConstraints.equals(input.dishTagConstraints) &&
                productQuantities.equals(input.productQuantities) &&
                minServingNumber.equals(input.minServingNumber) &&
                generatedMenuName.equals(input.generatedMenuName) &&
                user.equals(input.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishMinPrices, productConstraints, dishTagConstraints, productQuantities,
                minServingNumber, generatedMenuName, user);
    }

    @Override
    public String toString() {
        return "Input{" +
                "dishMinPrices=" + dishMinPrices +
                ", productConstraints=" + productConstraints +
                ", dishTagConstraints=" + dishTagConstraints +
                ", productQuantities=" + productQuantities +
                ", minServingNumber=" + minServingNumber +
                ", generatedMenuName='" + generatedMenuName + '\'' +
                ", user=" + user +
                '}';
    }


    private List<DishMinPrice> getAllDishes(UUID userId, DishRepository dishRepository) {
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

    private List<Tag> getAllDishesTag(List<DishMinPrice> dishMinPrices) {
        return dishMinPrices.stream().
                flatMap(dmp -> dmp.dish().getTags().stream()).
                distinct().
                toList();
    }

    private ImmutableList<DishTagConstraint> getAllDishTagConstraints(List<DishTagConstraintRaw> tagConstraints,
                                                                      List<Tag> allTags) {
        List<DishTagConstraint> result = new ArrayList<>();

        for(Tag tag : allTags) {
            List<DishTagConstraintRaw> match = tagConstraints.stream().
                    filter(dtc -> dtc.dishTag().equals(tag.getValue())).
                    toList();
            if(match.isEmpty()) {
                result.add(new DishTagConstraint(tag, Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO));
            } else {
                match.forEach(dtc ->
                        result.add(new DishTagConstraint(tag, toRelation(dtc.relation()), dtc.quantity()))
                );
            }
        }

        return ImmutableList.copyOf(result);
    }

    private List<String> getAllProductCategories(List<DishMinPrice> dishMinPrices) {
        return dishMinPrices.stream().
                flatMap(dmp -> dmp.dish().getIngredients().stream()).
                map(ingredient -> ingredient.getFilter().<AnyFilter>findAny(Filter.Type.CATEGORY)).
                filter(Optional::isPresent).
                map(filter -> filter.orElseThrow().getValues().get(0)).
                distinct().
                toList();
    }

    private ImmutableList<ProductQuantity> getProductQuantities(List<String> allProductCategories,
                                                                List<DishMinPrice> dishMinPrices) {
        List<ProductQuantity> result = new ArrayList<>();

        for(String category : allProductCategories) {
            for(DishMinPrice minPrice : dishMinPrices) {
                ProductQuantity productQuantity = minPrice.dish().getIngredients().stream().
                        filter(ingredient -> {
                            return ingredient.getFilter().
                                    <AnyFilter>findAny(Filter.Type.CATEGORY).
                                    map(filter -> filter.getValues().get(0).equals(category)).
                                    orElse(false);
                        }).
                        findFirst().
                        map(ingredient ->
                                new ProductQuantity(minPrice.dish(),
                                        category,
                                        ingredient.getNecessaryQuantity(BigDecimal.ONE))
                        ).
                        orElse(new ProductQuantity(minPrice.dish(), category, BigDecimal.ZERO));

                result.add(productQuantity);
            }
        }

        return ImmutableList.copyOf(result);
    }

    private ImmutableList<ProductConstraint> getAllProductConstraints(List<ProductConstraintRaw> productConstraints,
                                                                      List<String> allProductCategories) {
        List<ProductConstraint> result = new ArrayList<>();

        for(String category : allProductCategories) {
            List<ProductConstraintRaw> match = productConstraints.stream().
                    filter(pcr -> pcr.category().equals(category)).
                    toList();
            if(match.isEmpty()) {
                result.add(new ProductConstraint(category, Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO));
            } else {
                match.forEach(pcr ->
                        result.add(new ProductConstraint(category, toRelation(pcr.relation()), pcr.quantity()))
                );
            }
        }

        return ImmutableList.copyOf(result);
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


    public static class Builder {

        private User user;
        private String generatedMenuName;
        private int minMealsNumber;
        private BigDecimal servingNumberPerMeal;
        private final List<ProductConstraintRaw> productConstraints;
        private final List<DishTagConstraintRaw> dishConstraints;
        private DishRepository dishRepository;
        private MenuRepository menuRepository;

        public Builder() {
            productConstraints = new ArrayList<>();
            dishConstraints = new ArrayList<>();
        }

        /**
         * Устанавливает пользователя из блюд которого будет генерироваться новое меню.
         * @param user пользователь.
         * @return ссылку на этот же объект.
         */
        public Builder setUser(User user) {
            this.user = user;
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
            dishConstraints.add(new DishTagConstraintRaw(dishTag, relation, quantity));
            return this;
        }

        /**
         * Устанавливает репозиторий блюд.
         * @param dishRepository репозиторий блюд.
         * @return ссылку на этот же объект.
         */
        public Builder setDishRepository(DishRepository dishRepository) {
            this.dishRepository = dishRepository;
            return this;
        }

        /**
         * Устанавливает репозиторий меню.
         * @param menuRepository репозиторий меню.
         * @return ссылку на этот же объект.
         */
        public Builder setMenuRepository(MenuRepository menuRepository) {
            this.menuRepository = menuRepository;
            return this;
        }

        /**
         * Создает и возвращает набор входных данных для генерации нового меню.
         * @return новое меню.
         * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
         *         1. Если generatedMenuName является null. <br/>
         *         2. Если Уже существует меню с тем же наименованием, что и значение generatedMenuName. <br/>
         *         3. Если minMealsNumber отрицательное значение или равен нулю. <br/>
         *         4. Если servingNumberPerMeal является null. <br/>
         *         5. Если servingNumberPerMeal отрицательное значение или равен нулю. <br/>
         *         6. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *            значения category было задано null. <br/>
         *         7. Если для одного из ограничений на кол-во продуктов определенной категории было задано
         *            значение category, которое не используется ни для одного блюда пользователя. <br/>
         *         8. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значения relation было задано null<br/>
         *         9. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значения relation было задано значение НЕ принадлежащее множеству
         *             {"lessOrEqual", "greaterOrEqual"}. <br/>
         *         10. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значение quantity было задано null. <br/>
         *         11. Если для одного из ограничений на кол-во продуктов определенной категории, в качестве
         *             значение quantity было задано отрицательное значение. <br/>
         *         12. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения dishTag было задано null. <br/>
         *         13. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения dishTag был задан несуществующий тег. <br/>
         *         14. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения relation было задано null<br/>
         *         15. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значения relation было задано значение НЕ принадлежащее множеству
         *             {"lessOrEqual", "greaterOrEqual"}. <br/>
         *         16. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значение quantity было задано null. <br/>
         *         17. Если для одного из ограничений на кол-во блюд с определенным тегом, в качестве
         *             значение quantity было задано отрицательное значение. <br/>
         *         18. Если у пользователя нет ни одного блюда. <br/>
         *         19. Если всем ингредиентам всех блюд пользователя не соответствует ни один продукт. <br/>
         *         20. Если невозможно подобрать меню с заданными ограничениями. <br/>
         *         21. Если user равен null. <br/>
         *         22. Если dishRepository равен null. <br/>
         *         23. Если menuRepository равен null. <br/>
         */
        public Input tryBuild() {
            return new Input(
                    productConstraints,
                    dishConstraints,
                    minMealsNumber,
                    servingNumberPerMeal,
                    generatedMenuName,
                    dishRepository,
                    menuRepository,
                    user
            );
        }

    }

}

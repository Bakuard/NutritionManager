package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.filters.UserFilter;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableById;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Представляет оределенное блюдо.
 */
public class Dish implements Entity<Dish> {

    private final UUID id;
    private final User user;
    private String name;
    private BigDecimal servingSize;
    private String unit;
    private String description;
    private URL imageUrl;
    private final List<DishIngredient> ingredients;
    private final List<Tag> tags;

    private AppConfigData config;
    private ProductRepository productRepository;
    private final Sort ingredientProductsSort;

    /**
     * Конструктор копирования. Выполняет глубокое копирование.
     * @param other копируемое блюдо.
     */
    public Dish(Dish other) {
        this.id = other.id;
        this.user = new User(other.user);
        this.name = other.name;
        this.servingSize = other.servingSize;
        this.unit = other.unit;
        this.description = other.description;
        this.imageUrl = other.imageUrl;
        this.config = other.config;
        this.productRepository = other.productRepository;
        this.ingredients = new ArrayList<>(other.ingredients);
        this.tags = new ArrayList<>(other.tags);
        this.ingredientProductsSort = other.ingredientProductsSort;
    }

    private Dish(UUID id,
                 User user,
                 String name,
                 BigDecimal servingSize,
                 String unit,
                 String description,
                 String imageUrl,
                 List<DishIngredient.Builder> ingredients,
                 List<String> tags,
                 AppConfigData config,
                 ProductRepository productRepository) {
        Container<List<Tag>> tagContainer = new Container<>();
        Container<List<DishIngredient>> ingredientContainer = new Container<>();
        Container<URL> urlContainer = new Container<>();

        Validator.check(
                Rule.of("Dish.id").notNull(id),
                Rule.of("Dish.user").notNull(user),
                Rule.of("Dish.name").notNull(name).and(r -> r.notBlank(name)),
                Rule.of("Dish.unit").notNull(unit).and(r -> r.notBlank(unit)),
                Rule.of("Dish.servingSize").notNull(servingSize).and(r -> r.positiveValue(servingSize)),
                Rule.of("Dish.imageUrl").isNull(imageUrl).or(r -> r.isUrl(imageUrl, urlContainer)),
                Rule.of("Dish.ingredients").doesNotThrows(ingredients, DishIngredient.Builder::tryBuild, ingredientContainer).
                        and(r -> {
                            boolean b = ingredientContainer.get().stream().
                                    map(i -> i.getFilter().<UserFilter>findAny(Filter.Type.USER)).
                                    allMatch(u -> u != null && user.getId().equals(u.getUserId()));
                            if(b) return r.success(Constraint.IS_TRUE);
                            else return r.failure(Constraint.IS_TRUE,
                                    "All ingredients must have UserFilter and UserFilter.getUser() must be equal Dish.getUser()");
                        }).
                        and(r -> r.notContainsDuplicate(ingredientContainer.get(), DishIngredient::getName)),
                Rule.of("Dish.tags").doesNotThrows(tags, Tag::new, tagContainer),
                Rule.of("Dish.config").notNull(config),
                Rule.of("Dish.repository").notNull(productRepository)
        );

        this.id = id;
        this.user = user;
        this.name = name.trim();
        this.servingSize = servingSize.setScale(config.getNumberScale(), config.getRoundingMode());
        this.unit = unit.trim();
        this.description = description;
        this.imageUrl = urlContainer.get();
        this.ingredients = ingredientContainer.get();
        this.tags = new ArrayList<>(tagContainer.get());
        this.config = config;
        this.productRepository = productRepository;
        this.ingredientProductsSort = Sort.products().asc("price");
    }

    /**
     * Возвращает уникальный идетификатор данного блюда.
     * @return уникальный идетификатор данного блюда.
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает пользователя, с которым связанно данное блюдо.
     * @return пользователь, с которым связанно данное блюдо.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает наименование даннго блюда.
     * @return наименование даннго блюда.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает размер одной порции блюда.
     * @return размер одной порции блюда.
     */
    public BigDecimal getServingSize() {
        return servingSize;
    }

    /**
     * Возвращает наименование единицы измерения кол-ва для данного блюда.
     * @return наименование единицы измерения кол-ва для данного блюда.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Возвращает описание данного блюда.
     * @return описание данного блюда.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает путь изображения данного блюда. Путь не обязательно может быть путем в файловой системе.
     * @return путь изображения данного блюда.
     */
    public URL getImageUrl() {
        return imageUrl;
    }

    /**
     * Возвращает кол-во ингредиентов данного блюда.
     * @return кол-во ингредиентов данного блюда.
     */
    public int getIngredientNumber() {
        return ingredients.size();
    }

    /**
     * Возвращает кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента. Особые
     * случаи: <br/>
     * 1. Если блюдо не содержит ингредиента с таким индексом - возвращает пустой Optional. <br/>
     * @param ingredientIndex индекс ингредиента для которого рассчитывается кол-во взаимозаменяемых продуктов.
     * @return кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента или пустой Optional.
     */
    public Optional<Integer> getProductsNumber(int ingredientIndex) {
        return getIngredient(ingredientIndex).
                map(
                        ingredient -> productRepository.getProductsNumber(
                                new Criteria().setFilter(ingredient.getFilter())
                        )
                );
    }

    /**
     * Возвращает кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента. Особые
     * случаи: <br/>
     * 1. Если блюдо не содержит ингредиента с таким идентификатором - возвращает пустой Optional. <br/>
     * @param ingredientId уникальный идентификатор ингредиента для которого рассчитывается кол-во взаимозаменяемых продуктов.
     * @return кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента или пустой Optional.
     * @throws ValidateException если ingredientId равен null.
     */
    public Optional<Integer> getProductsNumber(UUID ingredientId) {
        return getIngredient(ingredientId).
                map(
                        ingredient -> productRepository.getProductsNumber(
                                new Criteria().setFilter(ingredient.getFilter())
                        )
                );
    }

    /**
     * Возвращает ингредиент по его индексу. Особые случаи: <br/>
     * 1. Если блюдо не содержит ингредиента с таким индексом - возвращает пустой Optional. <br/>
     * @param ingredientIndex индекс искомого ингредиента.
     * @return ингредиент блюда или пустой Optional.
     */
    public Optional<DishIngredient> getIngredient(int ingredientIndex) {
        DishIngredient ingredient = null;

        if(ingredientIndex >= 0 && ingredientIndex < ingredients.size()) {
            ingredient = ingredients.get(ingredientIndex);
        }

        return Optional.ofNullable(ingredient);
    }

    /**
     * Возвращает ингредиент по его уникальному идентификатору. Особые случаи: <br/>
     * 1. Если блюдо не содержит ингредиента с таким индексом - возвращает пустой Optional. <br/>
     * @param ingredientId уникальный идентификатор ингредиента.
     * @return ингредиент блюда или пустой Optional.
     * @throws ValidateException если ingredientId равен null.
     */
    public Optional<DishIngredient> getIngredient(UUID ingredientId) {
        Validator.check(
                Rule.of("Dish.ingredientId").notNull(ingredientId)
        );

        return ingredients.stream().
                filter(i -> i.getId().equals(ingredientId)).
                findFirst();
    }

    /**
     * Возвращает ингредиент по его индексу.
     * @param ingredientIndex индекс искомого ингредиента.
     * @return ингредиент блюда или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из условий:<br/>
     *                           1. ingredientIndex < нуля.<br/>
     *                           2. ingredientIndex >= кол-ву ингредиентов блюда.
     */
    public DishIngredient tryGetIngredient(int ingredientIndex) {
        Validator.check(
                Rule.of("Dish.ingredientIndex").range(ingredientIndex, 0, ingredients.size() - 1)
        );

        return ingredients.get(ingredientIndex);
    }

    /**
     * Возвращает ингредиент по его уникальному идентификатору.
     * @param ingredientId уникальный идентификатор ингредиента.
     * @return ингредиент блюда или пустой Optional.
     * @throws ValidateException если выполняется хотябы одно из следующих условий: <br/>
     *         1. ingredientId равен null. <br/>
     *         2. У блюда нет ингредиента с таким идентификатором. <br/>
     */
    public DishIngredient tryGetIngredient(UUID ingredientId) {
        return getIngredient(ingredientId).
                orElseThrow(
                        () -> new ValidateException(
                                "Unknown ingredient with id=" + ingredientId + " and dishId=" + id
                        ).addReason(Rule.of("Dish.ingredientId").failure(Constraint.CONTAINS_ITEM))
                );
    }

    /**
     * Возвращает все ингредиенты данного блюда в виде списка доступного только для чтения.
     * @return все ингрединты данного блюда.
     */
    public List<DishIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Возвращает продукт из множества всех взаимозаменяемых продуктов для данного ингредиента по его
     * порядковому номеру (т.е. индексу. Индексация начинается с нуля). Множество продуктов для данного
     * ингредиента упорядоченно по цене в порядке возрастания. Особые случаи:<br/>
     * 1. Если блюдо не содержит ингредиента с индексом ingredientIndex - возвращает пустой Optional. <br/>
     * 2. Если указанный ингредиент не имеет продукта с индексом productIndex - то {@link IngredientProduct#product()}
     *    будет возвращать пустой Optional.<br/>
     * @param ingredientIndex порядковый номер (индекс) ингредиента.
     * @param productIndex порядковый номер (индекс) продукта.
     * @return данные о продукте.
     */
    public Optional<IngredientProduct> getProduct(int ingredientIndex, int productIndex) {
        return getIngredient(ingredientIndex).
                map(ingredient -> {
                    Page<Product> page = getProductPageBy(ingredient, productIndex);
                    Optional<Product> product = page.getByGlobalIndex(BigInteger.valueOf(productIndex));
                    return new IngredientProduct(
                            product,
                            ingredient.getId(),
                            ingredientIndex,
                            productIndex
                    );
                });
    }

    /**
     * Возвращает продукт из множества всех взаимозаменяемых продуктов для данного ингредиента по его
     * идентификатору. Множество продуктов для данного ингредиента упорядоченно по цене в порядке возрастания.
     * Особые случаи:<br/>
     * 1. Если блюдо не содержит ингредиента с идентификатором ingredientId - возвращает пустой Optional. <br/>
     * 2. Если указанный ингредиент не имеет продукта с идентификатором productId - то {@link IngredientProduct#product()}
     *    будет возвращать пустой Optional.<br/>
     * @param ingredientId уникальный идентификатор ингредиента блюда.
     * @param productId уникальный идентификатор продукта.
     * @return данные о продукте.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. ingredientId является null. <br/>
     *         2. productId является null. <br/>
     */
    public Optional<IngredientProduct> getProduct(UUID ingredientId, UUID productId) {
        Validator.check(
                Rule.of("Dish.productId").notNull(productId)
        );

        return getIngredient(ingredientId).
                map(ingredient -> {
                    Page<Product> page = getProductPageBy(ingredient, productId);
                    Optional<Product> product = page.getContent().stream().
                            filter(p -> p.getId().equals(productId)).
                            findFirst();
                    return new IngredientProduct(
                            product,
                            ingredient.getId(),
                            getIngredientIndexBy(ingredient.getId()),
                            page.getGlobalIndexFor(p -> product.isPresent() && p.getId().equals(productId)).
                                    orElse(BigInteger.ONE.negate()).
                                    intValue()
                    );
                });
    }

    /**
     * Возвращает часть выборки продуктов соответствующих указанному ингредиенту. Все продукты в выборке
     * упорядоченны по цене в порядке возрастания. Особые случаи: <br/>
     * 1. Если ингредиенту не соответствует ни один продукт - возвращает пустой объект {@link Page}. <br/>
     * 2. Если среди ингредиентов блюда нет ингредиента с таким индексом - возвращает пустой Optional. <br/>
     * @param ingredientIndex индекс ингредиента.
     * @param pageNumber номер страницы выборки.
     * @return страницу из выборки продуктов соответствующих указанному ингредиенту.
     */
    public Optional<Page<IngredientProduct>> getProducts(int ingredientIndex, int pageNumber) {
        return getIngredient(ingredientIndex).
                map(ingredient -> {
                    Page<Product> productPage =  productRepository.getProducts(
                            new Criteria().
                                    setPageable(PageableByNumber.of(30, pageNumber)).
                                    setSort(ingredientProductsSort).
                                    setFilter(ingredient.getFilter())
                    );

                    return productPage.map(
                            (product, index) ->
                                    new IngredientProduct(
                                            Optional.ofNullable(product),
                                            ingredient.getId(),
                                            ingredientIndex,
                                            index.intValue()
                                    )
                    );
                });
    }

    /**
     * Возвращает часть выборки продуктов соответствующих указанному ингредиенту. Все продукты в выборке
     * упорядоченны по цене в порядке возрастания. Особые случаи: <br/>
     * 1. Если ингредиенту не соответствует ни один продукт - возвращает пустой объект {@link Page}. <br/>
     * 2. Если среди ингредиентов блюда нет ингредиента с таким идентификатором - возвращает пустой Optional. <br/>
     * @param ingredientId уникальный идентификатор ингредиента.
     * @param pageNumber номер страницы выборки.
     * @return страницу из выборки продуктов соответствующих указанному ингредиенту.
     * @throws ValidateException если ingredientId равен null.
     */
    public Optional<Page<IngredientProduct>> getProducts(UUID ingredientId, int pageNumber) {
        return getIngredient(ingredientId).
                map(ingredient -> {
                    Page<Product> productPage =  productRepository.getProducts(
                            new Criteria().
                                    setPageable(PageableByNumber.of(30, pageNumber)).
                                    setSort(ingredientProductsSort).
                                    setFilter(ingredient.getFilter())
                    );

                    return productPage.map(
                            (product, index) ->
                                    new IngredientProduct(
                                            Optional.ofNullable(product),
                                            ingredientId,
                                            getIngredientIndexBy(ingredientId),
                                            index.intValue()
                                    )
                    );
                });
    }

    /**
     * Возвращает данные о выбранном продукте для каждого ингредиента блюда. Индекс элемента в итоговом списке
     * соответствует индексу ингредиента к которому этот элемент относится. Особые случаи:<br/>
     * 1. Если блюдо не содержит ингредиентов - возвращает пустой список. <br/>
     * 2. Если {@link ProductConstraint#ingredientIndex()} некоторого элемента < 0 - этот элемент будет отброшен и
     *    не будет принимать участия в формировании итогового результата. <br/>
     * 3. Если {@link ProductConstraint#ingredientIndex()} некоторого элемента >= кол-ву ингредиентов блюда -
     *    этот элемент будет отброшен и не будет принимать участия в формировании итогового результата. <br/>
     * 4. Если {@link ProductConstraint#productIndex()} некоторого элемента < 0 - этот элемент будет отброшен и
     *    не будет принимать участия в формировании итогового результата. <br/>
     * 5. Если {@link ProductConstraint#productIndex()} некоторого элемента >= кол-ва продуктов соответствующих
     *    этому ингредиенту и при это множество продуктов соответствующих указанному ингредиенту не пусто - этот
     *    элемент будет отброшен и не будет принимать участия в формировании итогового результата. <br/>
     * 6. Если некоторому ингредиенту блюда не соответствует ни один продукт - то для этого ингредиента
     *    будет добавлен элемент, метод {@link IngredientProduct#product()} которого будет возвращаеть пустой
     *    Optional. <br/>
     * 7. Если для одного и того же ингредиента блюда указанно несколько элементов задающих для него продукт - будет
     *    выбран первый корректный элемент из списка. <br/>
     * 8. Если для некоторого ингредиента блюда не указанно ни одного продукта в входном списке или все элементы
     *    содержащие такие ограничения были отброшены - будет выбран самый дешевый из всех продуктов подходящих
     *    для данного ингредиента. <br/>
     * @param constraints ограничения задающие конкретные продукты для ингредиентов блюд.
     * @return данные об одном конкретном продукте для каждого ингредиента блюда.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если constraints имеет значение null. <br/>
     *         2. Если один из элементов constraints имеет значение null. <br/>
     */
    public List<IngredientProduct> getProductForEachIngredient(List<ProductConstraint> constraints) {
        Validator.check(
                Rule.of("Dish.constrains").notNull(constraints).
                        and(r -> r.notContainsNull(constraints))
        );

        return IntStream.range(0, ingredients.size()).
                mapToObj(ingredientIndex -> {
                    int productIndex = constraints.stream().
                            filter(c -> c.ingredientIndex() == ingredientIndex).
                            mapToInt(ProductConstraint::productIndex).
                            filter(pIndex -> pIndex >= 0 && pIndex < getProductsNumber(ingredientIndex).orElseThrow()).
                            findFirst().
                            orElse(0);

                    return getProduct(ingredientIndex, productIndex).orElseThrow();
                }).
                toList();
    }

    /**
     * Групирует продукты относящиеся к разным ингредиентам по одинаковым продуктам. Если
     * {@link IngredientProduct#product()} какого-то ингредиента возвращает пустой Optional -
     * он не будет участвовать в формировании итогового результата.<br/><br/>
     * Данный метод полагается, что передаваемый список был корректно сформирован вызывающим кодом, например,
     * вызовом метода {@link #getProductForEachIngredient(List)}.
     * @param ingredientProducts данные об одном конкретном продукте для каждого ингредиента блюда.
     * @return продукты разных ингредиентов сгрупированные по одинаковым продуктам.
     * @throws ValidateException если menuItemProducts является null.
     */
    public Map<Product, List<IngredientProduct>> groupByProduct(List<IngredientProduct> ingredientProducts) {
        Validator.check(
                Rule.of("Dish.ingredients").notNull(ingredientProducts)
        );

        return ingredientProducts.stream().
                filter(i -> i.product().isPresent()).
                collect(Collectors.groupingBy(i -> i.product().orElseThrow()));
    }

    /**
     * В некоторых случаях один и тот же продукт может использоваться для нескольких ингредиентов блюда. Этот метод
     * возвращает общее кол-во указаннаго продукта необходимого для всех ингредиентов блюда где он используется.
     * Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProducts (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода: {@link #groupByProduct(List)}.
     * @param ingredientProducts данные о продукте используемом для нескольких ингредиентов блюда.
     * @param servingNumber кол-во порций блюда.
     * @return общее кол-во указаннаго продукта необходимого для всех ингредиентов блюда где он используется.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getNecessaryQuantity(List<IngredientProduct> ingredientProducts,
                                                     BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(r -> r.positiveValue(servingNumber)),
                Rule.of("Dish.ingredients").notNull(ingredients)
        );

        return ingredientProducts.stream().
                filter(i -> i.product().isPresent()).
                map(i -> calculateNecessaryQuantityInUnits(i, servingNumber)).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает необходимое кол-во "упаковок" продукта. Стоимость необходимого кол-ва рассчитывается
     * с учетом фасовки продукта (размера упаковки). Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProducts (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно
     * с помощью метода: {@link #groupByProduct(List)}.
     * @param ingredientProducts данные о продукте используемом для нескольких ингредиентов блюда.
     * @param servingNumber кол-во порций блюда.
     * @return общее кол-во упаковок указаннаго продукта необходимого для всех ингредиентов блюда где он
     *         используется.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getNecessaryPackageQuantity(List<IngredientProduct> ingredientProducts,
                                                            BigDecimal servingNumber) {
        return getNecessaryQuantity(ingredientProducts, servingNumber).
                map(necessaryQuantity -> {
                    Product product = retrieveProduct(ingredientProducts);
                    return calculatePackagesNumber(product, necessaryQuantity);
                });
    }

    /**
     * Возвращает общую цену за необходимое кол-во "упаковок" продукта. Стоимость необходимого кол-ва рассчитывается
     * с учетом фасовки продукта (размера упаковки). Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProducts (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно
     * с помощью метода: {@link #groupByProduct(List)}.
     * @param ingredientProducts данные о продукте используемом для нескольких ингредиентов блюда.
     * @param servingNumber кол-во порций блюда.
     * @return общую цену за необходимое кол-во докупаемого продукта, БЕЗ учета кол-ва уже имеющегося в наличии
     *         у пользователя.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getNecessaryPackageQuantityPrice(List<IngredientProduct> ingredientProducts,
                                                                 BigDecimal servingNumber) {
        return getNecessaryPackageQuantity(ingredientProducts, servingNumber).
                map(necessaryPackageQuantity -> {
                    Product product = retrieveProduct(ingredientProducts);
                    return calculateProductPrice(product, necessaryPackageQuantity);
                });
    }

    /**
     * Для любого указанного продукта, из множества взаимозаменямых продуктов указанного ингредиента, вычисляет
     * и возвращает - в каком кол-ве его необходимо докупить (именно "докупить", а не "купить", т.к. искомый
     * продукт может уже иметься в некотором кол-ве у пользователя) для блюда в указанном кол-ве порций.
     * Недостающее кол-во рассчитывается с учетом фасовки продукта (размера упаковки) и представляет собой
     * кол-во недостающих "упаковок" продукта. <br/>
     * Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} возвращает пустой Optional - этот метод также вернет
     *    пустой Optional.
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProduct (не выходят ли индексы за допустимые пределы, действительно
     * ли указанный продукт соответствует указанном ингредиенту) полагая, что данный объект был сформирован правильно
     * с помощью одного из методов: {@link #getProduct(int, int)} или {@link #getProductForEachIngredient(List)}.
     * @param ingredientProduct данные об одном из продуктов соответствующих одному из ингредиентов.
     * @param servingNumber кол-во порций блюда.
     * @return кол-во "упаковок" докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. servingNumber является null.<br/>
     *              2. servingNumber <= 0 <br/>
     *              3. ingredientProduct является null. <br/>
     */
    public Optional<BigDecimal> getLackPackageQuantity(IngredientProduct ingredientProduct,
                                                       BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(v -> v.positiveValue(servingNumber)),
                Rule.of("Dish.ingredientProduct").notNull(ingredientProduct)
        );

        return ingredientProduct.product().
                map(product -> {
                    BigDecimal necessaryQuantity = calculateNecessaryQuantityInUnits(ingredientProduct, servingNumber);
                    BigDecimal lackQuantityInUnits = calculateLackQuantityInUnits(product, necessaryQuantity);
                    return calculatePackagesNumber(product, lackQuantityInUnits);
                });
    }

    /**
     * Для любого указанного продукта, из множества взаимозаменямых продуктов указанного ингредиента, вычисляет
     * и возвращает - в каком кол-ве его необходимо докупить (именно "докупить", а не "купить", т.к. искомый
     * продукт может уже иметься в некотором кол-ве у пользователя) для блюда в указанном кол-ве порций.
     * Недостающее кол-во рассчитывается с учетом фасовки продукта (размера упаковки) и представляет собой
     * кол-во недостающих "упаковок" продукта. Данный метдо используется, когда для разных ингредиентов блюда
     * были выбраны одинаковые продукты.
     * <br/>
     * Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProducts (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода: {@link #groupByProduct(List)}.
     * @param ingredientProducts данные о продукте используемом для нескольких ингредиентов блюда.
     * @param servingNumber кол-во порций блюда.
     * @return кол-во "упаковок" докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getLackPackageQuantity(List<IngredientProduct> ingredientProducts,
                                                       BigDecimal servingNumber) {
        return getNecessaryQuantity(ingredientProducts, servingNumber).
                map(necessaryQuantityInUnits -> {
                    Product product = retrieveProduct(ingredientProducts);
                    BigDecimal lackQuantityInUnits = calculateLackQuantityInUnits(product, necessaryQuantityInUnits);
                    return calculatePackagesNumber(product, lackQuantityInUnits);
                });
    }

    /**
     * Возвращает общую цену за недостающее кол-во "упаковок" докупаемого продукта. Стоимость недостающего
     * кол-ва рассчитывается с учетом фасовки продукта (размера упаковки).
     * <br/>
     * Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} возвращает пустой Optional - этот метод также вернет
     *    пустой Optional.
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProduct (не выходят ли индексы за допустимые пределы, действительно
     * ли указанный продукт соответствует указанном ингредиенту) полагая, что данный объект был сформирован правильно
     * с помощью одного из методов: {@link #getProduct(int, int)} или {@link #getProductForEachIngredient(List)}.
     * @param ingredientProduct данные об одном из продуктов соответствующих одному из ингредиентов.
     * @param servingNumber кол-во порций блюда.
     * @return общую цену за недостающее кол-во докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. servingNumber является null.<br/>
     *              2. servingNumber <= 0 <br/>
     *              3. ingredientProduct является null. <br/>
     */
    public Optional<BigDecimal> getLackPackageQuantityPrice(IngredientProduct ingredientProduct,
                                                            BigDecimal servingNumber) {
        return getLackPackageQuantity(ingredientProduct, servingNumber).
                map(lackQuantity -> {
                    Product product = ingredientProduct.product().orElseThrow();
                    return calculateProductPrice(product, lackQuantity);
                });
    }

    /**
     * Возвращает общую цену за недостающее кол-во "упаковок" докупаемого продукта. Стоимость недостающего
     * кол-ва рассчитывается с учетом фасовки продукта (размера упаковки).
     * <br/>
     * Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Метод не проверят содержимое объекта ingredientProducts (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно
     * с помощью метода: {@link #groupByProduct(List)}.
     * @param ingredientProducts данные о продукте используемом для нескольких ингредиентов блюда.
     * @param servingNumber кол-во порций блюда.
     * @return общую цену за недостающее кол-во докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getLackPackageQuantityPrice(List<IngredientProduct> ingredientProducts,
                                                            BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(r -> r.positiveValue(servingNumber)),
                Rule.of("Dish.ingredients").notNull(ingredients)
        );

        return getLackPackageQuantity(ingredientProducts, servingNumber).
                map(lackQuantityInPackages -> {
                    Product product = retrieveProduct(ingredientProducts);
                    return calculateProductPrice(product, lackQuantityInPackages);
                });
    }

    /**
     * Возвращает стоимость данного блюда, которая представляет собой суммарную стоимость недостающего кол-ва одного
     * из продуктов выбранных для каждого ингредиента. Допускается, что для некоторых ингредиентов блюда не
     * указано ни одного продукта. Особые случаи: <br/>
     * 1. Если {@link IngredientProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 2. Если какой-либо из элементов списка {@link IngredientProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/>
     * 3. Если список пуст - возвращает пустой Optional. <br/>
     * <br/><br/>
     * Данный метод не проверяет содержимое списка ingredients на достоверность (список не содержит null, для
     * каждого ингредиента указан только один продукт, каждый продукт соответствует своему ингредиенту и т.д.)
     * полагая, что данный список был сформирован корректно с использованием метода
     * {@link #getProductForEachIngredient(List)}.
     * @param ingredients каждый элемент списка содержит данные об одном конкретном продукте соответствующего
     *                    одному конкретному ингредиенту этого блюда.
     * @param servingNumber кол-во порций блюда для которых рассчитывается общая стоимость.
     * @return стоимость данного блюда или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если ingredients имеет значение null.<br/>
     */
    public Optional<BigDecimal> getLackProductPrice(List<IngredientProduct> ingredients,
                                                    BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(r -> r.positiveValue(servingNumber))
        );

        Map<Product, List<IngredientProduct>> groups = groupByProduct(ingredients);

        return groups.values().stream().
                map(value -> getLackPackageQuantityPrice(value, servingNumber).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает все теги данного блюда в виде списка доступного только для чтения.
     * @return все теги данного блюда.
     */
    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Возвращает кол-во всех возможных комбинаций состава данного блюда. Если для данного блюда не было
     * указанно ни одного ингредиента или любому ингредиенту не соответсвует ни один продукт - возвращает 0.
     * @return кол-во всех возможных комбинаций состава данного блюда.
     */
    public BigInteger getNumberIngredientCombinations() {
        return IntStream.range(0, ingredients.size()).
                map(i -> getProductsNumber(i).orElseThrow()).
                filter(i -> i > 0).
                mapToObj(BigInteger::valueOf).
                reduce(BigInteger::multiply).
                orElse(BigInteger.ZERO);
    }

    /**
     * Возвращает минимально возможную стоимость одной порции данного блюда. При расчете минимальной стоимости
     * порции блюда НЕ учитывается кол-во продуктов уже имеющееся в наличии у пользователя. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту этого блюда не соответствует ни одного продукта - возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту этого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости блюда.
     * @return минимально возможная стоимость данного блюда.
     */
    public Optional<BigDecimal> getMinPrice() {
        List<ProductConstraint> constraints = IntStream.range(0, getIngredientNumber()).
                        mapToObj(i -> new ProductConstraint(i, 0)).
                        toList();

        List<IngredientProduct> ingredientProducts = getProductForEachIngredient(constraints);

        return groupByProduct(ingredientProducts).values().stream().
                map(values -> getNecessaryPackageQuantityPrice(values, BigDecimal.ONE).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает максимально возможную стоимость одной порции данного блюда. При расчете максимальной стоимости
     * порции блюда НЕ учитывается кол-во продуктов уже имеющееся в наличии у пользователя. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту этого блюда не соответствует ни одного продукта - возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту этого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете максимальной стоимости блюда.
     * @return максимально возможная стоимость данного блюда.
     */
    public Optional<BigDecimal> getMaxPrice() {
        /*
         * Если в метод getProduct(ingredientIndex, productIndex) в качестве productIndex передать
         * значение, которое больше или равно кол-ву всех продуктов соответствующих указанному
         * ингредиенту - то метод вернет последний из продуктов. Здесь мы берем заведомо большее
         * значение для productIndex чтобы получить последний продукт.
         */
        List<IngredientProduct> ingredientProducts = IntStream.range(0, getIngredientNumber()).
                mapToObj(ingredientIndex -> {
                    DishIngredient ingredient = ingredients.get(ingredientIndex);
                    int productIndex = 100000;

                    Page<Product> page = getProductPageBy(ingredient, productIndex);
                    Optional<Product> product = page.getByGlobalIndex(
                            page.getMetadata().getTotalItems().subtract(BigInteger.ONE)
                    );
                    return new IngredientProduct(product, ingredient.getId(), ingredientIndex, productIndex);
                }).
                toList();

        return groupByProduct(ingredientProducts).values().stream().
                map(values -> getNecessaryPackageQuantityPrice(values, BigDecimal.ONE).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает среднюю стоимость для одной порции данного блюда. При расчете средней стоимости
     * порции блюда НЕ учитывается кол-во продуктов уже имеющееся в наличии у пользователя. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту блюда не соответствует ни один продукт - возвращает пустой Optional.<br/>
     * 3. Если для какому-либо ингредиенту не соответствует ни одного продукта - то он не принимает участия
     *    в рассчете средней стоимости блюда.
     * @return средняя стоимость данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice() {
        Optional<BigDecimal> max = getMaxPrice();
        Optional<BigDecimal> min = getMinPrice();
        Optional<BigDecimal> sum = min.map(vMin -> vMin.add(max.orElseThrow()));
        return sum.map(vSum -> vSum.divide(new BigDecimal(2), config.getMathContext()));
    }

    /**
     * Выполняет сравнение на равенство двух блюд с учетом всех их данных.
     * @param other блюдо с которым выполняется сравнение.
     * @return true - если все поля двух блюд соответственно равны, false - в противном случае.
     */
    @Override
    public boolean equalsFullState(Dish other) {
        return id.equals(other.id) &&
                user.equalsFullState(other.user) &&
                name.equals(other.name) &&
                servingSize.equals(other.servingSize) &&
                unit.equals(other.unit) &&
                Objects.equals(description, other.description) &&
                Objects.equals(imageUrl, other.imageUrl) &&
                equals(ingredients, other.ingredients) &&
                tags.equals(other.tags) &&
                config == other.config &&
                productRepository == other.productRepository;
    }

    /**
     * Сравнивает два объекта Dish. Два объекта Dish считаются равными, если их идентификаторы имеют одинаковое
     * значение.
     * @param o сранвиваемый объект Dish.
     * @return true, если два объекта Dish имеют одинаковые идентификаторы, иначе - false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id.equals(dish.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", servingSize=" + servingSize +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl=" + imageUrl +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }


    private boolean equals(List<DishIngredient> a, List<DishIngredient> b) {
        return a.size() == b.size() &&
                IntStream.range(0, a.size()).allMatch(i -> a.get(i).equalsFullState(b.get(i)));
    }


    private Product retrieveProduct(List<IngredientProduct> ingredientProducts) {
        return ingredientProducts.stream().
                filter(i -> i.product().isPresent()).
                map(i -> i.product().orElseThrow()).
                findAny().
                orElseThrow();
    }

    private BigDecimal calculateNecessaryQuantityInUnits(IngredientProduct ingredientProduct, BigDecimal servingNumber) {
        return ingredients.get(ingredientProduct.ingredientIndex()).getNecessaryQuantity(servingNumber);
    }

    private BigDecimal calculateLackQuantityInUnits(Product product,
                                                    BigDecimal necessaryQuantity) {
        return necessaryQuantity.subtract(product.getQuantity()).max(BigDecimal.ZERO);
    }

    private BigDecimal calculatePackagesNumber(Product product, BigDecimal quantityInUnits) {
        if(quantityInUnits.signum() > 0) {
            quantityInUnits = quantityInUnits.
                    divide(product.getContext().getPackingSize(), config.getMathContext()).
                    setScale(0, RoundingMode.UP);
        }

        return quantityInUnits;
    }

    private BigDecimal calculateProductPrice(Product product, BigDecimal packagesNumber) {
        return product.getContext().getPrice().multiply(packagesNumber, config.getMathContext());
    }


    private Page<Product> getProductPageBy(DishIngredient ingredient, int productIndex) {
        Criteria criteria = new Criteria().
                setPageable(PageableByNumber.ofIndex(30, productIndex)).
                setSort(ingredientProductsSort).
                setFilter(ingredient.getFilter());

        return productRepository.getProducts(criteria);
    }

    private Page<Product> getProductPageBy(DishIngredient ingredient, UUID productId) {
        Criteria criteria = new Criteria().
                setPageable(PageableById.of(30, productId)).
                setSort(ingredientProductsSort).
                setFilter(ingredient.getFilter());

        return productRepository.getProducts(criteria);
    }

    private int getIngredientIndexBy(UUID ingredientId) {
        return IntStream.range(0, ingredients.size()).
                filter(i -> ingredients.get(i).getId().equals(ingredientId)).
                findFirst().
                orElse(-1);
    }


    public record ProductConstraint(int ingredientIndex, int productIndex) {}

    public record ProductConstraintWithId(UUID ingredientId, UUID productId) {}

    public record IngredientProduct(Optional<Product> product,
                                    UUID ingredientId,
                                    int ingredientIndex,
                                    int productIndex) {}

    /**
     * Реализация паттерна "Builder" для блюда ({@link Dish}).
     */
    public static class Builder implements AbstractBuilder<Dish> {

        private UUID id;
        private User user;
        private String name;
        private BigDecimal servingSize;
        private String unit;
        private String description;
        private String imageUrl;
        private List<DishIngredient.Builder> ingredients;
        private List<String> tags;
        private AppConfigData config;
        private ProductRepository repository;

        public Builder() {
            ingredients = new ArrayList<>();
            tags = new ArrayList<>();
        }

        public Builder generateId() {
            id = UUID.randomUUID();
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setServingSize(BigDecimal servingSize) {
            this.servingSize = servingSize;
            return this;
        }

        public Builder setUnit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setConfig(AppConfigData config) {
            this.config = config;

            ingredients.forEach(b -> b.setConfig(config));

            return this;
        }

        public Builder addIngredient(DishIngredient.Builder ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        public Builder addTag(String tag) {
            tags.add(tag);
            return this;
        }

        public Builder setRepository(ProductRepository repository) {
            this.repository = repository;
            return this;
        }

        @Override
        public Dish tryBuild() throws ValidateException {
            return new Dish(
                    id,
                    user,
                    name,
                    servingSize,
                    unit,
                    description,
                    imageUrl,
                    ingredients,
                    tags,
                    config,
                    repository
            );
        }

    }

}

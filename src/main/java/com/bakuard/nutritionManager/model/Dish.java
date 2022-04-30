package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.filters.UserFilter;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.validation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
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
     * Устанавливает наименование для данного блюда. Указанное наименование будет сохранено без начальных и
     * конечных пробельных символов.
     * @param name наименование для данного блюда.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *         1. если name имеет значение null.<br/>
     *         2. если name не содержит ни одного отображаемого символа.
     */
    public void setName(String name) {
        Validator.check(
                Rule.of("Dish.name").notNull(name).and(v -> v.notBlank(name))
        );

        this.name = name.trim();
    }

    /**
     * Устанавливает размер одной порции данного блюда.
     * @param servingSize размер одной порции данного блюда.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *         1. если servingNumber имеет значение null.<br/>
     *         2. если servingNumber меньше или равен нулю.
     */
    public void setServingSize(BigDecimal servingSize) {
        Validator.check(
                Rule.of("Dish.servingSize").notNull(servingSize).
                        and(r -> r.positiveValue(servingSize))
        );

        this.servingSize = servingSize.setScale(config.getNumberScale(), config.getRoundingMode());
    }

    /**
     * Задает наименование единицы измерения кол-ва для данного блюда. Заданное значение будет сохранено без
     * начальных и конечных пробельных символов.
     * @param unit наименование единицы измерения кол-ва для данного блюда.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *         1. если unit имеет значение null.<br/>
     *         2. если unit не содержит ни одного отображаемого символа.
     */
    public void setUnit(String unit) {
        Validator.check(
                Rule.of("Dish.unit").notNull(unit).and(v -> v.notBlank(unit))
        );

        this.unit = unit.trim();
    }

    /**
     * Задает описание для данного блюда. Метод может принимать значение null.
     * @param description описание для данного блюда.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Задает путь изображения данного блюда. Путь не обязательно может быть путем в файловой системе.
     * Метод может принимать значение null.
     * @param imageUrl путь изображения данного блюда.
     */
    public void setImageUrl(String imageUrl) {
        Container<URL> urlContainer = new Container<>();

        Validator.check(
                Rule.of("Dish.imageUrl").isNull(imageUrl).or(v -> v.isUrl(imageUrl, urlContainer))
        );

        this.imageUrl = urlContainer.get();
    }

    /**
     * Добавляет в данное блюдо ингредиент представленный множеством взаимозаменяемых продуктов (которое,
     * в свою очередь, представленно ограничением filter). Для добавляемого ингредиента будет указанно
     * его кол-во и наименование. Если блюдо уже содержит ингредиент с таким наименованием, то для него будут
     * перезаписаны огрнаинчения и кол-во.
     * @param name наименование ингредиента.
     * @param filter ограничение задающее множество взаимозаменяемых продуктов, каждый из которых может
     *               выступать данным конкретным ингредиентом этого блюда.
     * @param quantity кол-во соответсвующего ингредиента.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *         1. если quantity меньше или равен нулю.<br/>
     *         2. если один из параметров имеет значение null.<br/>
     *         3. если name не содержит ни одного отображаемого символа.
     */
    public void putIngredient(String name, Filter filter, BigDecimal quantity) {
        DishIngredient ingredient = new DishIngredient(name, filter, quantity, config);

        int index = IntStream.range(0, ingredients.size()).
                filter(i -> ingredients.get(i).getName().equals(name)).
                findFirst().
                orElse(-1);

        if(index != -1) ingredients.set(1, ingredient);
        else ingredients.add(ingredient);
    }

    /**
     * Удаляет указанный ингредиент по его имени и возвращает его. Если блюдо не содержит ингредиент с таким
     * именем, то метод просто возвращает null.
     * @param name имя ингредиента.
     * @return удаленный ингредиент или null.
     */
    public DishIngredient removeIngredient(String name) {
        DishIngredient removed = ingredients.stream().
                filter(ingredient -> ingredient.getName().equals(name)).
                findAny().
                orElse(null);

        ingredients.remove(removed);

        return removed;
    }

    /**
     * Добавляет новый тег в указанное блюдо.
     * @param tag добавляемый тег.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанный тег уже содержится в данном объекте.
     */
    public void addTag(Tag tag) {
        Validator.check(
                Rule.of("Dish.tag").notNull(tag),
                Rule.of("Dish.tags").notContainsItem(tags, tag)
        );

        tags.add(tag);
    }

    /**
     * Удаляет указанный тег из блюда. Если указанный тег отсутствует в блюде или имеет значение null,
     * метод ничего не делает.
     * @param tag удаляемый тег.
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
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
     * Возвращает ингредиент по его имени. Если у блюда нет ингредиента с таким имененем - возвращает
     * пустой Optional.
     * @param name имя искомого ингредиента.
     * @return ингредиент блюда.
     */
    public Optional<DishIngredient> getIngredient(String name) {
        return ingredients.stream().
                filter(i -> i.getName().equals(name)).
                findAny();
    }

    /**
     * Возвращает ингредиент по его индексу. Особые случаи: <br/>
     * 1. Если индекс ингредиента меньше 0 - возвращает пустой Optional. <br/>
     * 2. Если индекс ингредиента больше или равен кол-ву ингредиентов блюда - возвращает пустой Optional. <br/>
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
     * Возвращает ингредиент по его индексу.
     * @param ingredientIndex индекс искомого ингредиента.
     * @return ингредиент блюда или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из условий:<br/>
     *                           1. ingredientIndex меньше нуля.<br/>
     *                           2. ingredientIndex больше или равен кол-ву ингредиентов блюда.
     */
    public DishIngredient tryGetIngredient(int ingredientIndex) {
        Validator.check(
                Rule.of("Dish.ingredientIndex").range(ingredientIndex, 0, ingredients.size() - 1)
        );

        return ingredients.get(ingredientIndex);
    }

    /**
     * Возвращает все ингредиенты данного блюда в виде списка доступного только для чтения.
     * @return все ингрединты данного блюда.
     */
    public List<DishIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /**
     * Возвращает кол-во ингредиентов данного блюда.
     * @return кол-во ингредиентов данного блюда.
     */
    public int getIngredientNumber() {
        return ingredients.size();
    }

    /**
     * Возвращает продукт из множества всех взаимозаменяемых продуктов для данного ингредиента по его
     * порядковому номеру (т.е. индексу. Индексация начинается с нуля). Множество продуктов для данного
     * ингредиента упорядоченно по цене в порядке возрастания. Особые случаи:<br/>
     * 1. Если в БД нет ни одного продукта удовлетворяющего ограничению {@link DishIngredient#getFilter()}
     *    данного ингредиента, то метод вернет пустой Optional.<br/>
     * 2. Если в БД есть продукты соответствующие данному ингредиенту и productIndex больше или равен их
     *    кол-ву, то метод вернет последний продукт из всего множества продуктов данного ингредиента.
     * @param ingredientIndex индекс ингредиента для которого возвращается один из возможных взаимозаменяемых продуктов.
     * @param productIndex порядковый номер продукта.
     * @return продукт по его порядковому номеру.
     * @throws ValidateException если выполняется хотя бы одноиз условий:<br/>
     *                           1. ingredientIndex меньше нуля.<br/>
     *                           2. ingredientIndex больше или равен кол-ву ингредиентов блюда.<br/>
     *                           3. если productIndex < 0
     */
    public Optional<Product> getProduct(int ingredientIndex, int productIndex) {
        Validator.check(
                Rule.of("Dish.productIndex").notNegative(productIndex)
        );

        DishIngredient ingredient = tryGetIngredient(ingredientIndex);
        Page<Product> page = productRepository.getProducts(
                new Criteria().
                        setPageable(Pageable.ofIndex(30, productIndex)).
                        setSort(ingredientProductsSort).
                        setFilter(ingredient.getFilter())
        );

        Product product = null;

        if(!page.getMetadata().isEmpty()) {
            product = page.get(
                    page.getMetadata().
                            getTotalItems().
                            subtract(BigInteger.ONE).
                            min(BigInteger.valueOf(productIndex))
            );
        }

        return Optional.ofNullable(product);
    }

    /**
     * Возвращает часть выборки продуктов соответствующих указанному ингредиенту. Все продукты в выборке
     * упорядоченны по цене в порядке возрастания.
     * @param ingredientIndex индекс ингредиента.
     * @param pageNumber номер страницы выборки.
     * @return страницу из выборки продуктов соответствующих указанному ингредиенту.
     * @throws ValidateException если выполняется хотя бы одно из условий:<br/>
     *                           1. ingredientIndex меньше нуля.<br/>
     *                           2. ingredientIndex больше или равен кол-ву ингредиентов блюда.
     */
    public Page<Product> getProducts(int ingredientIndex, int pageNumber) {
        DishIngredient ingredient = tryGetIngredient(ingredientIndex);
        return productRepository.getProducts(
                new Criteria().
                        setPageable(Pageable.of(30, pageNumber)).
                        setSort(ingredientProductsSort).
                        setFilter(ingredient.getFilter())
        );
    }

    /**
     * Возвращает кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента.
     * @param ingredientIndex индекс ингредиента для которого рассчитывается кол-во взаимозаменяемых продуктов.
     * @return кол-во всех продуктов, которые могут использоваться в качестве данного ингредиента.
     * @throws ValidateException если выполняется хотя бы одноиз условий:<br/>
     *                           1. ingredientIndex меньше нуля.<br/>
     *                           2. ingredientIndex больше или равен кол-ву ингредиентов блюда.
     */
    public int getProductsNumber(int ingredientIndex) {
        DishIngredient ingredient = tryGetIngredient(ingredientIndex);
        return productRepository.getProductsNumber(new Criteria().setFilter(ingredient.getFilter()));
    }

    /**
     * Для любого указанного продукта, из множества взаимозаменямых продуктов данного ингредиента, вычисляет
     * и возвращает - в каком кол-ве его необходимо докупить (именно "докупить", а не "купить", т.к. искомый
     * продукт может уже иметься в некотором кол-ве у пользователя) для блюда в указанном кол-ве порций.
     * Недостающее кол-во рассчитывается с учетом фасовки продукта (размера упаковки) и представляет собой
     * кол-во недостающих "упаковок" продукта. Особые случаи:<br/>
     * 1. Если указанный productIndex больше или равен кол-ву всех продуктов соответствующих данному ингрдиенту,
     *    то метод вернет результат для последнего продукта.<br/>
     * 2. Если в БД нет ни одного продукта удовлетворяющего ограничению {@link DishIngredient#getFilter()}
     *    данного ингредиента, то метод вернет пустой Optional.
     * @param productIndex порядковый номер продукта из множества взаимозаменяемых продуктов данного ингредиента.
     * @param ingredientIndex индекс ингредиента для которого рассчитывается кол-во недостающих продуктов.
     * @param servingNumber кол-во порций блюда.
     * @return кол-во "упаковок" докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. servingNumber является null.<br/>
     *              2. servingNumber <= 0 <br/>
     *              3. productIndex < 0 <br/>
     *              4. ingredientIndex меньше нуля.<br/>
     *              5. ingredientIndex больше или равен кол-ву ингредиентов блюда.<br/>
     */
    public Optional<BigDecimal> getLackQuantity(int ingredientIndex,
                                                int productIndex,
                                                BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(v -> v.positiveValue(servingNumber))
        );

        return getProduct(ingredientIndex, productIndex).
                map(product -> getLackQuantity(ingredients.get(ingredientIndex), product, servingNumber));
    }

    /**
     * Возвращает общую цену за недостающее кол-во "упаковок" докупаемого продукта. Стоимость недостающего
     * кол-ва рассчитывается с учетом фасовки продукта (размера упаковки). Особые случаи: <br/>
     * 1. Если в БД нет ни одного продукта удовлетворяющего ограничению {@link DishIngredient#getFilter()}
     *    данного ингредиента, то метод вернет пустой Optional.<br/>
     * 2. Если указанный productIndex больше или равен кол-ву всех продуктов соответствующих данному ингрдиенту,
     *    то метод вернет результат для последнего продукта.<br/>
     * @param productIndex порядковый номер продукта из множества взаимозаменяемых продуктов данного ингредиента.
     * @param ingredientIndex индекс ингредиента для которого рассчитывается общай цена недостающего кол-ва продуктов.
     * @param servingNumber кол-во порций блюда.
     * @return общую цену за недостающее кол-во докупаемого продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. servingNumber является null.<br/>
     *              2. servingNumber <= 0 <br/>
     *              3. productIndex < 0 <br/>
     *              4. ingredientIndex меньше нуля.<br/>
     *              5. ingredientIndex больше или равен кол-ву ингредиентов блюда.<br/>
     */
    public Optional<BigDecimal> getLackQuantityPrice(int ingredientIndex,
                                                     int productIndex,
                                                     BigDecimal servingNumber) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(v -> v.positiveValue(servingNumber))
        );

        return getProduct(ingredientIndex, productIndex).
                map(
                        product -> getLackQuantity(ingredients.get(ingredientIndex), product, servingNumber).
                                multiply(product.getContext().getPrice(), config.getMathContext())
                );
    }

    /**
     * Проверяет - содержится ли в блюде ингредиент с указанным именем.
     * @param name имя искомого ингредиента.
     * @return true - если указанный ингредиент содержится в данном блюде, иначе - false.
     */
    public boolean containsIngredients(String name) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.getName().equals(name));
    }

    /**
     * Возвращает все теги данного блюда в виде списка доступного только для чтения.
     * @return все теги данного блюда.
     */
    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Проверяет, содержится ли указанный тег в данном блюде.
     * @param tag искомый тег.
     * @return true - если указанный тег содержится в данном блюде, иначе - false.
     */
    public boolean containsTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Возвращает кол-во всех возможных комбинаций состава данного блюда. Если для данного блюда не было
     * указанно ни одного ингредиента или любому ингредиенту не соответсвует ни один продукт - возвращает 0.
     * @return кол-во всех возможных комбинаций состава данного блюда.
     */
    public BigInteger getNumberIngredientCombinations() {
        return IntStream.range(0, ingredients.size()).
                map(this::getProductsNumber).
                filter(i -> i > 0).
                mapToObj(BigInteger::valueOf).
                reduce(BigInteger::multiply).
                orElse(BigInteger.ZERO);
    }

    /**
     * Возвращает стоимость данного блюда, которая представляет собой суммарную стоимость недостающего кол-ва одного
     * из продуктов выбранных для каждого ингредиента. Особые случаи:<br/>
     * 1. Если блюдо не содержит ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту не соответсвует ни один продукт - возвращает пустой Optional.<br/>
     * 3. Если для какого-либо ингредиента не соответствует ни один продукт - то он не принимает участия
     *    в рассчете цены блюда.<br/>
     * 4. Если для какого-либо ингредиента не выбран продукт - то в качестве значения по умолчанию будет
     *    выбран самый дешевый продукт соответствующий данному ингредиенту.
     * @param servingNumber кол-во порций блюда для которых рассчитывается общая стоимость.
     * @param productsIndex набор пар - [индекс ингредиента, индекс продукта], где индекс продукта
     *                    это индекс из отсортированного в порядке возрастания по цене множества взаимозаменяемых
     *                    продуктов этого ингредиента.
     * @return стоиомость данного блюда или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber имеет значение null.<br/>
     *              2. если servingNumber меньше или равно нулю.<br/>
     *              3. если productsIndex имеет значение null.<br/>
     *              4. если хотя бы один из ключей productsIndex имеет значение null.<br/>
     *              5. если хотя бы одно из значений productsIndex имеет значение null.<br/>
     *              6. если хотя бы одно из значений productsIndex отрицательное число.<br/>
     *              7. если хотя бы один из ключей productsIndex меньше нуля.<br/>
     *              8. если хотя бы один из ключей productsIndex больше или равен кол-ву ингредиентов.<br/>
     */
    public Optional<BigDecimal> getLackProductPrice(BigDecimal servingNumber,
                                                    Map<Integer, Integer> productsIndex) {
        Validator.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(r -> r.positiveValue(servingNumber)),
                Rule.of("Dish.productsIndex").notNull(productsIndex).
                        and(r -> r.notContainsNull(productsIndex.keySet(), "keySet")).
                        and(r -> r.notContainsNull(productsIndex.values(), "values")).
                        and(r -> r.notContains(
                                productsIndex.keySet(),
                                (i -> Result.State.of(i >= 0 && i < ingredients.size())),
                                "keySet"
                        )).
                        and(r -> r.notContains(
                                productsIndex.values(),
                                (i -> Result.State.of(i >= 0)),
                                "values"
                        ))
        );

        return IntStream.range(0, ingredients.size()).
                mapToObj(i -> {
                    int productIndex = productsIndex.get(i) != null ? productsIndex.get(i) : 0;
                    return getLackQuantityPrice(i, productIndex, servingNumber);
                }).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает минимально возможную стоимость данного блюда. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту этого блюда не соответствует ни одного продукта - возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту этого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости блюда.
     * @return минимально возможная стоимость данного блюда.
     */
    public Optional<BigDecimal> getMinPrice() {
        return IntStream.range(0, getIngredientNumber()).
                mapToObj(i -> {
                    BigDecimal necessaryQuantity = ingredients.get(i).getNecessaryQuantity(BigDecimal.ONE);
                    return getProduct(i, 0).
                            map(product -> product.getContext().getPrice().multiply(necessaryQuantity));
                }).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает максимально возможную стоимость данного блюда. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту этого блюда не соответствует ни одного продукта - возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту этого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете максимальной стоимости блюда.
     * @return максимально возможная стоимость данного блюда.
     */
    public Optional<BigDecimal> getMaxPrice() {
        return IntStream.range(0, getIngredientNumber()).
                mapToObj(i -> {
                    /*
                    * Если в метод getProduct(ingredientIndex, productIndex) в качестве productIndex передать
                    * значение, которое больше или равно кол-ву всех продуктов соответствующих указанному
                    * ингредиенту - то метод вернет последний из продуктов. Здесь мы берем заведомо большее
                    * значение для productIndex чтобы получить последний продукт.
                     */
                    int lastProductIndex = 100000;
                    BigDecimal necessaryQuantity = ingredients.get(i).getNecessaryQuantity(BigDecimal.ONE);
                    return getProduct(i, lastProductIndex).
                            map(product -> product.getContext().getPrice().multiply(necessaryQuantity));
                }).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает среднюю стоимость для данного блюда. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту блюда не соответствует ни один продукт - возвращает пустой Optional.<br/>
     * 3. Если для какому-либо ингредиенту не соответствует ни одного продукта - то он не принимает участия
     *    в рассчете средней стоимости блюда.
     * @return средняя стоимость данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice() {
        return getMinPrice().
                flatMap(min -> getMaxPrice().map(max -> max.add(min))).
                map(sum -> sum.divide(new BigDecimal(2), config.getMathContext()));
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
                ingredients.equals(other.ingredients) &&
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


    private BigDecimal getLackQuantity(DishIngredient ingredient,
                                       Product product,
                                       BigDecimal servingNumber) {
        BigDecimal lackQuantity = ingredient.getNecessaryQuantity(servingNumber).
                subtract(product.getQuantity()).
                max(BigDecimal.ZERO);

        if(lackQuantity.signum() > 0) {
            lackQuantity = lackQuantity.
                    divide(product.getContext().getPackingSize(), config.getMathContext()).
                    setScale(0, RoundingMode.UP);
        }

        return lackQuantity;
    }


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

        public Builder addIngredient(String name, Filter filter, BigDecimal quantity) {
            ingredients.add(
                    new DishIngredient.Builder().
                            setName(name).
                            setFilter(filter).
                            setQuantity(quantity).
                            setConfig(config)
            );
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

        public boolean containsTag(String tag) {
            return tags.contains(tag);
        }

        public boolean containsIngredient(String name, Filter filter, BigDecimal quantity) {
            return ingredients.stream().anyMatch(i -> i.contains(name, filter, quantity));
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

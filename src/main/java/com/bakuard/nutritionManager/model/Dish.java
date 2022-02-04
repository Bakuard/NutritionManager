package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Представляет оределенное блюдо.
 */
public class Dish {

    private final UUID id;
    private final User user;
    private String name;
    private String unit;
    private String description;
    private String imagePath;
    private final List<DishIngredient> ingredients;
    private final List<Tag> tags;
    private AppConfigData config;
    private ProductRepository productRepository;

    private Dish(UUID id,
                 User user,
                 String name,
                 String unit,
                 String description,
                 String imagePath,
                 List<DishIngredient.Builder> ingredients,
                 List<String> tags,
                 AppConfigData config,
                 ProductRepository productRepository) {
        Checker.Container<List<Tag>> tagContainer = Checker.container();
        Checker.Container<List<DishIngredient>> ingredientContainer = Checker.container();

        Checker.of(getClass(), "constructor").
                nullValue("id", id).
                nullValue("user", user).
                nullValue("name", name).
                blankValue("name", name).
                nullValue("unit", unit).
                blankValue("unit", unit).
                tryBuildForEach(ingredients, ingredientContainer).
                tryBuildForEach(tags, Tag::new, tagContainer).
                nullValue("config", config).
                nullValue("repository", productRepository).
                checkWithValidateException("Fail to create dish");

        this.id = id;
        this.user = user;
        this.name = name.trim();
        this.unit = unit.trim();
        this.description = description;
        this.imagePath = imagePath;
        this.ingredients = ingredientContainer.get();
        this.tags = new ArrayList<>(tagContainer.get());
        this.config = config;
        this.productRepository = productRepository;
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
        Checker.of(getClass(), "setName").
                nullValue("name", name).
                blankValue("name", name).
                checkWithValidateException("Fail to set dish name");

        this.name = name.trim();
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
        Checker.of(getClass(), "setUnit").
                nullValue("unit", unit).
                blankValue("unit", unit).
                checkWithValidateException("Fail to set dish unit");

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
     * @param imagePath путь изображения данного блюда.
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
        DishIngredient ingredient = new DishIngredient(name, filter, quantity, productRepository, user, config);

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
        Checker.of(getClass(), "addTag").
                nullValue("tag", tag).
                duplicateTag("tags", tags, tag).
                checkWithValidateException("Fail to add tag to dish");

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
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Возвращает все ингредиенты данного блюда в виде списка доступного только для чтения.
     * @return все ингрединты данного блюда.
     */
    public List<DishIngredient> getReadonlyIngredients() {
        return Collections.unmodifiableList(ingredients);
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
    public List<Tag> getReadonlyTags() {
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
     * указанно ни одного ингредиента - возвращает 0.
     * @return кол-во всех возможных комбинаций состава данного блюда.
     */
    public BigInteger getNumberIngredientCombinations() {
        BigInteger result = BigInteger.ZERO;

        for(DishIngredient ingredient : ingredients) {
            if(result.signum() == 0) result = BigInteger.valueOf(ingredient.getProductsNumber());
            result = result.multiply(BigInteger.valueOf(ingredient.getProductsNumber()));
        }

        return result;
    }

    /**
     * Возвращает стоимость данного блюда с учетом конкретных продуктов выбранных как его ингредиенты и
     * кол-во порций.
     * @param servingNumber кол-во порций блюда для которых рассчитывается общая стоимость.
     * @param ingredients набор пар - [наименование ингредиента, индекс продукта], где индекс продукта
     *                    это индекс из отсортированного в порядке возрастания по имени множества взаимозаменяемых
     *                    продуктов, каждый из которых может выступать соответствующим этому множеству ингредиентом.
     * @return цена данного блюда или пустой Optional, если блюдо не содержит ни одного ингредиента.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber меньше нуля.<br/>
     *              2. если любой из параметров имеет значение null.<br/>
     *              3. если хотя бы одно из значений ingredients - отрицательное число.<br/>
     *              4. если хотя бы один из ключей ingredients - наменование ингредиента, которого нет
     *                 в данном блюде.
     */
    public Optional<BigDecimal> getPrice(BigDecimal servingNumber,
                                         Map<String, Integer> ingredients) {
        Checker checker = Checker.of(getClass(), "getPrice").
                nullValue("servingNumber", servingNumber).
                nullValue("ingredients", ingredients).
                negativeValue("servingNumber", servingNumber).
                containsNegative("ingredients", ingredients.values()).
                checkWithValidateException("Fail to get dish price");

        if(this.ingredients.size() != ingredients.size() ||
                this.ingredients.stream().
                map(DishIngredient::getName).
                anyMatch(i -> !ingredients.containsKey(i))) {
            checker.addConstraint("ingredients", ConstraintType.UNKNOWN_ITEM).
                    checkWithValidateException("Fail to get dish price");
        }

        BigDecimal result = this.ingredients.isEmpty() ? null : BigDecimal.ZERO;

        for(DishIngredient ingredient : this.ingredients) {
            int productIndex = ingredients.get(ingredient.getName());
            result = result.add(ingredient.getLackQuantityPrice(productIndex, servingNumber));
        }

        return Optional.ofNullable(result);
    }

    /**
     * Возвращает среднюю арифметическую цену для данного блюда. Если для блюда не было указанно ни одного
     * ингредиента - возвращает пустой Optional.
     * @return средня арифметическая цена данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice() {
        if(ingredients.isEmpty()) return Optional.empty();

        BigDecimal productTotalNumber = BigDecimal.ZERO;
        BigDecimal productsPriceSum = BigDecimal.ZERO;
        for(DishIngredient ingredient : ingredients) {
            productTotalNumber = productTotalNumber.add(BigDecimal.valueOf(ingredient.getProductsNumber()));
            productsPriceSum = productsPriceSum.add(ingredient.getProductsPriceSum());
        }

        return Optional.of(productsPriceSum.divide(productTotalNumber, config.getMathContext()));
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
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", ingredients=" + ingredients +
                ", tags=" + tags +
                '}';
    }

    /**
     * Реализация паттерна "Builder" для блюда ({@link Dish}).
     */
    public static class Builder implements AbstractBuilder<Dish> {

        private UUID id;
        private User user;
        private String name;
        private String unit;
        private String description;
        private String imagePath;
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

            ingredients.forEach(b -> b.setUser(user));

            return this;
        }

        public Builder setName(String name) {
            this.name = name;
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

        public Builder setImagePath(String imagePath) {
            this.imagePath = imagePath;
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
                            setConfig(config).
                            setRepository(repository).
                            setUser(user)
            );
            return this;
        }

        public Builder addTag(String tag) {
            tags.add(tag);
            return this;
        }

        public Builder setRepository(ProductRepository repository) {
            this.repository = repository;

            ingredients.forEach(b -> b.setRepository(repository));

            return this;
        }

        @Override
        public Dish tryBuild() throws ValidateException {
            return new Dish(
                    id,
                    user,
                    name,
                    unit,
                    description,
                    imagePath,
                    ingredients,
                    tags,
                    config,
                    repository
            );
        }

    }

}

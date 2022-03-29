package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
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
    private URL imageUrl;
    private final List<DishIngredient> ingredients;
    private final List<Tag> tags;
    private AppConfigData config;
    private ProductRepository productRepository;

    /**
     * Конструктор копирования. Выполняет глубокое копирование.
     * @param other копируемое блюдо.
     */
    public Dish(Dish other) {
        this.id = other.id;
        this.user = new User(other.user);
        this.name = other.name;
        this.unit = other.unit;
        this.description = other.description;
        this.imageUrl = other.imageUrl;
        this.config = other.config;
        this.productRepository = other.productRepository;
        this.ingredients = other.ingredients.stream().
                map(DishIngredient::new).
                collect(Collectors.toCollection(ArrayList::new));
        this.tags = new ArrayList<>(other.tags);
    }

    private Dish(UUID id,
                 User user,
                 String name,
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

        ValidateException.check(
                Rule.of("Dish.id").notNull(id),
                Rule.of("Dish.user").notNull(user),
                Rule.of("Dish.name").notNull(name).and(v -> v.notBlank(name)),
                Rule.of("Dish.unit").notNull(unit).and(v -> v.notBlank(unit)),
                Rule.of("Dish.imageUrl").isNull(imageUrl).or(v -> v.isUrl(imageUrl, urlContainer)),
                Rule.of("Dish.ingredients").doesNotThrow(ingredients, DishIngredient.Builder::tryBuild, ingredientContainer),
                Rule.of("Dish.tags").doesNotThrow(tags, Tag::new, tagContainer),
                Rule.of("Dish.config").notNull(config),
                Rule.of("Dish.repository").notNull(productRepository)
        );

        this.id = id;
        this.user = user;
        this.name = name.trim();
        this.unit = unit.trim();
        this.description = description;
        this.imageUrl = urlContainer.get();
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
        ValidateException.check(
                Rule.of("Dish.name").notNull(name).and(v -> v.notBlank(name))
        );

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
        ValidateException.check(
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

        ValidateException.check(
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
        ValidateException.check(
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
    public URL getImageUrl() {
        return imageUrl;
    }

    /**
     * Возвращает все ингредиенты данного блюда в виде списка доступного только для чтения.
     * @return все ингрединты данного блюда.
     */
    public List<DishIngredient> getIngredients() {
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
        BigInteger result = BigInteger.ZERO;

        for(DishIngredient ingredient : ingredients) {
            int productsNumber = ingredient.getProductsNumber();

            if(result.signum() == 0 && productsNumber > 0) {
                result = BigInteger.valueOf(productsNumber);
            } else if(productsNumber > 0) {
                result = result.multiply(BigInteger.valueOf(productsNumber));
            }
        }

        return result;
    }

    /**
     * Возвращает стоимость данного блюда с учетом выбранных продуктов, по одному для каждго ингредиента, и
     * кол-ва порций. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента или любому ингредиенту
     *    не соответсвует ни один продукт - возвращает пустой Optional.<br/>
     * 2. Если для какому-либо ингредиенту не соответствует ни одного продукта - то он не принимает участия
     *    в рассчете цены блюда.
     * @param servingNumber кол-во порций блюда для которых рассчитывается общая стоимость.
     * @param productsIndex набор пар - [наименование ингредиента, индекс продукта], где индекс продукта
     *                    это индекс из отсортированного в порядке возрастания по имени множества взаимозаменяемых
     *                    продуктов, каждый из которых может выступать соответствующим этому множеству ингредиентом.
     * @return цена данного блюда или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. если servingNumber меньше или равен нулю.<br/>
     *              2. если любой из параметров имеет значение null.<br/>
     *              3. если хотя бы одно из значений productsIndex - отрицательное число.<br/>
     *              4. если хотя бы один из ключей productsIndex - наменование ингредиента, которого нет
     *                 в данном блюде.
     */
    public Optional<BigDecimal> getPrice(BigDecimal servingNumber,
                                         Map<String, Integer> productsIndex) {
        ValidateException.check(
                Rule.of("Dish.servingNumber").notNull(servingNumber).
                        and(v -> v.positiveValue(servingNumber)),
                Rule.of("Dish.productsIndex").notNull(productsIndex).
                        and(v -> v.notContains(productsIndex.values(), (i -> Result.State.of(i != null && i >= 0)))).
                        and(v -> v.containsTheSameItems(
                                productsIndex.keySet(),
                                ingredients.stream().map(DishIngredient::getName).toList()
                        ))
        );

        return ingredients.stream().
                map(i -> i.getLackQuantityPrice(productsIndex.get(i.getName()), servingNumber)).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает среднюю арифметическую цену для данного блюда. Особые случаи:<br/>
     * 1. Если для данного блюда не было указанно ни одного ингредиента или любому ингредиенту
     *    не соответсвует ни один продукт - возвращает пустой Optional.<br/>
     * 2. Если для какому-либо ингредиенту не соответствует ни одного продукта - то он не принимает участия
     *    в рассчете средней арифметической цены блюда.
     * @return средня арифметическая цена данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice() {
        return ingredients.stream().
                map(DishIngredient::getProductsPriceSum).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add).
                map(totalPrice -> {
                    BigDecimal totalNumber = ingredients.stream().
                            map(i -> new BigDecimal(i.getProductsNumber())).
                            reduce(BigDecimal::add).
                            get();

                    return totalPrice.divide(totalNumber, config.getMathContext());
                });
    }

    /**
     * Выполняет сравнение на равенство двух блюд с учетом всех их данных.
     * @param other блюдо с которым выполняется сравнение.
     * @return true - если все поля двух блюд соответственно равны, false - в противном случае.
     */
    public boolean equalsFullState(Dish other) {
        return id.equals(other.id) &&
                user.equalsFullState(other.user) &&
                name.equals(other.name) &&
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
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", tags=" + tags +
                ", ingredients=" + ingredients +
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

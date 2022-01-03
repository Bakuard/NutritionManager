package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.exceptions.BlankValueException;
import com.bakuard.nutritionManager.model.exceptions.DuplicateTagException;
import com.bakuard.nutritionManager.model.exceptions.OutOfRangeException;
import com.bakuard.nutritionManager.model.filters.Constraint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

/**
 * Представляет оределенное блюдо.
 */
public class Dish {

    private final UUID id;
    private final UUID userId;
    private String name;
    private String unit;
    private String description;
    private String imagePath;
    private final List<DishIngredient> ingredients;
    private final Set<Tag> tags;
    private final Set<Tag> readonlyTags;

    Dish(UUID id, UUID userId) {
        checkId(id);
        checkUserId(userId);

        this.id = id;
        this.userId = userId;
        name = "Dish #" + id;
        unit = "килограмм";
        ingredients = new ArrayList<>();
        tags = new HashSet<>();
        readonlyTags = Collections.unmodifiableSet(tags);
    }

    /**
     * Устанавливает наименование для данного блюда. Указанное наименование будет сохранено без начальных и
     * конечных пробельных символов.
     * @param name наименование для данного блюда.
     * @throws NullPointerException если name имеет значение null.
     * @throws BlankValueException если name не содержит ни одного отображаемого символа.
     */
    public void setName(String name) {
        checkName(name);
        this.name = name.trim();
    }

    /**
     * Задает наименование единицы измерения кол-ва для данного блюда. Заданное значение будет сохранено без
     * начальных и конечных пробельных символов.
     * @param unit наименование единицы измерения кол-ва для данного блюда.
     * @throws NullPointerException если unit имеет значение null.
     * @throws BlankValueException если unit не содержит ни одного отображаемого символа.
     */
    public void setUnit(String unit) {
        checkUnit(unit);
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
     * в свою очередь, представленно ограничением constraint). Для добавляемого ингредиента будет указанно
     * его кол-во и наименование. Если блюдо уже содержит ингредиент с таким наименованием, то для него будут
     * перехаписаны огрнаинчения и кол-во.
     * @param name наименование ингредиента.
     * @param constraint ограничение задающее множество взаимозаменяемых продуктов, каждое из которых может
     *                   выступать данным конкретным ингредиентом этого блюда.
     * @param quantity кол-во соответсвующего ингредиента.
     * @throws OutOfRangeException если quantity меньше или равен нулю.
     * @throws NullPointerException если один из параметров имеет значение null.
     * @throws BlankValueException если наименование ингредиент null или пустая строка.
     */
    public void putIngredient(String name, Constraint constraint, BigDecimal quantity) {
        checkIngredientName(name);
        checkConstraint(constraint);
        checkIngredientQuantity(quantity);

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
     * @throws NullPointerException если указанный тег имеет значние null.
     * @throws DuplicateTagException если указанный уже присутсвует в объекте.
     */
    public void addTag(Tag tag) {
        checkTag(tag);
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
     * Возвращает уникальный иднетификатор пользвоателя, с которым связанно данное блюдо.
     * @return уникальный иднетификатор пользвоателя, с которым связанно данное блюдо.
     */
    public UUID getUserId() {
        return userId;
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
     * Возвращает все ингредиенты данного блюда в виде списка доуступного только для чтения.
     * @return все ингрединты данного блюда.
     */
    public List<DishIngredient> getReadonlyIngredients() {
        return null;
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
     * Проверяет, содержится ли указанный тег в данном блюде.
     * @param tag искомый тег.
     * @return true - если указанный тег содержится в данном блюде, иначе - false.
     */
    public boolean containsTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Возвращает все теги указанные для данного блюда. Возвращаемое множество доступно только для чтения.
     * @return все теги указанные для данного блюда.
     */
    public Set<Tag> getReadonlyTags() {
        return readonlyTags;
    }

    /**
     * Возвращает кол-во всех возможных комбинаций состава данного блюда. Если для данного блюда не было
     * указанно ни одного ингредиента - возвращает 0.
     * @param repository репозиторий продуктов.
     * @return кол-во всех возможных комбинаций состава данного блюда.
     */
    public BigInteger getNumberIngredientCombinations(ProductRepository repository) {
        return null;
    }

    /**
     * Возвращает стоимость данного блюда с учетом конкретных продуктов выбранных как его ингредиенты и
     * кол-во порций.
     * @param servingNumber кол-во порций блюда для которых рассчитывается общая стоимость.
     * @param ingredientsIndexes набор индексов продуктов, где каждое число в списке соответсвует одному конкретному
     *                           продукту, а индекс этого числа - конкретному ингредиенту блюда.
     * @return цена данного блюда или пустой Optional, если блюдо не содержит ни одного ингредиента.
     */
    public Optional<BigDecimal> getPrice(BigDecimal servingNumber, List<Integer> ingredientsIndexes) {
        return null;
    }

    /**
     * Возвращает среднюю арифметическую цену для данного блюда. Если для блюда не было указанно ни одного
     * ингредиента - возвращает пустой Optional.
     * @param repository репозиторий продуктов.
     * @param mc ограничения на точность и округление при вычислении средней арифметической цены блюда.
     * @return средня арифметическая цена данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice(ProductRepository repository, MathContext mc) {
        return Optional.empty();
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
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }


    private void checkId(UUID id) {
        Objects.requireNonNull(id, "Dish id can't be null.");
    }

    private void checkUserId(UUID userId) {
        Objects.requireNonNull(userId, "Dish userId can't be null.");
    }

    private void checkName(String name) {
        Objects.requireNonNull(name, "Dish name can not be null.");

        if(name.isBlank())
            throw new BlankValueException("Dish name can not be blank", getClass(), "name");
    }

    private void checkUnit(String unit) {
        Objects.requireNonNull(unit, "Dish unit can not be null.");

        if(unit.isBlank())
            throw new BlankValueException("Dish unit can not be blank", getClass(), "unit");
    }

    private void checkConstraint(Constraint constraint) {
        Objects.requireNonNull(constraint, "Dish constraint can not be null.");
    }

    private void checkIngredientName(String name) {
        Objects.requireNonNull(name, "Dish ingredient name can't be null.");

        if(name.isBlank())
            throw new BlankValueException("Dish ingredient name can't be blank.", getClass(), "Dish ingredient name");
    }

    private void checkIngredientQuantity(BigDecimal quantity) {
        Objects.requireNonNull(quantity, "Quantity of dish ingredient can not be null.");

        if(quantity.signum() <= 0) {
            throw new OutOfRangeException("Quantity of dish ingredient must be positive.", getClass(), "quantity");
        }
    }

    private void checkTag(Tag tag) {
        Objects.requireNonNull(tag, "Dish tag can not be null.");

        if(tags.contains(tag))
            throw new DuplicateTagException(
                    "This tag is already specified for the dish.",
                    getClass(),
                    "tag",
                    tag);
    }

}

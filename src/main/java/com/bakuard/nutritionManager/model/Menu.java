package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.exceptions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.URL;
import java.util.*;

/**
 * Представляет собой меню - группу из неповторяющихся блюд, где для каждого блюда указанно его кол-во.
 */
public class Menu {

    private final UUID id;
    private final UUID userId;
    private String name;
    private String description;
    private URL imagePath;
    private Map<UUID, BigDecimal> dishes;

    Menu(UUID id, UUID userId) {


        this.id = id;
        this.userId = userId;
        name = "Menu #" + id;
        dishes = new LinkedHashMap<>();
    }

    /**
     * Устанавливает наименование для данного меню. Указанное наименование будет сохраненно без начальных
     * и конечных символов.
     * @param name наименование для данного меню.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа
     */
    public void setName(String name) {

    }

    /**
     * Задает наименование для данного меню. Метод может принимать значение null.
     * @param description наименование для данного меню.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Задает путь к изображению данного меню. Задаваемый путь не обязательно должен быть путем в файловой
     * системе. Метод может принимать значение null.
     * @param imagePath путь к изображению данного меню.
     */
    public void setImagePath(String imagePath) {

    }

    /**
     * Добавляет блюдо с указанным идентификатором в данное меню и указывает для него кол-во. Если блюдо с
     * указанным идентификатором уже присутвует в меню, то метод перезаписывает для него кол-во на указанное.
     * @param dishId уникальный идентификатор блюда.
     * @param quantity кол-во блюда.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение меньше или равно нулю
     */
    public void putDish(UUID dishId, BigDecimal quantity) {

    }

    /**
     * Удаляет из данного меню блюдо с указанным идентификатором. Если в данном меню нет блюда с указанным
     * идентификатором или указанный идентификатор имеет значение null, то метод ничего не делает.
     * @param dishId уникальный идентификатор удаляемого блюда.
     */
    public void removeDish(UUID dishId) {
        dishes.remove(dishId);
    }

    /**
     * Возвращает уникальный идентификатор данного блюда.
     * @return уникальный идентификатор данного блюда.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает уникальный иднетификатор пользователя, с которым связанно данное меню.
     * @return уникальный иднетификатор пользователя, с которым связанно данное меню.
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Возвращает наименование данного меню.
     * @return наименование данного меню.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает описание к даннмоу меню.
     * @return описание к данному меню.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает путь к изображению данного меню.
     * @return путь к изображению данного меню.
     */
    public URL getImagePath() {
        return imagePath;
    }

    /**
     * Возвращает все блюда входящие в данное меню вместе с указанием их кол-ва. Кол-во каждого блюда входящего
     * в данное меню рассчитвается с учетом кол-ва меню, задаваемого параметром menuQuantity. Возвращаемые
     * данные представлены в виде неизменяемого объекта Map.
     * @param repository репозиторий блюд.
     * @param menuQuantity кол-во данного меню, для которого рассчитвается список всех входящих в него блюд и их
     *                     кол-во.
     * @return все блюда входящие в данное меню вместе с указанием их кол-ва.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение menuQuantity равняется null<br/>
     *         2. если указанное значение menuQuantity меньше или равно нулю
     */
    public Map<Dish, BigDecimal> getReadonlyDishes(DishRepository repository, BigDecimal menuQuantity) {
        return null;
    }

    /**
     * Проверяет - содержится ли в данном меню блюдо с таким идентификатором или нет.
     * @param dishId идентификатор искомого блюда.
     * @return true - если блюдо с таким идентификатором содержится в данном меню, иначе - false.
     */
    public boolean containsDish(UUID dishId) {
        return false;
    }

    /**
     * Возвращает кол-во всех возможных комбинаций состава данного меню по продуктам. Если для данного меню
     * не было указано ни одного блюда, или для блюд этого меню не было указано ни одного продукта - возвращает
     * 0.
     * @param repository репозиторий продуктов.
     * @return кол-во всех возможных комбинаций состава данного меню по продуктам.
     */
    public BigInteger getNumberIngredientCombinations(ProductRepository repository) {
        return null;
    }

    public List<MenuIngredient> getReadonlyIngredients(Map<UUID, List<Integer>> dishIngredientIndexes) {
        return null;
    }

    public BigDecimal getPrice(BigDecimal menuQuantity, List<MenuIngredient> menuIngredients) {
        return null;
    }

    /**
     * Возвращает среднюю арифметическую цену для данного меню. Если для меню не указанно ни одного блюда,
     * или для каждого из указанных блюд не указанно ни одного продукта, то метод возвращает пустой Optional.
     * @param repository репозиторий продуктов.
     * @param mc ограничения на точность и округление при вычислении средней арифметической цены меню.
     * @return средняя арифметическая цена данного меню.
     */
    public Optional<BigDecimal> getAveragePrice(ProductRepository repository, MathContext mc) {
        return Optional.empty();
    }

    /**
     * Сравнивает два объекта Menu. Два объекта считаются равными, если равны их идентификаторы ({@link #getId()}).
     * @param o сравниваемый объет Menu.
     * @return true - если объекты равны, иначе - false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return id.equals(menu.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", dishes=" + dishes +
                '}';
    }

}

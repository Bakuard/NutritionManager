package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.util.Page;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для агрегата {@link Dish}.
 */
public interface DishRepository {

    /**
     * Сохраняет данные указанного блюда в БД. Если в БД нет блюда с таким идентификатором, то добавляет его.
     * Если в БД есть блюдо с таким идентификатором - то обновляет его. В обоих случаях проверяется - есть ли
     * в БД у пользователя {@link Dish#getUser()} блюдо с таким же именем как и у переданного объекта. Если
     * ответ положительный - генерирует исключение.
     * @param dish сохраняемое блюдо.
     * @return true - если указанное блюдо отсутсвовало в БД или отличалось от переданного, иначе - false.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если dish имеет значение null.<br/>
     *         2. если в БД уже есть другое блюдо с таким же именем.
     */
    public boolean save(Dish dish);

    /**
     * Удаляет из БД блюдо идентификатор которого равен dishId. Если в БД нет блюда с таким
     * идентификатором - выбрасывает исключение.
     * @param dishId идентификатор блюда.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти блюдо с таким ID.<br/>
     *         2. если dishId равен null.
     */
    public Dish tryRemove(UUID dishId);

    /**
     * Возвращает блюдо по его идентификатору. Если в БД нет блюда с таким идентификатором -
     * возвращает пустой Optional.
     * @param dishId идентификатор блюда.
     * @return объект Dish.
     * @throws ValidateException если dishId равен null.
     */
    public Optional<Dish> getById(UUID dishId);

    /**
     * Возвращает блюдо по его имени. Если в БД нет блюда с таким именем - возвращает пустой Optional.
     * @param name уникальное имя блюда.
     * @return объект Dish.
     * @throws ValidateException если name равен null.
     */
    public Optional<Dish> getByName(String name);

    /**
     * Возвращает блюдо по его идентификатору. Если в БД нет блюда с таким идентификатором -
     * выбрасывает исключение.
     * @param dishId идентификатор блюда.
     * @return объект Dish.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти блюдо с таким ID.<br/>
     *         2. если dishId равен null.
     */
    public Dish tryGetById(UUID dishId);

    /**
     * Возвращает блюдо по его имени. Если в БД нет блюда с таким именем - выбрасывает исключение.
     * @param name уникальное имя блюда.
     * @return объект Dish.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти блюдо с таким name.<br/>
     *         2. если name равен null.
     */
    public Dish tryGetByName(String name);

    /**
     * Возвращает упорядоченную выборку блюд из множества всех блюд с учетом заданных ограничений
     * в виде criteria (см. {@link Criteria}).
     * @param criteria критерий формирования выборки блюд.
     * @return выборку блюд удовлетворяющую ограничениям criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<Dish> getDishes(Criteria criteria);

    /**
     * Возвращает выборку тегов блюд упорядоченную по значению в возрастающем порядке ({@link Tag#getValue()}).
     * Выборка будет формироваться только из тех тегов, блюда которых удовлетворяют ограничению criteria
     * (см. {@link Criteria}).
     * @param criteria критерий формирования выборки тегов.
     * @return выборку тегов удовлетворяющую ограничениям criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<Tag> getTags(Criteria criteria);

    /**
     * Возвращает выборку из наименований единиц измерения блюд удовлетворяющих ограничению criteria
     * (см. {@link Criteria}).
     * @param criteria критерий формирования выборки единиц измерения блюд.
     * @return выборку из единиц измерения блюд удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getUnits(Criteria criteria);

    /**
     * Возвращает выборку из наименований всех блюд удовлетворяющих ограничению criteria
     * (см. {@link Criteria}).
     * @param criteria критерий формирования выборки наименований блюд.
     * @return выборку из наименований блюд удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getNames(Criteria criteria);

    /**
     * Возвращает кол-во всех блюд удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return  кол-во всех блюд удовлетворяющих ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getDishesNumber(Criteria criteria);

    /**
     * Возвращает кол-во всех тегов блюд удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return  кол-во всех блюд удовлетворяющих ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getTagsNumber(Criteria criteria);

    /**
     * Возвращает общее число всех единиц измерения кол-ва блюд удовлетворяющих ограничению criteria
     * (см. {@link Criteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return общее число всех единиц измерения кол-ва блюд удовлетворяющих ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getUnitsNumber(Criteria criteria);

    /**
     * Возвращает общее число всех наименований блюд удовлетворяющих ограничению criteria
     * (см. {@link Criteria}).
     * @param criteria критерий указывающий какие наименования блюд подсчитывать.
     * @return общее число всех наименований блюд удовлетворяющих ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getNamesNumber(Criteria criteria);

}

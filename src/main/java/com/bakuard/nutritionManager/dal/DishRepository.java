package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.dal.criteria.dishes.*;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;
import com.bakuard.nutritionManager.model.util.Page;

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
     * @throws ServiceException если верно одно из следующих условий:<br/>
     *         1. если dish имеет значение null.<br/>
     *         2. если в БД уже есть другое блюдо с таким же именем.
     */
    public boolean save(Dish dish);

    /**
     * Удаляет из БД блюдо идентификатор которого равен dishId. Если в БД нет блюда с таким
     * идентификатором - выбрасывает исключение.
     * @param dishId идентификатор блюда.
     * @throws ServiceException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти блюдо с таким ID.<br/>
     *         2. если dishId равен null.
     */
    public Dish remove(UUID dishId);

    /**
     * Возвращает блюдо по его идентификатору. Если в БД нет блюда с таким идентификатором -
     * выбрасывает исключение.
     * @param dishId идентификатор блюда.
     * @return объект Dish или null.
     * @throws ServiceException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти блюдо с таким ID.<br/>
     *         2. если dishId равен null.
     */
    public Dish getById(UUID dishId);

    /**
     * Возвращает упорядоченную выборку блюд из множества всех блюд с учетом заданных ограничений
     * в виде criteria (см. {@link DishCriteria}).
     * @param criteria критерий формирования выборки блюд.
     * @return выборку блюд удовлетворяющую ограничениям criteria.
     * @throws ServiceException если criteria является null.
     */
    public Page<Dish> getDishes(DishCriteria criteria);

    /**
     * Возвращает выборку тегов блюд упорядоченную по значению в возрастающем порядке ({@link Tag#getValue()}).
     * Выборка будет формироваться только из тех тегов, блюда которых удовлетворяют ограничению criteria
     * (см. {@link DishCriteria}).
     * @param criteria критерий формирования выборки тегов.
     * @return выборку тегов удовлетворяющую ограничениям criteria.
     * @throws ServiceException если criteria является null.
     */
    public Page<Tag> getTags(DishFieldCriteria criteria);

    /**
     * Возвращает выборку из наименований единиц измерения блюд удовлетворяющих ограничению criteria
     * (см. {@link DishFieldCriteria}).
     * @param criteria критерий формирования выборки единиц измерения блюд.
     * @return выборку из единиц измерения блюд удовлетворяющую ограничению criteria.
     * @throws ServiceException если criteria является null.
     */
    public Page<String> getUnits(DishFieldCriteria criteria);

    /**
     * Возвращает кол-во всех блюд удовлетворяющих ограничению criteria (см. {@link DishesNumberCriteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return  кол-во всех блюд удовлетворяющих ограничению criteria.
     * @throws ServiceException если criteria является null.
     */
    public int getDishesNumber(DishesNumberCriteria criteria);

    /**
     * Возвращает кол-во всех тегов блюд удовлетворяющих ограничению criteria (см. {@link DishFieldNumberCriteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return  кол-во всех блюд удовлетворяющих ограничению criteria.
     * @throws ServiceException если criteria является null.
     */
    public int getTagsNumber(DishFieldNumberCriteria criteria);

    /**
     * Возвращает общее число всех единиц измерения кол-ва блюд удовлетворяющих ограничению criteria
     * (см. {@link DishFieldNumberCriteria}).
     * @param criteria критерий указывающий какие единицы измерения блюд подсчитывать.
     * @return общее число всех единиц измерения кол-ва блюд удовлетворяющих ограничению criteria.
     * @throws ServiceException если criteria является null.
     */
    public int getUnitsNumber(DishFieldNumberCriteria criteria);

}

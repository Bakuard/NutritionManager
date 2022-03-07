package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.Filter;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии подсчета продуктов. Все параметры фильтрации делятся на два типа: обязательные и не обязательные.
 * Обязательные параметры необходимо уакзать при создании объекта. Не обязательные параметры задаются с помощью set
 * методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет установленно
 * значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional). Все set
 * методы возвращают ссылку на тот же самый объект.
 */
public class ProductsNumberCriteria {

    /**
     * Создает и возвращает новый объект ProductsNumberCriteria.
     * @param user пользователь для продуктов которого ведется подсчет.
     * @return новый объект ProductsNumberCriteria.
     * @throws ValidateException если user имеет значение null.
     */
    public static ProductsNumberCriteria of(User user) {
        return new ProductsNumberCriteria(user);
    }


    private User user;
    private boolean onlyFridge;
    private Filter filter;

    private ProductsNumberCriteria(User user) {
        Checker.of(getClass(), "constructor").
                notNull("user", user).
                validate();

        this.user = user;
    }

    /**
     * Возвращает пользователя для продуктов которого ведется подсчет.
     * @return пользователь для продуктов которого ведется подсчет.
     */
    public User getUser() {
        return user;
    }

    /**
     * Указывает нужно ли учитывать только те продукты, которые есть в наличии у пользователя
     * (т.е. {@link Product#getQuantity()} у них возвращает значения больше 0). Значение по умолчанию - false.
     * @param onlyFridge указывает нужно ли учитывать только прдукты в наличии у пользователя.
     * @return этот же объект.
     */
    public ProductsNumberCriteria setOnlyFridge(Boolean onlyFridge) {
        this.onlyFridge = onlyFridge != null && onlyFridge;
        return this;
    }

    /**
     * Проверяет - нужно ли учитывать только те продукты, которые есть в наличии у пользователя
     * (т.е. {@link Product#getQuantity()} у них возвращает значения больше 0).
     * @return true - если нужно учитывать только те продукты, которые есть в наличии у пользователя, false -
     *                в противном случае.
     */
    public boolean isOnlyFridge() {
        return onlyFridge;
    }

    /**
     * Устанавливает ограничения продуктов для которых ведется подсчет (подробнее см. {@link Filter} и его подтипы).
     * Значение по умолчанию - пустой Optional (т.е. подсчет идет для всех продуктов пользвателя, если
     * не учитывать другие параметры данного объекта).
     * @param filter ограничения для подсчитываемых продуктов.
     * @return этот же объект.
     */
    public ProductsNumberCriteria setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Возвращает ограничения для отбираемых продуктов (подробнее см. {@link Filter} и его подтипы).
     * @return ограничения для отбираемых продуктов.
     */
    public Optional<Filter> getFilter() {
        return Optional.ofNullable(filter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductsNumberCriteria that = (ProductsNumberCriteria) o;
        return onlyFridge == that.onlyFridge &&
                Objects.equals(user, that.user) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, onlyFridge, filter);
    }

    @Override
    public String toString() {
        return "ProductsNumberCriteria{" +
                "user=" + user +
                ", onlyFridge=" + onlyFridge +
                ", constraint=" + filter +
                '}';
    }

}

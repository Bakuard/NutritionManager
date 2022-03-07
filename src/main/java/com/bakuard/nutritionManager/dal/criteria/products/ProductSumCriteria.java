package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.Filter;

import java.util.Objects;

/**
 * Задает критерии выборки продуктов участвующих в подсчете их общей денежной стоимости. Все параметры фильтрации
 * делятся на два типа: обязательные и не обязательные. Обязательные параметры необходимо укзать при создании объекта.
 * Не обязательные параметры задаются с помощью set методов. Если такой метод не вызывать или передать ему null -
 * для не обязательного параметра будет установленно значение по умолчанию (это может некоторое специальное значение
 * определеннго типа или пустой Optional). Все set методы возвращают ссылку на тот же самый объект.
 */
public class ProductSumCriteria {

    /**
     * Создает и возвращает новый обеъект ProductSumCriteria.
     * @param filter фильтр определющий какие продукты должны участвовать в запросе.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект ProductCriteria.
     * @throws ValidateException если хотя бы один из параметров имеет значение null.
     */
    public static ProductSumCriteria of(User user, Filter filter) {
        return new ProductSumCriteria(user, filter);
    }


    private User user;
    private Filter filter;

    private ProductSumCriteria(User user, Filter filter) {
        Checker.of(getClass(), "constructor").
                notNull("user", user).
                notNull("filter", filter).
                validate("Fail to create ProductSumCriteria");

        this.user = user;
        this.filter = filter;
    }

    /**
     * Возвращает пользователя из данных которого будет делаться выборка.
     * @return пользователь из данных которого будет делаться выборка.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает ограничения для отбираемых продуктов (подробнее см. {@link Filter} и его подтипы).
     * @return ограничения для отбираемых продуктов.
     */
    public Filter getFilter() {
        return filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSumCriteria that = (ProductSumCriteria) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, filter);
    }

    @Override
    public String toString() {
        return "ProductSumCriteria{" +
                "user=" + user +
                ", filter=" + filter +
                '}';
    }

}

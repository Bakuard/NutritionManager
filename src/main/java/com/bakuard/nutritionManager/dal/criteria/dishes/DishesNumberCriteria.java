package com.bakuard.nutritionManager.dal.criteria.dishes;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.Filter;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии подсчета блюд. Все параметры фильтрации делятся на два типа: обязательные и не обязательные.
 * Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются с помощью set
 * методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет установленно
 * значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional). Все set
 * методы возвращают ссылку на тот же самый объект.
 */
public class DishesNumberCriteria {

    /**
     * Создает и возвращает новый объект DishesNumberCriteria.
     * @param user пользователь для блюд которого ведется подсчет.
     * @return новый объект DishesNumberCriteria.
     * @throws ValidateException если user имеет значение null.
     */
    public static DishesNumberCriteria of(User user) {
        return new DishesNumberCriteria(user);
    }


    private User user;
    private Filter filter;

    private DishesNumberCriteria(User user) {
        Checker.of().
                notNull("user", user).
                validate();

        this.user = user;
    }

    /**
     * Возвращает пользователя для блюд которого ведется подсчет.
     * @return пользователь для блюд которого ведется подсчет.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает ограничения для отбираемых блюд (подробнее см. {@link Filter}).
     * @return ограничения для отбираемых блюд.
     */
    public Optional<Filter> getFilter() {
        return Optional.ofNullable(filter);
    }

    /**
     * Устанавливает ограничения блюд для которых ведется подсчет (подробнее см. {@link Filter}).
     * Значение по умолчанию - пустой Optional (т.е. подсчет идет для всех блюд пользвателя).
     * @param filter ограничения для подсчитываемых блюд.
     * @return этот же объект.
     */
    public DishesNumberCriteria setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishesNumberCriteria that = (DishesNumberCriteria) o;
        return user.equals(that.user) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, filter);
    }

    @Override
    public String toString() {
        return "DishesNumberCriteria{" +
                "user=" + user +
                ", filter=" + filter +
                '}';
    }

}

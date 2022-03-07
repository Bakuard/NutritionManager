package com.bakuard.nutritionManager.dal.criteria.dishes;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;

import java.util.Objects;

/**
 * Задает критерии подсчета по определенному полю блюд. Все параметры фильтрации делятся на два типа: обязательные
 * и не обязательные. Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются
 * с помощью set етодов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет
 * установленно значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional).
 * Все set методы возвращают ссылку на тот же самый объект.
 */
public class DishFieldNumberCriteria {

    /**
     * Создает и возвращает новый обеъект DishFieldNumberCriteria.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект DishFieldNumberCriteria.
     * @throws ValidateException если user имеет значение null.
     */
    public static DishFieldNumberCriteria of(User user) {
        return new DishFieldNumberCriteria(user);
    }


    private User user;

    private DishFieldNumberCriteria(User user) {
        Checker.of(getClass(), "constructor").
                notNull("user", user).
                validate();

        this.user = user;
    }

    /**
     * Возвращает пользователя блюд для значений выбранного поля которых ведется подсчет.
     * @return пользователь блюд для значений выбранного поля которых ведется подсчет.
     */
    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishFieldNumberCriteria that = (DishFieldNumberCriteria) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }

    @Override
    public String toString() {
        return "DishFieldNumberCriteria{" +
                "user=" + user +
                '}';
    }

}

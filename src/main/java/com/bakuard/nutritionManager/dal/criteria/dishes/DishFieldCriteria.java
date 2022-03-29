package com.bakuard.nutritionManager.dal.criteria.dishes;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.Objects;

/**
 * Задает критерии выборки по определенному полю блюд. Все параметры фильтрации делятся на два типа: обязательные и не
 * обязательные. Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются с
 * помощью set методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет
 * установленно значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional).
 * Все set методы возвращают ссылку на тот же самый объект.
 */
public class DishFieldCriteria {

    /**
     * Создает и возвращает новый объект DishFieldCriteria.
     * @param pageable параметры страницы использующиеся для пагинации.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый обеъект DishFieldCriteria.
     * @throws ValidateException если хотя бы один из параметров имеет значение null.
     */
    public static DishFieldCriteria of(User user, Pageable pageable) {
        return new DishFieldCriteria(user, pageable);
    }


    private User user;
    private Pageable pageable;

    private DishFieldCriteria(User user, Pageable pageable) {
        ValidateException.check(
                Rule.of("DishFieldCriteria.pageable").notNull(pageable),
                Rule.of("DishFieldCriteria.user").notNull(user)
        );

        this.user = user;
        this.pageable = pageable;
    }

    /**
     * Возвращает пользователя из данных которого будет делаться выборка.
     * @return пользователь из данных которого будет делаться выборка.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает заданные параметры пагинации.
     * @return заданные параметры пагинаци
     */
    public Pageable getPageable() {
        return pageable;
    }

    /**
     * Возвращает критерии используемые при подсчете уникальных значений определеннго поля блюд. Все параметры
     * возвращаемого критерия подсчета устанавливаются из соответствующих параметов текущего объекта.
     * @return критерии используемые при подсчете значений по определенному полю продуктов.
     */
    public DishFieldNumberCriteria getNumberCriteria() {
        return DishFieldNumberCriteria.of(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishFieldCriteria that = (DishFieldCriteria) o;
        return user.equals(that.user) &&
                pageable.equals(that.pageable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, pageable);
    }

    @Override
    public String toString() {
        return "DishFieldCriteria{" +
                "user=" + user +
                ", pageable=" + pageable +
                '}';
    }

}

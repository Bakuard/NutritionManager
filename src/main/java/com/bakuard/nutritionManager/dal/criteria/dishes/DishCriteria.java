package com.bakuard.nutritionManager.dal.criteria.dishes;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии выборки блюд. Все параметры фильтрации делятся на два типа: обязательные и не обязательные.
 * Обязательные параметры необходимо укзать при создании объекта. Не обязательные параметры задаются с помощью set
 * методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет установленно
 * значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional). Все set
 * методы возвращают ссылку на тот же самый объект.
 */
public class DishCriteria {

    /**
     * Создает и возвращает новый обеъект DishCriteria.
     * @param pageable параметры страницы исползующиеся для пагинации.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект DishCriteria.
     * @throws ValidateException если хотя бы один из параметров имеет значение null.
     */
    public static DishCriteria of(Pageable pageable, User user) {
        return new DishCriteria(pageable, user);
    }


    private Pageable pageable;
    private User user;
    private Filter filter;
    private Sort order;

    private DishCriteria(Pageable pageable, User user) {
        ValidateException.check(
                Rule.of("DishCriteria.pageable").notNull(pageable),
                Rule.of("DishCriteria.user").notNull(user)
        );

        this.pageable = pageable;
        this.user = user;
        order = Sort.dishDefaultSort();
    }

    /**
     * Возвращает заданные параметры пагинации.
     * @return заданные параметры пагинации.
     */
    public Pageable getPageable() {
        return pageable;
    }

    /**
     * Возвращает пользователя из данных которого будет делаться выборка.
     * @return пользователь из данных которого будет делаться выборка.
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
     * Возвращает правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return правило сортировки по которой формируется и сортируется итоговая выборка.
     */
    public Sort getDishSort() {
        return order;
    }

    /**
     * Возвращает критерии используемые при подсчете блюд. Все параметры возвращаемого критерия подсчета
     * блюд устанавливаются из соответствующих параметов текущего объекта.
     * @return критерии используемые при подсчете блюд.
     */
    public DishesNumberCriteria getNumberCriteria() {
        return DishesNumberCriteria.of(user).
                setFilter(filter);
    }

    /**
     * Устанавливает ограничения для отбираемых блюд (подробнее см. {@link Filter}).
     * Значение по умолчанию - пустой Optional (т.е. выборка формируется из всех блюд пользователя).
     * @param filter ограничения для отбираемых блюд.
     * @return этот же объект.
     */
    public DishCriteria setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Устанавливает правило сортировки по которой формируется и сортируется итоговая выборка. Значение по умолчанию
     * {@link Sort#dishDefaultSort()} ()}. Если два блюда одинаковы с точки зрения правила сортировки, то они
     * сортируются по их ID.
     * @param order правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return этот же объект.
     */
    public DishCriteria setDishSort(Sort order) {
        if(order != null) this.order = order;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishCriteria that = (DishCriteria) o;
        return pageable.equals(that.pageable) &&
                user.equals(that.user) &&
                Objects.equals(filter, that.filter) &&
                Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, user, filter, order);
    }

    @Override
    public String toString() {
        return "DishCriteria{" +
                "pageable=" + pageable +
                ", user=" + user +
                ", filter=" + filter +
                ", order=" + order +
                '}';
    }

}

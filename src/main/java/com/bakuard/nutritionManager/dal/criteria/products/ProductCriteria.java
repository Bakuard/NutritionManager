package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии выборки продуктов. Все параметры фильтрации делятся на два типа: обязательные и не обязательные.
 * Обязательные параметры необходимо укзать при создании объекта. Не обязательные параметры задаются с помощью set
 * методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет установленно
 * значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional). Все set
 * методы возвращают ссылку на тот же самый объект.
 */
public class ProductCriteria {

    /**
     * Создает и возвращает новый обеъект ProductCriteria.
     * @param pageable параметры страницы исползующиеся для пагинации.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект ProductCriteria.
     * @throws ValidateException если хотя бы один из параметров имеет значение null.
     */
    public static ProductCriteria of(Pageable pageable, User user) {
        return new ProductCriteria(pageable, user);
    }


    private Pageable pageable;
    private User user;
    private boolean onlyFridge;
    private Filter filter;
    private Sort order;

    private ProductCriteria(Pageable pageable, User user) {
        ValidateException.check(
                Rule.of("ProductCriteria.pageable").notNull(pageable),
                Rule.of("ProductCriteria.user").notNull(user)
        );

        this.pageable = pageable;
        this.user = user;
        order = Sort.productDefaultSort();
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
     * Указывает нужно ли учитывать только те продукты, которые есть в наличии у пользователя
     * (т.е. {@link Product#getQuantity()} у них возвращает значения больше 0). Значение по умолчанию - false.
     * @param onlyFridge указывает нужно ли учитывать только прдукты в наличии у пользователя.
     * @return этот же объект.
     */
    public ProductCriteria setOnlyFridge(Boolean onlyFridge) {
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
     * Устанавливает ограничения для отбираемых продуктов (подробнее см. {@link Filter} и его подтипы).
     * Значение по умолчанию - пустой Optional (т.е. выборка формируется из всех продуктов пользвателя, если
     * не учитывать другие параметры данного объекта).
     * @param filter ограничения для отбираемых продуктов.
     * @return этот же объект.
     */
    public ProductCriteria setFilter(Filter filter) {
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

    /**
     * Устанавливает правило сортировки по которой формируется и сортируется итоговая выборка. Значение по умолчанию
     * {@link Sort#productDefaultSort()} ()}. Если два продукта одинаковы с точки зрения правила сортировки, то они
     * сортируются по их ID.
     * @param order правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return этот же объект.
     */
    public ProductCriteria setProductSort(Sort order) {
        if(order != null) this.order = order;
        return this;
    }

    /**
     * Возвращает правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return правило сортировки по которой формируется и сортируется итоговая выборка.
     */
    public Sort getProductSort() {
        return order;
    }

    /**
     * Возвращает критерии используемые при подсчете продуктов. Все параметры возвращаемого критерия подсчета
     * продуктов устанавливаются из соответствующих параметов текущего объекта.
     * @return критерии используемые при подсчете продуктов.
     */
    public ProductsNumberCriteria getNumberCriteria() {
        return ProductsNumberCriteria.of(user).
                setOnlyFridge(onlyFridge).
                setFilter(filter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCriteria that = (ProductCriteria) o;
        return onlyFridge == that.onlyFridge &&
                pageable.equals(that.pageable) &&
                user.equals(that.user) &&
                Objects.equals(filter, that.filter) &&
                Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, user, onlyFridge, filter, order);
    }

    @Override
    public String toString() {
        return "ProductCriteria{" +
                "pageable=" + pageable +
                ", user=" + user +
                ", onlyFridge=" + onlyFridge +
                ", constraint=" + filter +
                ", order=" + order +
                '}';
    }

}

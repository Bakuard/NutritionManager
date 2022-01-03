package com.bakuard.nutritionManager.dal.criteria;

import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.filters.Constraint;
import com.bakuard.nutritionManager.model.filters.ProductSort;
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
     * @return новый обеъект ProductCriteria.
     * @throws MissingValueException если хотя бы один из параметров имеет значение null.
     */
    public static ProductCriteria of(Pageable pageable, User user) {
        return new ProductCriteria(pageable, user);
    }


    private Pageable pageable;
    private User user;
    private boolean onlyFridge;
    private Constraint constraint;
    private ProductSort order;

    private ProductCriteria(Pageable pageable, User user) {
        MissingValueException.check(pageable, getClass(), "pageable");
        MissingValueException.check(user, getClass(), "user");

        this.pageable = pageable;
        this.user = user;
        order = ProductSort.defaultSort();
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
     * Устанавливает ограничения для отбираемых продуктов (подробнее см. {@link Constraint} и его подтипы).
     * Значение по умолчанию - пустой Optional (т.е. выборка формируется из всех продуктов пользвателя, если
     * не учитывать другие параметры данного объекта).
     * @param constraint ограничения для отбираемых продуктов.
     * @return этот же объект.
     */
    public ProductCriteria setConstraint(Constraint constraint) {
        this.constraint = constraint;
        return this;
    }

    /**
     * Возвращает ограничения для отбираемых продуктов (подробнее см. {@link Constraint} и его подтипы).
     * @return ограничения для отбираемых продуктов.
     */
    public Optional<Constraint> getConstraint() {
        return Optional.ofNullable(constraint);
    }

    /**
     * Устанавливает правило сортировки по которой формируется и сортируется итоговая выборка. Значение по умолчанию
     * {@link ProductSort#defaultSort()}.
     * @param order правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return этот же объект.
     */
    public ProductCriteria setProductSort(ProductSort order) {
        if(order != null) this.order = order;
        return this;
    }

    /**
     * Возвращает правило сортировки по которой формируется и сортируется итоговая выборка.
     * @return правило сортировки по которой формируется и сортируется итоговая выборка.
     */
    public ProductSort getProductSort() {
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
                setConstraint(constraint);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCriteria that = (ProductCriteria) o;
        return onlyFridge == that.onlyFridge &&
                Objects.equals(pageable, that.pageable) &&
                Objects.equals(user, that.user) &&
                Objects.equals(constraint, that.constraint) &&
                Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, user, onlyFridge, constraint, order);
    }

    @Override
    public String toString() {
        return "ProductCriteria{" +
                "pageable=" + pageable +
                ", user=" + user +
                ", onlyFridge=" + onlyFridge +
                ", constraint=" + constraint +
                ", order=" + order +
                '}';
    }

}

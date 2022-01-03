package com.bakuard.nutritionManager.dal.criteria;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии подсчета по определенному полю продуктов. Все параметры фильтрации делятся на два типа: обязательные
 * и не обязательные. Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются
 * с помощью set етодов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет
 * установленно значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional).
 * Все set методы возвращают ссылку на тот же самый объект.
 */
public class ProductFieldNumberCriteria {

    /**
     * Создает и возвращает новый обеъект ProductFieldNumberCriteria.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект ProductFieldNumberCriteria.
     * @throws MissingValueException если user имеет значение null.
     */
    public static ProductFieldNumberCriteria of(User user) {
        return new ProductFieldNumberCriteria(user);
    }


    private User user;
    private String productCategory;

    private ProductFieldNumberCriteria(User user) {
        MissingValueException.check(user, getClass(), "user");

        this.user = user;
    }

    /**
     * Возвращает пользователя для магазинов продуктов которого ведется подсчет.
     * @return пользователь для магазинов продуктов которого ведется подсчет.
     */
    public User getUser() {
        return user;
    }

    /**
     * Задает категорию продуктов. Подсчет будет вестись только для магазинов продуктов указанной категории.
     * Значение по умолчанию пустой Optional (т.е. в выборку попадут магазины продуктов любой категории).
     * @param productCategory категория продуктов.
     * @return этот же объект.
     */
    public ProductFieldNumberCriteria setProductCategory(String productCategory) {
        this.productCategory = productCategory;
        return this;
    }

    /**
     * Возвращает категорию продуктов для значений выбранного поля которых ведется подсчет.
     * @return категорию продуктов.
     */
    public Optional<String> getProductCategory() {
        return Optional.ofNullable(productCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFieldNumberCriteria that = (ProductFieldNumberCriteria) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(productCategory, that.productCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, productCategory);
    }

    @Override
    public String toString() {
        return "NumberShopCriteria{" +
                "user=" + user +
                ", productCategory='" + productCategory + '\'' +
                '}';
    }

}

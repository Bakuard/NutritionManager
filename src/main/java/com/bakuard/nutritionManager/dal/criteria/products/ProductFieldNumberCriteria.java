package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;

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
     * @throws ServiceException если user имеет значение null.
     */
    public static ProductFieldNumberCriteria of(User user) {
        return new ProductFieldNumberCriteria(user);
    }


    private User user;
    private String productCategory;

    private ProductFieldNumberCriteria(User user) {
        Checker.of(getClass(), "constructor").
                nullValue("user", user).
                checkWithServiceException();

        this.user = user;
    }

    /**
     * Возвращает пользователя продуктов для значений выбранного поля которых ведется подсчет.
     * @return пользователь продуктов для значений выбранного поля которых ведется подсчет.
     */
    public User getUser() {
        return user;
    }

    /**
     * Задает категорию продуктов. Подсчет будет вестись только значений поля продуктов указанной категории.
     * Значение по умолчанию пустой Optional (т.е. в выборку попадут продукты любой категории).
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

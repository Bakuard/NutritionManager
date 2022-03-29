package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.AnyFilter;
import com.bakuard.nutritionManager.model.filters.Filter;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии подсчета по определенному полю продуктов. Все параметры фильтрации делятся на два типа: обязательные
 * и не обязательные. Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются
 * с помощью set методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет
 * установленно значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional).
 * Все set методы возвращают ссылку на тот же самый объект.
 */
public class ProductFieldNumberCriteria {

    /**
     * Создает и возвращает новый обеъект ProductFieldNumberCriteria.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый объект ProductFieldNumberCriteria.
     * @throws ValidateException если user имеет значение null.
     */
    public static ProductFieldNumberCriteria of(User user) {
        return new ProductFieldNumberCriteria(user);
    }


    private User user;
    private AnyFilter productCategory;

    private ProductFieldNumberCriteria(User user) {
        ValidateException.check(
                Rule.of("ProductFieldNumberCriteria.user").notNull(user)
        );

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
     * Задает категории продуктов. В подсчете будут участвовать продукты только указанных категорий.
     * Значение по умолчанию пустой Optional (т.е. в выборку попадут продукты любой категории).
     * @param productCategory категории продуктов.
     * @return этот же объект.
     * @throws IllegalArgumentException если productCategory.getType() имеет значение отличное от Filter.Type.CATEGOR
     */
    public ProductFieldNumberCriteria setProductCategory(AnyFilter productCategory) {
        if(productCategory != null && productCategory.getType() != Filter.Type.CATEGORY) {
            throw new IllegalArgumentException("productCategory.getType() must return Filter.Type.CATEGORY");
        }

        this.productCategory = productCategory;
        return this;
    }

    /**
     * Возвращает категорию продуктов для значений выбранного поля которых ведется подсчет.
     * @return категорию продуктов.
     */
    public Optional<AnyFilter> getProductCategory() {
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

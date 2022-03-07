package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.AnyFilter;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.Objects;
import java.util.Optional;

/**
 * Задает критерии выборки по определенному полю продуктов. Все параметры фильтрации делятся на два типа: обязательные и не
 * обязательные. Обязательные параметры необходимо указать при создании объекта. Не обязательные параметры задаются с
 * помощью set методов. Если такой метод не вызывать или передать ему null - для не обязательного параметра будет
 * установленно значение по умолчанию (это может некоторое специальное значение определеннго типа или пустой Optional).
 * Все set методы возвращают ссылку на тот же самый объект.
 */
public class ProductFieldCriteria {

    /**
     * Создает и возвращает новый объект ProductFieldCriteria.
     * @param pageable параметры страницы использующиеся для пагинации.
     * @param user пользователь из данных которого будет формироваться выборка.
     * @return новый обеъект ProductFieldCriteria.
     * @throws ValidateException если хотя бы один из параметров имеет значение null.
     */
    public static ProductFieldCriteria of(Pageable pageable, User user) {
        return new ProductFieldCriteria(pageable, user);
    }


    private Pageable pageable;
    private User user;
    private AnyFilter productCategory;

    private ProductFieldCriteria(Pageable pageable, User user) {
        Checker.of().
                notNull("pageable", pageable).
                notNull("user", user).
                validate();

        this.pageable = pageable;
        this.user = user;
    }

    /**
     * Возвращает заданные параметры пагинации.
     * @return заданные параметры пагинаци
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
     * Задает категорию продуктов. Итоговая выборка будет формироваться только из продуктов указанной категории.
     * Значение по умолчанию пустой Optional (т.е. в выборка будет формироваться из продуктов любых категорий).
     * @param productCategory категория продуктов.
     * @return этот же объект.
     */
    public ProductFieldCriteria setProductCategory(AnyFilter productCategory) {
        if(productCategory != null && productCategory.getType() != Filter.Type.CATEGORY) {
            throw new IllegalArgumentException("productCategory.getType() must return Filter.Type.CATEGORY");
        }

        this.productCategory = productCategory;
        return this;
    }

    /**
     * Возвращает категорию продуктов по которой делается выборка.
     * @return категорию продуктов.
     */
    public Optional<AnyFilter> getProductCategory() {
        return Optional.ofNullable(productCategory);
    }

    /**
     * Возвращает критерии используемые при подсчете уникальных значений определенног поля продуктов. Все параметры
     * возвращаемого критерия подсчета устанавливаются из соответствующих параметов текущего объекта.
     * @return критерии используемые при подсчете значений по определенному полю продуктов.
     */
    public ProductFieldNumberCriteria getNumberCriteria() {
        return ProductFieldNumberCriteria.of(user).setProductCategory(productCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFieldCriteria that = (ProductFieldCriteria) o;
        return Objects.equals(pageable, that.pageable) &&
                Objects.equals(user, that.user) &&
                Objects.equals(productCategory, that.productCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, user, productCategory);
    }

    @Override
    public String toString() {
        return "ShopCriteria{" +
                "pageable=" + pageable +
                ", user=" + user +
                ", productCategory='" + productCategory + '\'' +
                '}';
    }

}

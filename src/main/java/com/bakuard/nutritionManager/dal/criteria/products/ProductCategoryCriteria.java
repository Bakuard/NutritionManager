package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.Objects;

public class ProductCategoryCriteria {

    public static ProductCategoryCriteria of(Pageable pageable, User user) {
        return new ProductCategoryCriteria(pageable, user);
    }


    private Pageable pageable;
    private User user;

    private ProductCategoryCriteria(Pageable pageable, User user) {
        Checker.of().
                notNull("pageable", pageable).
                notNull("user", user).
                validate();

        this.pageable = pageable;
        this.user = user;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public User getUser() {
        return user;
    }

    public ProductCategoryNumberCriteria getNumberCriteria() {
        return ProductCategoryNumberCriteria.of(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategoryCriteria that = (ProductCategoryCriteria) o;
        return pageable.equals(that.pageable) &&
                user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, user);
    }

    @Override
    public String toString() {
        return "ProductCategoryCriteria{" +
                "pageable=" + pageable +
                ", user=" + user +
                '}';
    }

}

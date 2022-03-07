package com.bakuard.nutritionManager.dal.criteria.products;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;

import java.util.Objects;

public class ProductCategoryNumberCriteria {

    public static ProductCategoryNumberCriteria of(User user) {
        return new ProductCategoryNumberCriteria(user);
    }


    private User user;

    private ProductCategoryNumberCriteria(User user) {
        Checker.of(getClass(), "constructor").
                notNull("user", user).
                validate();

        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCategoryNumberCriteria that = (ProductCategoryNumberCriteria) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }

    @Override
    public String toString() {
        return "ProductCategoryNumberCriteria{" +
                "user=" + user +
                '}';
    }

}

package com.bakuard.nutritionManager.dal.criteria;

import com.bakuard.nutritionManager.model.User;

import java.util.Objects;

public class ProductCategoryNumberCriteria {

    public static ProductCategoryNumberCriteria of(User user) {
        return new ProductCategoryNumberCriteria(user);
    }


    private User user;

    private ProductCategoryNumberCriteria(User user) {
        MissingValueException.check(user, getClass(), "user");

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

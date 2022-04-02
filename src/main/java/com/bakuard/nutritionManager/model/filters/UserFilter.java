package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

import com.google.common.collect.ImmutableList;

public class UserFilter extends AbstractFilter {

    private final User user;

    UserFilter(User user) {
        ValidateException.check(
                Rule.of("UserFilter.user").notNull(user)
        );

        this.user = user;
    }

    @Override
    public Type getType() {
        return Type.USER;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFilter that = (UserFilter) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return "UserFilter{" +
                "user=" + user +
                '}';
    }

}

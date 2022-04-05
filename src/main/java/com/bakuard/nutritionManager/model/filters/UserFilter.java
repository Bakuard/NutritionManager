package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

import com.google.common.collect.ImmutableList;

import java.util.UUID;

public class UserFilter extends AbstractFilter {

    private final UUID userId;

    UserFilter(UUID userId) {
        ValidateException.check(
                Rule.of("UserFilter.userId").notNull(userId)
        );

        this.userId = userId;
    }

    @Override
    public Type getType() {
        return Type.USER;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFilter that = (UserFilter) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "UserFilter{" +
                "userId=" + userId +
                '}';
    }

}

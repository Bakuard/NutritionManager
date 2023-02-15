package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.Validator;

import com.google.common.collect.ImmutableList;

import java.util.Objects;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.notNull;

public class UserFilter extends AbstractFilter {

    private final UUID userId;

    UserFilter(UUID userId) {
        Validator.check("UserFilter.userId", notNull(userId));

        this.userId = userId;
    }

    @Override
    public Type getType() {
        return Type.USER;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFilter that = (UserFilter) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return getType() + "(" + userId + ')';
    }

}

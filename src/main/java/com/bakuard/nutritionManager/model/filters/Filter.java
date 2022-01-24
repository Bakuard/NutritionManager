package com.bakuard.nutritionManager.model.filters;

import com.google.common.collect.ImmutableList;

public interface Filter {

    public static enum Type {
        OR_ELSE,
        AND,
        MIN_TAGS,
        CATEGORY,
        SHOPS,
        VARIETIES,
        MANUFACTURER
    }

    public Type getType();

    public ImmutableList<Filter> getOperands();

}

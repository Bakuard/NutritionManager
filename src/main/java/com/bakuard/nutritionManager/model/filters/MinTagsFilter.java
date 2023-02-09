package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.validation.Validator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class MinTagsFilter extends AbstractFilter {

    private final ImmutableSortedSet<Tag> tags;

    MinTagsFilter(List<Tag> tags) {
        Validator.check(
                "MinTagsFilter.tags", notNull(tags).
                        and(() -> notContainsNull(tags)).
                        and(() -> min(tags.size(), 1))
        );

        this.tags = ImmutableSortedSet.copyOf(tags);
    }

    @Override
    public Type getType() {
        return Type.MIN_TAGS;
    }

    public ImmutableSortedSet<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinTagsFilter minTagsFilter = (MinTagsFilter) o;
        return tags.equals(minTagsFilter.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    @Override
    public String toString() {
        return getType().name() + tags;
    }

}

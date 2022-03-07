package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;

import com.bakuard.nutritionManager.model.exceptions.Validator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.util.List;
import java.util.Objects;

public class MinTagsFilter implements Filter {

    private final ImmutableSortedSet<Tag> tags;

    MinTagsFilter(List<Tag> tags) {
        Validator.create().
                notNull("tags", tags).
                notContainsNull("tags", tags).
                containsAtLeast("tags", tags, 1).
                validate();

        this.tags = ImmutableSortedSet.copyOf(tags);
    }

    @Override
    public Type getType() {
        return Type.MIN_TAGS;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
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
        return "MinTags{" +
                "tags=" + tags +
                '}';
    }

}

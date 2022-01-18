package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;

import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.FilterValidateException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MinTagsFilter implements Filter {

    /**
     * Создает и возвращает новый объект MinTags содержащий указанные теги.
     * @param tags теги для которых определяется создаваемое ограничение MinTags.
     * @return новый объект MinTags.
     * @throws FilterValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из элементов имеет значение null.<br/>
     *          2. если передаваемый массив имеет значение null.<br/>
     *          3. если передаваемый массив пустой
     */
    public static MinTagsFilter of(Tag... tags) {
        List<Tag> list = null;
        if(tags != null) list = Arrays.asList(tags);

        return new MinTagsFilter(list);
    }

    /**
     * Создает и возвращает новый объект MinTags содержащий указанные теги.
     * @param tags теги для которых определяется создаваемое ограничение MinTags.
     * @return новый объект MinTags.
     * @throws FilterValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотябы один из элементов имеет значение null.<br/>
     *          2. если передаваемый список имеет значение null.<br/>
     *          3. если передаваемый список пустой
     */
    public static MinTagsFilter of(List<Tag> tags) {
        return new MinTagsFilter(tags);
    }


    private final ImmutableSortedSet<Tag> tags;

    private MinTagsFilter(List<Tag> tags) {
        tryThrow(
                Constraint.check(getClass(), "tags",
                        Constraint.nullValue(tags),
                        Constraint.containsNull(tags),
                        Constraint.notEnoughItems(tags, 1))
        );

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


    private void tryThrow(Constraint constraint) {
        if(constraint != null) {
            FilterValidateException e = new FilterValidateException("Fail to update user.");
            e.addReason(constraint);
            throw e;
        }
    }

}

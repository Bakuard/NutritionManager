package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.NotEnoughItemsException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MinTags implements Constraint {

    /**
     * Создает и возвращает новый объект MinTags содержащий указанные теги.
     * @param tags теги для которых определяется создаваемое ограничение MinTags.
     * @return новый объект MinTags.
     * @throws MissingValueException если tags является null или любой из тегов в списке является null.
     * @throws NotEnoughItemsException если tags является пустым.
     */
    public static MinTags of(Tag... tags) {
        MissingValueException.check(tags, MinTags.class, "tags");
        return new MinTags(Arrays.asList(tags));
    }

    /**
     * Создает и возвращает новый объект MinTags содержащий указанные теги.
     * @param tags теги для которых определяется создаваемое ограничение MinTags.
     * @return новый объект MinTags.
     * @throws MissingValueException если tags является null или любой из тегов в списке является null.
     * @throws NotEnoughItemsException если tags является пустым.
     */
    public static MinTags of(List<Tag> tags) {
        return new MinTags(tags);
    }


    private final ImmutableSortedSet<Tag> tags;

    private MinTags(List<Tag> tags) {
        MissingValueException.check(tags, getClass(), "tags");
        tags.forEach(tag -> MissingValueException.check(tag, getClass(), "tag"));
        if(tags.isEmpty())
            throw new NotEnoughItemsException("tag list must contain at least one value", getClass(), "tags");

        this.tags = ImmutableSortedSet.copyOf(Objects.requireNonNull(tags, "tags can't be null"));
    }

    @Override
    public Type getType() {
        return Type.MIN_TAGS;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableSortedSet<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinTags minTags = (MinTags) o;
        return tags.equals(minTags.tags);
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

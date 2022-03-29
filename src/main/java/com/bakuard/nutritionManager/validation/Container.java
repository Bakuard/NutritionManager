package com.bakuard.nutritionManager.validation;

import java.util.Optional;
import java.util.function.Function;

public class Container<T> {

    private T value;

    public Container() {

    }

    public T get() {
        return value;
    }

    public Optional<T> getIfAllPresent(Container<?>... containers) {
        boolean allPresent = value != null;
        for(int i = 0; i < containers.length && allPresent; i++) {
            allPresent = containers[i].value != null;
        }
        if(allPresent) return Optional.of(value);
        else return Optional.empty();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return Optional.ofNullable(value).map(mapper);
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    public void clear() {
        value = null;
    }

}

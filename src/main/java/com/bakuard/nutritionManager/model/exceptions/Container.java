package com.bakuard.nutritionManager.model.exceptions;

public class Container<T> {

    private T value;
    private boolean isOpen;

    Container() {
        isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}

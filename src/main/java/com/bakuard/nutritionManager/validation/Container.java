package com.bakuard.nutritionManager.validation;

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

    public boolean isClose() {
        return !isOpen;
    }

    public T get() {
        if(isClose()) {
            throw new IllegalStateException("Container is close");
        }
        return value;
    }

    public void set(T value) {
        if(isClose()) {
            throw new IllegalStateException("Container is close");
        }
        this.value = value;
    }

}

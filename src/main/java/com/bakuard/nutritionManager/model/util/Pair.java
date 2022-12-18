package com.bakuard.nutritionManager.model.util;

public record Pair<T, V>(T first, V second) {

    @Override
    public String toString() {
        return "Pair{" + first + ", " + second + '}';
    }

}

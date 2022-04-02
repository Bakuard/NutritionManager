package com.bakuard.nutritionManager.model;

import java.util.UUID;

public interface Entity<T> {

    public UUID getId();

    public boolean equalsFullState(T other);

}

package com.bakuard.nutritionManager.model;

import java.util.UUID;

/**
 * Общий интерфейс для всех бизнес сущностей.
 * @param <T> конкретный тип бизнес сущности.
 */
public interface Entity<T> {

    /**
     * Возвращает уникальный идентификатор сущности. Идентификатор должен быть гарантированно уникальным
     * среди всех сущностей того же класса.
     * @return уникальный идентификатор сущности.
     */
    public UUID getId();

}

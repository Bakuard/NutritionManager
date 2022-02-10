package com.bakuard.nutritionManager.model.util;

import com.bakuard.nutritionManager.model.exceptions.ValidateException;

/**
 * Интерфейс строителей (паттерн "Builder") для бизнес сущностей.
 * @param <T>
 */
public interface AbstractBuilder<T> {

    /**
     * Пытается создать экземпляр класса представляющего бизнес сущность. Если все инварианты сущности
     * были соблюдены - сздает и возвращает новый объект для этой бизнес сущности, иначе - выбрасывает исключение
     * содержащее данные о ВСЕХ нарушенных инвариантах. Конкретный класс представляющий некоторую бизнес сущность
     * и её инварианты - определяются реалзиациями этого интерфейса.
     * @return новый объект для бизнес сущности
     * @throws ValidateException если был нарушен хотя бы один инвариант бизнес сущности.
     */
    public T tryBuild() throws ValidateException;

}

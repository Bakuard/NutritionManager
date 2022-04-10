package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.validation.ValidateException;

import java.util.UUID;

public interface Entity<T> {

    public UUID getId();

    public boolean equalsFullState(T other);


    /**
     * Интерфейс строителей (паттерн "Builder") для бизнес сущностей.
     * @param <T> тип объекта, для которого применяется этот паттерн.
     */
    public static interface Builder<T> {

        /**
         * Пытается создать экземпляр класса представляющего бизнес сущность. Если все инварианты сущности
         * были соблюдены - создает и возвращает новый объект для этой бизнес сущности, иначе - выбрасывает исключение
         * содержащее данные о ВСЕХ нарушенных инвариантах. Конкретный класс представляющий некоторую бизнес сущность
         * и её инварианты - определяются реалзиациями этого интерфейса.
         * @return новый объект для бизнес сущности
         * @throws ValidateException если был нарушен хотя бы один инвариант бизнес сущности.
         */
        public T tryBuild() throws ValidateException;

    }

}

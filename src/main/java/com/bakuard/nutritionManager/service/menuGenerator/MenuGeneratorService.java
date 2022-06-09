package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.validation.ValidateException;

public class MenuGeneratorService {

    public MenuGeneratorService() {

    }

    /**
     * Подбирает новое меню. При подборе нового меню используется критерий минимальной суммарной стоимости
     * продуктов необходимых для приготовления всех блюд этого меню. Входными данными для данного метода является
     * набор ограничений передаваемых этому методу в виде объекта input. <br/><br/>
     * <strong>ВАЖНО!</strong> В задачи этого метода НЕ входит сохранение созданного им меню.
     * @param input набор ограничений для подбираемого меню (подробнее см. {@link Input}).
     * @return новое меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если input равен null. <br/>
     *         2. Если невозможно подобрать меню с заданными ограничениями. <br/>
     */
    public Menu generate(Input input) {
        return null;
    }

}

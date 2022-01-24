package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.ProductContext;

import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Данное огрнаничение используется при фильтрации продуктов по сортам. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getVariety()} соответсвует любому из указаных в ограничении сортов.
 */
public class VarietiesFilter implements Filter {

    /**
     * Создает и возвращает ограничение VarietiesConstraint для указанного списка сортов продуктов.
     * @param varieties список сортов продуктов для котороо создается указанное ограничение.
     * @return ограничение VarietiesConstraint для указанного списка сортов продуктов.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *            1. если указанный список сортов или любой из сортов указанный в нем имеют
     *               значение null.<br/>
     *            2. если хотябы один из сортов в указанном списке не содержит ни одного отображаемого
     *               символа.<br/>
     *            3. если указанный список пуст.
     */
    public static VarietiesFilter of(List<String> varieties) {
        return new VarietiesFilter(varieties);
    }

    /**
     * Создает и возвращает ограничение VarietiesConstraint для указанных сортов продуктов.
     * @param variety первый обязательный сорт участвующий в создаваемом ограничении.
     * @param varieties не обязательные сорта участвующие в создаваемом ограничении.
     * @return ограничение VarietiesConstraint для указанных сортов продуктов.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если любой из сортов или массив manufacturers имеют значение null.<br/>
     *          2. если хотябы один из сортов не содержит ни одного отображаемого символа.
     */
    public static VarietiesFilter of(String variety, String... varieties) {
        List<String> list = new ArrayList<>();
        list.add(variety);
        if(varieties == null) list.add(null);
        else list.addAll(Arrays.asList(varieties));

        return new VarietiesFilter(list);
    }


    private ImmutableList<String> varieties;

    public VarietiesFilter(List<String> varieties) {
        Checker.of(getClass(), "varieties").
                nullValue("varieties", varieties).
                containsNull("varieties", varieties).
                notEnoughItems("varieties", varieties, 1).
                containsBlankValue("varieties", varieties).
                checkWithServiceException();

        this.varieties = ImmutableList.copyOf(varieties);
    }

    @Override
    public Type getType() {
        return Type.VARIETIES;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getVarieties() {
        return varieties;
    }

}

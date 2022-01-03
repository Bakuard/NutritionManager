package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.ProductContext;
import com.bakuard.nutritionManager.model.exceptions.BlankValueException;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.NotEnoughItemsException;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Данное огрнаничение используется при фильтрации продуктов по сортам. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getVariety()} соответсвует любому из указаных в ограничении сортов.
 */
public class VarietiesConstraint implements Constraint {

    /**
     * Создает и возвращает ограничение VarietiesConstraint для указанного списка сортов продуктов.
     * @param varieties список сортов продуктов для котороо создается указанное ограничение.
     * @return ограничение VarietiesConstraint для указанного списка сортов продуктов.
     * @throws MissingValueException если указанный список сортов продуктов или любой из сортов указанный в нем имеют
     *                               значение null.
     * @throws BlankValueException если хотябы один из сортов продуктов в указанном списке не содержит ни одного
     *                             отображаемого символа.
     * @throws NotEnoughItemsException если указанный список пуст.
     */
    public static VarietiesConstraint of(List<String> varieties) {
        return new VarietiesConstraint(varieties);
    }

    /**
     * Создает и возвращает ограничение VarietiesConstraint для указанных сортов продуктов.
     * @param variety первый обязательный сорт участвующий в создаваемом ограничении.
     * @param varieties не обязательные сорта участвующие в создаваемом ограничении.
     * @return ограничение VarietiesConstraint для указанных сортов продуктов.
     * @throws MissingValueException если любой из указанных сортов или массив varieties имеют значение null.
     * @throws BlankValueException если хотябы один из указанных сортов не содержит ни одного отображаемого символа.
     */
    public static VarietiesConstraint of(String variety, String... varieties) {
        return new VarietiesConstraint(variety, varieties);
    }


    private ImmutableList<String> varieties;

    public VarietiesConstraint(String variety, String... varieties) {
        MissingValueException.check(varieties, getClass(), "varieties");
        ArrayList<String> allVarieties= new ArrayList<>(Arrays.asList(varieties));
        allVarieties.add(variety);
        allVarieties.forEach(v -> {
            MissingValueException.check(v, getClass(), "variety");
            if(v.isBlank()) throw new BlankValueException("VarietiesConstraint variety can't be blank", getClass(), "variety");
        });

        this.varieties = ImmutableList.copyOf(allVarieties);
    }

    public VarietiesConstraint(List<String> varieties) {
        MissingValueException.check(varieties, getClass(), "varieties");
        varieties.forEach(shop -> {
            MissingValueException.check(shop, getClass(), "variety");
            if(shop.isBlank()) throw new BlankValueException(
                    "VarietiesConstraint variety can't be blank", getClass(), "variety");
        });
        if(varieties.isEmpty()) {
            throw new NotEnoughItemsException(
                    "variety list must contain at least one value", getClass(), "variety");
        }

        this.varieties = ImmutableList.copyOf(varieties);
    }

    @Override
    public Type getType() {
        return Type.VARIETIES;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getVarieties() {
        return varieties;
    }

}

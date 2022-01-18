package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.ProductContext;

import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.FilterValidateException;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Данное огрнаничение используется при фильтрации продуктов по магазинам. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getShop()} соответсвует любому из указаных в ограничении магазинов.
 */
public class ShopsFilter implements Filter {

    /**
     * Создает и возвращает ограничение ShopsConstraint для указанного списка магазинов.
     * @param shops список магазинов для котороо создается указанное ограничение.
     * @return ограничение ShopsConstraint для указанного списка магазинов.
     * @throws FilterValidateException если выполняется одно из следующих условий:<br/>
     *            1. если указанный список магазинов или любой из магазинов указанный в нем имеют
     *               значение null.<br/>
     *            2. если хотябы один из магазинов в указанном списке не содержит ни одного отображаемого
     *               символа.<br/>
     *            3. если указанный список пуст.
     */
    public static ShopsFilter of(List<String> shops) {
        return new ShopsFilter(shops);
    }

    /**
     * Создает и возвращает ограничение ShopsConstraint для указанных магазинов.
     * @param shop обязательный магазинов участвующий в создаваемом ограничении.
     * @param shops не обязательные магазины участвующие в создаваемом ограничении.
     * @return ограничение ShopsConstraint для указанных магазинов.
     * @throws FilterValidateException если выполняется одно из следующих условий:<br/>
     *          1. если любой из магазинов или массив manufacturers имеют значение null.<br/>
     *          2. если хотябы один из магазинов не содержит ни одного отображаемого символа.
     */
    public static ShopsFilter of(String shop, String... shops) {
        List<String> list = new ArrayList<>();
        list.add(shop);
        if(shops == null) list.add(null);
        else list.addAll(Arrays.asList(shops));

        return new ShopsFilter(list);
    }


    private final ImmutableList<String> shops;

    private ShopsFilter(List<String> shops) {
        tryThrow(
                Constraint.check(getClass(), "shops",
                        Constraint.nullValue(shops),
                        Constraint.containsNull(shops),
                        Constraint.notEnoughItems(shops, 1),
                        Constraint.containsBlank(shops))
        );

        this.shops = ImmutableList.copyOf(shops);
    }

    @Override
    public Type getType() {
        return Type.SHOPS;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getShops() {
        return shops;
    }


    private void tryThrow(Constraint constraint) {
        if(constraint != null) {
            FilterValidateException e = new FilterValidateException("Fail to update user.");
            e.addReason(constraint);
            throw e;
        }
    }

}

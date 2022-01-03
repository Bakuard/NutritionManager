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
 * Данное огрнаничение используется при фильтрации продуктов по магазинам. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getShop()} соответсвует любому из указаных в ограничении магазинов.
 */
public class ShopsConstraint implements Constraint {

    /**
     * Создает и возвращает ограничение ShopsConstraint для указанного списка магазинов.
     * @param shops список магазинов для котороо создается указанное ограничение.
     * @return ограничение ShopsConstraint для указанного списка магазинов.
     * @throws MissingValueException если указанный список магазинов или любой из магазинов указанный в нем имеют
     *                               значение null.
     * @throws BlankValueException если хотябы один из магазинов в указанном списке не содержит ни одного отображаемого
     *                             символа.
     * @throws NotEnoughItemsException если указанный список пуст.
     */
    public static ShopsConstraint of(List<String> shops) {
        return new ShopsConstraint(shops);
    }

    /**
     * Создает и возвращает ограничение ShopsConstraint для указанных магазинов.
     * @param shop обязательный магазинов участвующий в создаваемом ограничении.
     * @param shops не обязательные магазины участвующие в создаваемом ограничении.
     * @return ограничение ShopsConstraint для указанных магазинов.
     * @throws MissingValueException если любой из указанных магазинов или массив shops имеют значение null.
     * @throws BlankValueException если хотябы один из указанных магазинов не содержит ни одного отображаемого символа.
     */
    public static ShopsConstraint of(String shop, String... shops) {
        return new ShopsConstraint(shop, shops);
    }


    private final ImmutableList<String> shops;

    private ShopsConstraint(String shop, String... shops) {
        MissingValueException.check(shops, getClass(), "shops");
        ArrayList<String> allShops = new ArrayList<>(Arrays.asList(shops));
        allShops.add(shop);
        allShops.forEach(s -> {
            MissingValueException.check(s, getClass(), "shop");
            if(s.isBlank()) throw new BlankValueException("ShopsConstraint shop can't be blank", getClass(), "shop");
        });

        this.shops = ImmutableList.copyOf(allShops);
    }

    private ShopsConstraint(List<String> shops) {
        MissingValueException.check(shops, getClass(), "shops");
        shops.forEach(shop -> {
            MissingValueException.check(shop, getClass(), "shop");
            if(shop.isBlank()) throw new BlankValueException("ShopsConstraint shop can't be blank", getClass(), "shop");
        });
        if(shops.isEmpty()) {
            throw new NotEnoughItemsException("shop list must contain at least one value", getClass(), "shop");
        }

        this.shops = ImmutableList.copyOf(shops);
    }

    @Override
    public Type getType() {
        return Type.SHOPS;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getShops() {
        return shops;
    }

}

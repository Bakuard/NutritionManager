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
 * Данное огрнаничение используется при фильтрации продуктов по производителям. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getManufacturer()} ()} соответсвует любому из указаных в ограничении
 * производителей.
 */
public class ManufacturerConstraint implements Constraint {

    /**
     * Создает и возвращает ограничение ManufacturerConstraint для указанного списка производителей.
     * @param manufacturers список производиетелей для котороо создается указанное ограничение.
     * @return ограничение ManufacturerConstraint для указанного списка производителей.
     * @throws MissingValueException если указанный список производителей или любой из производителей указанный в нем имеют
     *                               значение null.
     * @throws BlankValueException если хотябы один из производителей в указанном списке не содержит ни одного отображаемого
     *                             символа.
     * @throws NotEnoughItemsException если указанный список пуст.
     */
    public static ManufacturerConstraint of(List<String> manufacturers) {
        return new ManufacturerConstraint(manufacturers);
    }

    /**
     * Создает и возвращает ограничение ManufacturerConstraint для указанных производителей.
     * @param manufacturer обязательный производитель укчаствующий в создаваемом ограничении.
     * @param manufacturers не обязательные производители участвующие в создаваемо ограничении.
     * @return ограничение ManufacturerConstraint для указанных производителей.
     * @throws MissingValueException если любой из производителей или массив manufacturers имеют значение null.
     * @throws BlankValueException если хотябы один из производителей не содержит ни одного отображаемого символа.
     */
    public static ManufacturerConstraint of(String manufacturer, String... manufacturers) {
        MissingValueException.check(manufacturers, ManufacturerConstraint.class, "manufacturers");
        return new ManufacturerConstraint(manufacturer, manufacturers);
    }


    private final ImmutableList<String> manufacturers;

    private ManufacturerConstraint(String manufacturer, String... manufacturers) {
        MissingValueException.check(manufacturers, getClass(), "manufacturers");
        ArrayList<String> allManufacturers = new ArrayList<>(Arrays.asList(manufacturers));
        allManufacturers.add(manufacturer);
        allManufacturers.forEach(m -> {
            MissingValueException.check(m, getClass(), "manufacturer");
            if(m.isBlank())
                throw new BlankValueException("ManufacturerConstraint manufacturer can't be blank", getClass(), "manufacturer");
        });

        this.manufacturers = ImmutableList.copyOf(allManufacturers);
    }

    private ManufacturerConstraint(List<String> manufacturers) {
        MissingValueException.check(manufacturers, getClass(), "manufacturers");
        manufacturers.forEach(manufacturer -> {
            MissingValueException.check(manufacturer, getClass(), "manufacturer");
            if(manufacturer.isBlank())
                throw new BlankValueException("ManufacturerConstraint manufacturer can't be blank", getClass(), "manufacturer");
        });
        if(manufacturers.isEmpty()) {
            throw new NotEnoughItemsException("manufacturer list must contain at least one value", getClass(), "manufacturer");
        }

        this.manufacturers = ImmutableList.copyOf(manufacturers);
    }

    @Override
    public Type getType() {
        return Type.MANUFACTURER;
    }

    @Override
    public ImmutableList<Constraint> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getManufacturers() {
        return manufacturers;
    }

}

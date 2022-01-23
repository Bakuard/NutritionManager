package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.ProductContext;
import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.Constraint;
import com.bakuard.nutritionManager.model.exceptions.ServiceException;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Данное огрнаничение используется при фильтрации продуктов по производителям. Продукт удовлетворяет ограничению,
 * если его параметр {@link ProductContext#getManufacturer()} ()} соответсвует любому из указаных в ограничении
 * производителей.
 */
public class ManufacturerFilter implements Filter {

    /**
     * Создает и возвращает ограничение ManufacturerConstraint для указанного списка производителей.
     * @param manufacturers список производиетелей для котороо создается указанное ограничение.
     * @return ограничение ManufacturerConstraint для указанного списка производителей.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *            1. если указанный список производителей или любой из производителей указанный в нем имеют
     *               значение null.<br/>
     *            2. если хотябы один из производителей в указанном списке не содержит ни одного отображаемого
     *               символа.<br/>
     *            3. если указанный список пуст.
     */
    public static ManufacturerFilter of(List<String> manufacturers) {
        return new ManufacturerFilter(manufacturers);
    }

    /**
     * Создает и возвращает ограничение ManufacturerConstraint для указанных производителей.
     * @param manufacturer обязательный производитель укчаствующий в создаваемом ограничении.
     * @param manufacturers не обязательные производители участвующие в создаваемо ограничении.
     * @return ограничение ManufacturerConstraint для указанных производителей.
     * @throws ServiceException если выполняется одно из следующих условий:<br/>
     *          1. если любой из производителей или массив manufacturers имеют значение null.<br/>
     *          2. если хотябы один из производителей не содержит ни одного отображаемого символа.
     */
    public static ManufacturerFilter of(String manufacturer, String... manufacturers) {
        ArrayList<String> list = new ArrayList<>();
        list.add(manufacturer);
        if(manufacturers == null) list.add(null);
        else list.addAll(Arrays.asList(manufacturers));

        return new ManufacturerFilter(list);
    }


    private final ImmutableList<String> manufacturers;

    private ManufacturerFilter(List<String> manufacturers) {
        Checker.of(getClass(), "manufacturers").
                nullValue("manufacturers", manufacturers).
                containsNull("manufacturers", manufacturers).
                notEnoughItems("manufacturers", manufacturers, 1).
                containsBlankValue("manufacturers", manufacturers).
                checkWithServiceException();

        this.manufacturers = ImmutableList.copyOf(manufacturers);
    }

    @Override
    public Type getType() {
        return Type.MANUFACTURER;
    }

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    public ImmutableList<String> getManufacturers() {
        return manufacturers;
    }


}

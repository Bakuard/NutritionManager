package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Dish;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DishProductsDataSource implements JRDataSource {

    private List<Dish.IngredientProduct> ingredientProducts;
    private Iterator<Dish.ProductGroup> productsIterator;
    private Dish.ProductGroup current;
    private BigDecimal servingNumber;
    private Dish dish;
    private Clock clock;

    public DishProductsDataSource(Dish dish,
                                  BigDecimal servingNumber,
                                  List<Dish.ProductConstraint> constraints,
                                  Clock clock) {
        ingredientProducts = dish.getProductForEachIngredient(constraints);
        productsIterator = dish.groupByProduct(ingredientProducts).stream().
                sorted(Comparator.comparing(item -> item.product().getContext().getShop())).
                toList().
                iterator();

        this.dish = dish;
        this.servingNumber = servingNumber;
        this.clock = clock;
    }

    @Override
    public boolean next() throws JRException {
        boolean hasNext = productsIterator.hasNext();
        if(hasNext) current = productsIterator.next();
        return hasNext;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        String result = "";

        switch(jrField.getName()) {
            case "dishName" -> result = dish.getName();
            case "servingNumber" -> result = format(servingNumber);
            case "creationReportData" -> result = LocalDate.now(clock).toString();
            case "totalPrice" -> result = format(
                    dish.getLackProductPrice(ingredientProducts, servingNumber).orElseThrow()
            );
            case "shopGroup" -> result = current.product().getContext().getShop();
            case "productGroup", "productName" -> result = current.product().getContext().getCategory();
            case "grade" -> result = current.product().getContext().getGrade();
            case "price" -> result = format(current.product().getContext().getPrice());
            case "packingSize" -> result = format(current.product().getContext().getPackingSize());
            case "unit" -> result = current.product().getContext().getUnit();
            case "manufacturer" -> result = current.product().getContext().getManufacturer();
            case "quantity" -> result = format(current.product().getQuantity());
            case "necessaryQuantity" -> result = format(
                    dish.getNecessaryQuantity(current, servingNumber)
            );
            case "lackQuantity" -> result = format(
                    dish.getLackPackageQuantity(current, servingNumber)
            );
            case "lackQuantityPrice" -> result = format(
                    dish.getLackPackageQuantityPrice(current, servingNumber)
            );
        }

        return result;
    }


    private String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

}

package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Product;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class DishProductsDataSource implements JRDataSource {

    private List<Dish.IngredientProduct> ingredientProducts;
    private Iterator<Map.Entry<Product, List<Dish.IngredientProduct>>> productsIterator;
    private Map.Entry<Product, List<Dish.IngredientProduct>> current;
    private BigDecimal servingNumber;
    private Dish dish;

    public DishProductsDataSource(Dish dish,
                                  BigDecimal servingNumber,
                                  List<Dish.ProductConstraint> constraints) {
        ingredientProducts = dish.getProductForEachIngredient(constraints);
        productsIterator = dish.groupByProduct(ingredientProducts).entrySet().stream().
                sorted(Comparator.comparing(pair -> pair.getKey().getContext().getShop())).
                toList().
                iterator();

        this.dish = dish;
        this.servingNumber = servingNumber;
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
            case "creationReportData" -> result = LocalDate.now().toString();
            case "totalPrice" -> result = dish.getLackProductPrice(ingredientProducts, servingNumber).
                    orElseThrow().
                    toPlainString();
            case "shopGroup" -> result = current.getKey().getContext().getShop();
            case "productGroup", "productName" -> result = current.getKey().getContext().getCategory();
            case "grade" -> result = current.getKey().getContext().getGrade();
            case "price" -> result = current.getKey().getContext().getPrice().toPlainString();
            case "packingSize" -> result = current.getKey().getContext().getPackingSize().toPlainString();
            case "unit" -> result = current.getKey().getContext().getUnit();
            case "manufacturer" -> result = current.getKey().getContext().getManufacturer();
            case "quantity" -> result = current.getKey().getQuantity().toPlainString();
            case "necessaryQuantity" -> result = dish.getNecessaryQuantity(current.getValue(), servingNumber).
                    orElseThrow().
                    toPlainString();
            case "lackQuantity" -> result = dish.getLackPackageQuantity(current.getValue(), servingNumber).
                    orElseThrow().
                    toPlainString();
            case "lackQuantityPrice" -> result = dish.getLackPackageQuantityPrice(current.getValue(), servingNumber).
                    orElseThrow().
                    toPlainString();
        }

        return result;
    }

}

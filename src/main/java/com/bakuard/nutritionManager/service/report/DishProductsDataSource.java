package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DishProductsDataSource implements JRDataSource {

    private Map<Product, List<Dish.IngredientProduct>> products;
    private BigDecimal servingNumber;

    public DishProductsDataSource(Dish dish,
                                  BigDecimal servingNumber,
                                  List<Dish.ProductConstraint> constraints) {
        List<Dish.IngredientProduct> ingredientProducts = dish.getProductForEachIngredient(constraints);
        products = dish.groupByProduct(ingredientProducts);
        this.servingNumber = servingNumber;
    }

    @Override
    public boolean next() throws JRException {
        return false;
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        return null;
    }

}

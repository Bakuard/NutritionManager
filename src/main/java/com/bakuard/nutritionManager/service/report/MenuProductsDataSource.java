package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Product;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MenuProductsDataSource implements JRDataSource {

    private Map<Product, List<Menu.MenuItemProduct>> products;
    private BigDecimal servingNumber;

    public MenuProductsDataSource(Menu menu,
                                  BigDecimal servingNumber,
                                  List<Menu.ProductConstraint> constraints) {
        List<Menu.MenuItemProduct> ingredientProducts = menu.getMenuItemProducts(constraints);
        products = menu.groupByProduct(ingredientProducts);
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

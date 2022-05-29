package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Product;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MenuProductsDataSource implements JRDataSource {

    private List<Menu.MenuItemProduct> itemProducts;
    private Iterator<Map.Entry<Product, List<Menu.MenuItemProduct>>> productsIterator;
    private Map.Entry<Product, List<Menu.MenuItemProduct>> current;
    private BigDecimal menuNumber;
    private Menu menu;

    public MenuProductsDataSource(Menu menu,
                                  BigDecimal menuNumber,
                                  List<Menu.ProductConstraint> constraints) {
        itemProducts = menu.getMenuItemProducts(constraints);
        productsIterator = menu.groupByProduct(itemProducts).entrySet().stream().
                sorted(Comparator.comparing(pair -> pair.getKey().getContext().getShop())).
                toList().
                iterator();

        this.menu = menu;
        this.menuNumber = menuNumber;
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
            case "totalPrice" -> result = format(
                    menu.getLackProductsPrice(itemProducts, menuNumber).orElseThrow()
            );
            case "shopGroup" -> result = current.getKey().getContext().getShop();
            case "productGroup", "productName" -> result = current.getKey().getContext().getCategory();
            case "grade" -> result = current.getKey().getContext().getGrade();
            case "price" -> result = format(current.getKey().getContext().getPrice());
            case "packingSize" -> result = format(current.getKey().getContext().getPackingSize());
            case "unit" -> result = current.getKey().getContext().getUnit();
            case "manufacturer" -> result = current.getKey().getContext().getManufacturer();
            case "quantity" -> result = format(current.getKey().getQuantity());
            case "necessaryQuantity" -> result = format(
                    menu.getNecessaryQuantity(current.getValue(), menuNumber).orElseThrow()
            );
            case "lackQuantity" -> result = format(
                    menu.getLackPackageQuantity(current.getValue(), menuNumber).orElseThrow()
            );
            case "lackQuantityPrice" -> result = format(
                    menu.getLackPackageQuantityPrice(current.getValue(), menuNumber).orElseThrow()
            );
            case "useInDishes" -> result = menu.getMenuItems(current.getValue()).stream().
                    map(item -> item.getDish().getName()).
                    reduce((a, b) -> String.join(", ", a, b)).
                    orElseThrow();
        }

        return result;
    }


    private String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

}

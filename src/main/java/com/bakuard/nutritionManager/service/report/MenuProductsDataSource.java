package com.bakuard.nutritionManager.service.report;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Product;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MenuProductsDataSource implements JRDataSource {

    private List<Menu.MenuItemProduct> itemProducts;
    private Iterator<Menu.ProductGroup> productsIterator;
    private Menu.ProductGroup current;
    private BigDecimal menuNumber;
    private Menu menu;
    private Clock clock;

    public MenuProductsDataSource(Menu menu,
                                  BigDecimal menuNumber,
                                  List<Menu.ProductConstraint> constraints,
                                  Clock clock) {
        itemProducts = menu.getMenuItemProducts(constraints);
        productsIterator = menu.groupByProduct(itemProducts).stream().
                sorted(Comparator.comparing(item -> item.product().getContext().getShop())).
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
            case "menuName" -> result = menu.getName();
            case "menuNumber" -> result = format(menuNumber);
            case "creationReportData" -> result = LocalDate.now(clock).toString();
            case "totalPrice" -> result = format(
                    menu.getLackProductsPrice(itemProducts, menuNumber).orElseThrow()
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
                    menu.getNecessaryQuantity(current, menuNumber)
            );
            case "lackQuantity" -> result = format(
                    menu.getLackPackageQuantity(current, menuNumber)
            );
            case "lackQuantityPrice" -> result = format(
                    menu.getLackPackageQuantityPrice(current, menuNumber)
            );
            case "useInDishes" -> result = menu.getMenuItems(current).stream().
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

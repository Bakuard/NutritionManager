package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.validation.Constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

class MenuTest {

    private static AppConfigData conf;

    @BeforeAll
    public static void beforeAll() {
        try {
            conf = new AppConfigData(
                    "/config/appConfig.properties",
                    "/config/security.properties"
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints is null
             => exception
            """)
    public void getMenuItemProducts1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contains null
             => exception
            """)
    public void getMenuItemProducts2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(Arrays.asList(
                        new Menu.ProductConstraint("dish#1", 0, 0), null
                )),
                Constraint.NOT_CONTAINS_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             menu haven't any items
             => return empty list
            """)
    public void getMenuItemProducts3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", 0, 3),
                        new Menu.ProductConstraint("dish#3", 1, 2)
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             all menu dishes haven't ingredients
             => return empty list
            """)
    public void getMenuItemProducts4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(createMenuItem(createDish(user, 1, repository), new BigDecimal(5))).
                addItem(createMenuItem(createDish(user, 2, repository), BigDecimal.ONE)).
                addItem(createMenuItem(createDish(user, 3, repository), BigDecimal.TEN)).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", 0, 3),
                        new Menu.ProductConstraint("dish#3", 1, 2)
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where dishName is null
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts5() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint(null, 0, 0),
                        new Menu.ProductConstraint(null, 1, 3),
                        new Menu.ProductConstraint(null, 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where dishName is not in the menu items
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts6() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("unknown dish", 0, 0),
                        new Menu.ProductConstraint("unknown dish", 1, 3),
                        new Menu.ProductConstraint("unknown dish", 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where ingredientIndex < 0
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts7() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", -1, 0),
                        new Menu.ProductConstraint("dish#1", -1, 3),
                        new Menu.ProductConstraint("dish#1", -1, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where ingredientIndex = ingredients number
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts8() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 3, 0),
                        new Menu.ProductConstraint("dish#1", 3, 3),
                        new Menu.ProductConstraint("dish#1", 3, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where ingredientIndex > ingredients number
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts9() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 4, 0),
                        new Menu.ProductConstraint("dish#1", 4, 3),
                        new Menu.ProductConstraint("dish#1", 4, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where productIndex < 0
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts10() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 0, this::createProduct),
                filter(user, 2), createProductPage(user, 0, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, -1),
                        new Menu.ProductConstraint("dish#1", 1, -1),
                        new Menu.ProductConstraint("dish#1", 2, -1),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 0), 0, 1, 0),
                menuItemProduct(createProduct(user, 0), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where productIndex = products number
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts11() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, this::createProduct, 0,1,2,3,4,5,6,7,8,9),
                filter(user, 1), createProductPage(user, this::createProduct, 10,11,12,13,14,15,16,17,18,19),
                filter(user, 2), createProductPage(user, this::createProduct, 20,21,22,23,24,25,26,27,28,29),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 10),
                        new Menu.ProductConstraint("dish#1", 1, 10),
                        new Menu.ProductConstraint("dish#1", 2, 10),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 10), 0, 1, 0),
                menuItemProduct(createProduct(user, 20), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where productIndex > products number
             => return correct result (skip this item and use default value)
            """)
    public void getMenuItemProducts12() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, this::createProduct, 0,1,2,3,4,5,6,7,8,9),
                filter(user, 1), createProductPage(user, this::createProduct, 10,11,12,13,14,15,16,17,18,19),
                filter(user, 2), createProductPage(user, this::createProduct, 20,21,22,23,24,25,26,27,28,29),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 11),
                        new Menu.ProductConstraint("dish#1", 1, 11),
                        new Menu.ProductConstraint("dish#1", 2, 11),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 10), 0, 1, 0),
                menuItemProduct(createProduct(user, 20), 0, 2, 0),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             all menu dish ingredients haven't suitable products
             => return list where MenuItemProduct.product() return empty Optional
            """)
    public void getMenuItemProducts13() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 0),
                emptyMenuItemProduct(0, 2, 0),
                emptyMenuItemProduct(1, 0, 0),
                emptyMenuItemProduct(1, 1, 0),
                emptyMenuItemProduct(1, 2, 0),
                emptyMenuItemProduct(2, 0, 0),
                emptyMenuItemProduct(2, 1, 0),
                emptyMenuItemProduct(2, 2, 0)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             some menu dish ingredients have suitable products,
             => return correct result (MenuItemProduct.product() must return empty Optional for ingredient without products)
            """)
    public void getMenuItemProducts14() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 3, this::createProduct),
                filter(user, 2), createProductPage(user, 2, this::createProduct),
                filter(user, 3), Pageable.firstEmptyPage(),
                filter(user, 4), Pageable.firstEmptyPage(),
                filter(user, 5), Pageable.firstEmptyPage(),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 2), 0, 2, 2),
                emptyMenuItemProduct(1, 0, 0),
                emptyMenuItemProduct(1, 1, 0),
                emptyMenuItemProduct(1, 2, 0),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             all menu dish ingredients have suitable products,
             there are several ProductConstraint for some ingredients
             => return correct result
            """)
    public void getMenuItemProducts15() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 3, this::createProduct),
                filter(user, 2), createProductPage(user, 2, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, -1),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#1", 2, 15),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, -1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 1, 4),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, -1),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 0, 20),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 2), 0, 2, 2),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             menu have dishes,
             all menu dish ingredients have suitable products,
             no products selected for ingredients
             => return correct result (default value)
            """)
    public void getMenuItemProducts16() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, this::createProduct, 0, 2, 3, 4, 5),
                filter(user, 1), createProductPage(user, this::createProduct, 11, 12, 13, 14, 15),
                filter(user, 2), createProductPage(user, this::createProduct, 21, 22, 23, 24, 25),
                filter(user, 3), createProductPage(user, this::createProduct, 31, 32, 33, 34, 35),
                filter(user, 4), createProductPage(user, this::createProduct, 41, 42, 43, 44, 45),
                filter(user, 5), createProductPage(user, this::createProduct, 51, 52, 53, 54, 55),
                filter(user, 6), createProductPage(user, this::createProduct, 61, 62, 63, 64, 65),
                filter(user, 7), createProductPage(user, this::createProduct, 71, 72, 73, 74, 75),
                filter(user, 8), createProductPage(user, this::createProduct, 81, 82, 83, 84, 85)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of()
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 11), 0, 1, 0),
                menuItemProduct(createProduct(user, 21), 0, 2, 0),
                menuItemProduct(createProduct(user, 31), 1, 0, 0),
                menuItemProduct(createProduct(user, 41), 1, 1, 0),
                menuItemProduct(createProduct(user, 51), 1, 2, 0),
                menuItemProduct(createProduct(user, 61), 2, 0, 0),
                menuItemProduct(createProduct(user, 71), 2, 1, 0),
                menuItemProduct(createProduct(user, 81), 2, 2, 0)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             menu have dishes,
             all menu dish ingredients have suitable products,
             arguments is correct
             => return correct result
            """)
    public void getMenuItemProducts17() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, 0, this::createProduct),
                filter(user, 1), createProductPage(user, 3, this::createProduct),
                filter(user, 2), createProductPage(user, 2, this::createProduct),
                filter(user, 3), createProductPage(user, 1, this::createProduct),
                filter(user, 4), createProductPage(user, 3, this::createProduct),
                filter(user, 5), createProductPage(user, 4, this::createProduct),
                filter(user, 6), createProductPage(user, 10, this::createProduct),
                filter(user, 7), createProductPage(user, 15, this::createProduct),
                filter(user, 8), createProductPage(user, 11, this::createProduct)
        );
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(createProduct(user, 0), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 2), 0, 2, 2),
                menuItemProduct(createProduct(user, 1), 1, 0, 1),
                menuItemProduct(createProduct(user, 3), 1, 1, 3),
                menuItemProduct(createProduct(user, 4), 1, 2, 4),
                menuItemProduct(createProduct(user, 10), 2, 0, 10),
                menuItemProduct(createProduct(user, 15), 2, 1, 15),
                menuItemProduct(createProduct(user, 11), 2, 2, 11)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishIngredientQuantity(product, menuNumber):
             product is null
             => exception
            """)
    public void getDishIngredientQuantity1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getDishIngredientQuantity(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getDishIngredientQuantity(product, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getDishIngredientQuantity2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(createProduct(user, 0), 0, 1, 0);

        AssertUtil.assertValidateException(
                () -> menu.getDishIngredientQuantity(product, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getDishIngredientQuantity(product, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getDishIngredientQuantity3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(createProduct(user, 0), 0, 1, 0);

        AssertUtil.assertValidateException(
                () -> menu.getDishIngredientQuantity(product, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getDishIngredientQuantity(product, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getDishIngredientQuantity4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(createProduct(user, 0), 0, 1, 0);

        AssertUtil.assertValidateException(
                () -> menu.getDishIngredientQuantity(product, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getDishIngredientQuantity(product, menuNumber):
             all arguments is correct
             => return correct result
            """)
    public void getDishIngredientQuantity5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                tryBuild();
        Menu.MenuItemProduct product =
                menuItemProduct(createProduct(user, 0), 0, 1, 0);

        Optional<BigDecimal> actual = menu.getDishIngredientQuantity(product, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(100), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             products is null
             => exception
            """)
    public void getProductQuantityForDishes1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getProductQuantityForDishes2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(menuItems, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getProductQuantityForDishes3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(menuItems, new BigDecimal(-10)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getProductQuantityForDishes4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 13),
                menuItemProduct(createProduct(user, 3), 1, 2, 7),
                menuItemProduct(createProduct(user, 3), 2, 1, 2)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(menuItems, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             products is empty
             => return empty Map
            """)
    public void getProductQuantityForDishes5() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of();

        Map<MenuItem, BigDecimal> actual = menu.getProductQuantityForDishes(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             all products item contain empty Optional
             => return empty Map
            """)
    public void getProductQuantityForDishes6() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 13),
                emptyMenuItemProduct(1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        Map<MenuItem, BigDecimal> actual = menu.getProductQuantityForDishes(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             some products item contain empty Optional
             => return correct result (skip item where MenuItemProduct#product() return empty Optional)
            """)
    public void getProductQuantityForDishes7() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        Map<MenuItem, BigDecimal> actual = menu.getProductQuantityForDishes(menuItems, BigDecimal.TEN);

        Map<MenuItem, BigDecimal> expected = Map.of(
                menu.getItems().get(0), new BigDecimal(350).setScale(conf.getNumberScale(), conf.getRoundingMode()),
                menu.getItems().get(1), new BigDecimal(100).setScale(conf.getNumberScale(), conf.getRoundingMode())
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             all products item contain product
             => return correct result
            """)
    public void getProductQuantityForDishes8() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        1, 2, 7),
                menuItemProduct(createProduct(user, 3).setPrice(new BigDecimal(120)),
                        2, 1, 2)
        );

        Map<MenuItem, BigDecimal> actual = menu.getProductQuantityForDishes(menuItems, BigDecimal.TEN);

        Map<MenuItem, BigDecimal> expected = Map.of(
                menu.getItems().get(0), new BigDecimal(350).setScale(conf.getNumberScale(), conf.getRoundingMode()),
                menu.getItems().get(1), new BigDecimal(100).setScale(conf.getNumberScale(), conf.getRoundingMode()),
                menu.getItems().get(2), new BigDecimal(100).setScale(conf.getNumberScale(), conf.getRoundingMode())
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             products is null
             => exception
            """)
    public void getNecessaryQuantity1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getNecessaryQuantity2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(menuItems, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getNecessaryQuantity3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(menuItems, new BigDecimal(-10)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getNecessaryQuantity4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(menuItems, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             products is empty
             => return empty Optional
            """)
    public void getNecessaryQuantity5() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of();

        Optional<BigDecimal> actual = menu.getNecessaryQuantity(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             all products item contain empty Optional
             => return empty Optional
            """)
    public void getNecessaryQuantity6() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 13),
                emptyMenuItemProduct(1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        Optional<BigDecimal> actual = menu.getNecessaryQuantity(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             some products item contain empty Optional
             => return correct result (skip item where MenuItemProduct#product() return empty Optional)
            """)
    public void getNecessaryQuantity7() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 13),
                menuItemProduct(createProduct(user, 3), 1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        BigDecimal actual = menu.getNecessaryQuantity(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(450), actual);
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             all products item contain product
             => return correct result
            """)
    public void getNecessaryQuantity8() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 0),
                menuItemProduct(createProduct(user, 3), 0, 1, 13),
                menuItemProduct(createProduct(user, 3), 1, 2, 7),
                menuItemProduct(createProduct(user, 3), 2, 1, 2)
        );

        BigDecimal actual = menu.getNecessaryQuantity(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(550), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackPackageQuantity1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getLackPackageQuantity2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(menuItems, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getLackPackageQuantity3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(menuItems, new BigDecimal(-10)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getLackPackageQuantity4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(menuItems, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             products is empty
             => return empty Optional
            """)
    public void getLackPackageQuantity5() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of();

        Optional<BigDecimal> actual = menu.getLackPackageQuantity(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             all products item contain empty Optional
             => return empty Optional
            """)
    public void getLackPackageQuantity6() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 13),
                emptyMenuItemProduct(1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        Optional<BigDecimal> actual = menu.getLackPackageQuantity(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             some products item contain empty Optional
             => return correct result (skip item where MenuItemProduct#product() return empty Optional)
            """)
    public void getLackPackageQuantity7() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantity(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(34), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             all products item contain product
             => return correct result
            """)
    public void getLackPackageQuantity8() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                menuItemProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantity(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(42), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackPackageQuantityPrice1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getLackPackageQuantityPrice2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(menuItems, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getLackPackageQuantityPrice3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(menuItems, new BigDecimal(-10)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getLackPackageQuantityPrice4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3), 0, 0, 3),
                menuItemProduct(createProduct(user, 3), 0, 1, 3),
                menuItemProduct(createProduct(user, 3), 1, 2, 3),
                menuItemProduct(createProduct(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(menuItems, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             products is empty
             => return empty Optional
            """)
    public void getLackPackageQuantityPrice5() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of();

        Optional<BigDecimal> actual = menu.getLackPackageQuantityPrice(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             all products item contain empty Optional
             => return empty Optional
            """)
    public void getLackPackageQuantityPrice6() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 13),
                emptyMenuItemProduct(1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        Optional<BigDecimal> actual = menu.getLackPackageQuantityPrice(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             some products item contain empty Optional
             => return correct result (skip item where MenuItemProduct#product() return empty Optional)
            """)
    public void getLackPackageQuantityPrice7() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                emptyMenuItemProduct(2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantityPrice(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1904), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             all products item contain product
             => return correct result
            """)
    public void getLackPackageQuantityPrice8() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                menuItemProduct(createProduct(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantityPrice(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(2352), actual);
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackProductsPrice1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackProductsPrice(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             menuNumber is null
             => exception
            """)
    public void getLackProductsPrice2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(createProduct(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(createProduct(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(createProduct(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(createProduct(user, 11).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        2, 2, 11)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackProductsPrice(menuItems, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             menuNumber < 0
             => exception
            """)
    public void getLackProductsPrice3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(createProduct(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(createProduct(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(createProduct(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(createProduct(user, 11).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        2, 2, 11)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackProductsPrice(menuItems, new BigDecimal(-10)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             menuNumber = 0
             => exception
            """)
    public void getLackProductsPrice4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(createProduct(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(createProduct(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(createProduct(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(createProduct(user, 11).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        2, 2, 11)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackProductsPrice(menuItems, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             products is empty
             => return empty Optional
            """)
    public void getLackProductsPrice5() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of();

        Optional<BigDecimal> actual = menu.getLackProductsPrice(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             all products item contain empty Optional
             => return empty Optional
            """)
    public void getLackProductsPrice6() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                emptyMenuItemProduct(0, 0, 0),
                emptyMenuItemProduct(0, 1, 3),
                emptyMenuItemProduct(0, 2, 2),
                emptyMenuItemProduct(1, 0, 1),
                emptyMenuItemProduct(1, 1, 3),
                emptyMenuItemProduct(1, 2, 4),
                emptyMenuItemProduct(2, 0, 10),
                emptyMenuItemProduct(2, 1, 15),
                emptyMenuItemProduct(2, 2, 11)
        );

        Optional<BigDecimal> actual = menu.getLackProductsPrice(menuItems, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             some products item contain empty Optional
             => return correct result (skip item where MenuItemProduct#product() return empty Optional)
            """)
    public void getLackProductsPrice7() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(createProduct(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                emptyMenuItemProduct(1, 2, 4),

                emptyMenuItemProduct(2, 0, 10),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                emptyMenuItemProduct(2, 2, 11)
        );

        BigDecimal actual = menu.getLackProductsPrice(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1960 + 14800 + 2500), actual);
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             all products item contain product
             => return correct result
            """)
    public void getLackProductsPrice8() {
        User user = createUser(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(filter(user, 0), new BigDecimal(5)),
                                        createIngredient(filter(user, 1), new BigDecimal(2)),
                                        createIngredient(filter(user, 2), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(createProduct(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(createProduct(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(createProduct(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(createProduct(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(createProduct(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(createProduct(user, 11).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        2, 2, 11)
        );

        BigDecimal actual = menu.getLackProductsPrice(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1960 + 14800 + 2500 + 12000 + 2500 + 36000), actual);
    }


    private User createUser(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
    }

    private Product.Builder createProduct(User user, int id) {
        BigDecimal productNumber = new BigDecimal(id).abs().add(BigDecimal.ONE);
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(id)).
                setUser(user).
                setCategory("category " + id).
                setShop("shop " + id).
                setGrade("variety " + id).
                setManufacturer("manufacturer " + id).
                setUnit("unitA").
                setPrice(productNumber.multiply(BigDecimal.TEN)).
                setPackingSize(new BigDecimal("0.5").multiply(productNumber)).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description " + id).
                setImageUrl("https://nutritionmanager.xyz/products/images?id=" + id).
                addTag("tag " + id).
                addTag("common tag");
    }

    private Dish createDish(User user,
                            int id,
                            ProductRepository repository,
                            DishIngredient.Builder... ingredients) {
        Dish.Builder builder = new Dish.Builder().
                setId(toUUID(id)).
                setUser(user).
                setName("dish#" + id).
                setServingSize(BigDecimal.ONE).
                setUnit("unit A").
                setDescription("description#" + id).
                setImageUrl("https://nutritionmanager.xyz/dishes/images?id=" + id).
                setConfig(conf).
                setRepository(repository).
                addTag("tag A").
                addTag("common tag");

        for(int i = 0; i < ingredients.length; i++) {
            builder.addIngredient(ingredients[i].setName("ingredient#" + i));
        }

        return builder.tryBuild();
    }

    private DishIngredient.Builder createIngredient(Filter filter, BigDecimal quantity) {
        return new DishIngredient.Builder().
                setName("ingredient").
                setFilter(filter).
                setQuantity(quantity).
                setConfig(conf);
    }

    private Menu.Builder createMenu(int id, User user) {
        return new Menu.Builder().
                setId(toUUID(id)).
                setUser(user).
                setName("Menu#" + id).
                setDescription("Description for menu#" + id).
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=" + id).
                setConfig(conf).
                addTag("common tag").
                addTag("tag#" + id);
    }

    private MenuItem.Builder createMenuItem(Dish dish, BigDecimal quantity) {
        return new MenuItem.Builder().
                setDish(dish).
                setQuantity(quantity).
                setConfig(conf);
    }

    public Menu.MenuItemProduct menuItemProduct(Product.Builder product,
                                                int itemIndex,
                                                int ingredientIndex,
                                                int productIndex) {
        return new Menu.MenuItemProduct(
                Optional.of(product.tryBuild()),
                itemIndex,
                ingredientIndex,
                productIndex
        );
    }

    public Menu.MenuItemProduct emptyMenuItemProduct(int itemIndex, 
                                                     int ingredientIndex, 
                                                     int productIndex) {
        return new Menu.MenuItemProduct(
                Optional.empty(),
                itemIndex,
                ingredientIndex,
                productIndex
        );
    }

    private ProductRepository mockProductRepository(Filter filter00, Page<Product> page00,
                                                    Filter filter01, Page<Product> page01,
                                                    Filter filter02, Page<Product> page02,
                                                    Filter filter10, Page<Product> page10,
                                                    Filter filter11, Page<Product> page11,
                                                    Filter filter12, Page<Product> page12,
                                                    Filter filter20, Page<Product> page20,
                                                    Filter filter21, Page<Product> page21,
                                                    Filter filter22, Page<Product> page22) {
        ProductRepository repository = Mockito.mock(ProductRepository.class);

        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter00)))).
                thenReturn(page00.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter01)))).
                thenReturn(page01.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter02)))).
                thenReturn(page02.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter10)))).
                thenReturn(page10.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter11)))).
                thenReturn(page11.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter12)))).
                thenReturn(page12.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter20)))).
                thenReturn(page20.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter21)))).
                thenReturn(page21.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(createCriteriaNumber(filter22)))).
                thenReturn(page22.getMetadata().getTotalItems().intValueExact());

        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter00)))).
                thenReturn(page00);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter01)))).
                thenReturn(page01);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter02)))).
                thenReturn(page02);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter10)))).
                thenReturn(page10);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter11)))).
                thenReturn(page11);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter12)))).
                thenReturn(page12);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter20)))).
                thenReturn(page20);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter21)))).
                thenReturn(page21);
        Mockito.when(repository.getProducts(Mockito.eq(createCriteria(filter22)))).
                thenReturn(page22);

        return repository;
    }

    private Filter filter(User user, int num) {
        return Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("tag" + num), new Tag("common tag")),
                Filter.anyCategory("category " + num),
                Filter.anyGrade("grade " + num)
        );
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private Criteria createCriteria(Filter filter) {
        return new Criteria().
                setPageable(Pageable.of(30, 0)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria createCriteriaNumber(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Page<Product> createProductPage(User user,
                                            int productIndex,
                                            BiFunction<User, Integer, Product.Builder> productFactory) {
        Page.Metadata metadata = Pageable.ofIndex(30, productIndex).
                createPageMetadata(1000, 30);

        int offset = metadata.getOffset().intValue();
        List<Product> products = IntStream.range(0, metadata.getActualSize()).
                mapToObj(i -> productFactory.apply(user, offset + i).tryBuild()).
                toList();

        return metadata.createPage(products);
    }

    private Page<Product> createProductPage(User user,
                                            BiFunction<User, Integer, Product.Builder> productFactory,
                                            int... productIds) {
        Page.Metadata metadata = Pageable.of(30 , 0).
                createPageMetadata(productIds.length, 30);

        List<Product> products = Arrays.stream(productIds).
                mapToObj(i -> productFactory.apply(user, i).tryBuild()).
                toList();

        return metadata.createPage(products);
    }

}
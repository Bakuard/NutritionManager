package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.SimpleTestConfig;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

@SpringBootTest(classes = SimpleTestConfig.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@TestPropertySource(locations = "classpath:application.properties")
class MenuTest {

    @Autowired
    private AppConfigData conf;

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints is null
             => exception
            """)
    public void getMenuItemProducts1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(menuItem(dish(user, 1, repository), new BigDecimal(5), 0)).
                addItem(menuItem(dish(user, 2, repository), BigDecimal.ONE, 1)).
                addItem(menuItem(dish(user, 3, repository), BigDecimal.TEN, 2)).
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 0, this::product),
                filter(user, 2), productPage(user, 0, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 0), 0, 1, 0),
                menuItemProduct(product(user, 0), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user, 0),
                        product(user, 1),
                        product(user, 2),
                        product(user, 3),
                        product(user, 4)),
                filter(user, 1), productPage(
                        product(user, 11),
                        product(user, 12),
                        product(user, 13),
                        product(user, 14),
                        product(user, 15)),
                filter(user, 2), productPage(
                        product(user, 21),
                        product(user, 22),
                        product(user, 23),
                        product(user, 24),
                        product(user, 25)),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 5),
                        new Menu.ProductConstraint("dish#1", 1, 5),
                        new Menu.ProductConstraint("dish#1", 2, 5),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 11), 0, 1, 0),
                menuItemProduct(product(user, 21), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user, 0),
                        product(user, 1),
                        product(user, 2),
                        product(user, 3),
                        product(user, 4)),
                filter(user, 1), productPage(
                        product(user, 11),
                        product(user, 12),
                        product(user, 13),
                        product(user, 14),
                        product(user, 15)),
                filter(user, 2), productPage(
                        product(user, 21),
                        product(user, 22),
                        product(user, 23),
                        product(user, 24),
                        product(user, 25)),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 6),
                        new Menu.ProductConstraint("dish#1", 1, 6),
                        new Menu.ProductConstraint("dish#1", 2, 6),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 11), 0, 1, 0),
                menuItemProduct(product(user, 21), 0, 2, 0),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Page.empty());
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 3, this::product),
                filter(user, 2), productPage(user, 2, this::product),
                filter(user, 3), Page.empty(),
                filter(user, 4), Page.empty(),
                filter(user, 5), Page.empty(),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 2), 0, 2, 2),
                emptyMenuItemProduct(1, 0, 0),
                emptyMenuItemProduct(1, 1, 0),
                emptyMenuItemProduct(1, 2, 0),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 3, this::product),
                filter(user, 2), productPage(user, 2, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 2), 0, 2, 2),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(product(user,0), product(user,1)),
                filter(user, 1), productPage(product(user,11), product(user,12)),
                filter(user, 2), productPage(product(user,21), product(user,22)),
                filter(user, 3), productPage(product(user,31), product(user,32)),
                filter(user, 4), productPage(product(user,41), product(user,42)),
                filter(user, 5), productPage(product(user,51), product(user,52)),
                filter(user, 6), productPage(product(user,61), product(user,62)),
                filter(user, 7), productPage(product(user,71), product(user,72)),
                filter(user, 8), productPage(product(user,81), product(user,82)),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                List.of()
        );

        List<Menu.MenuItemProduct> expected = List.of(
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 11), 0, 1, 0),
                menuItemProduct(product(user, 21), 0, 2, 0),
                menuItemProduct(product(user, 31), 1, 0, 0),
                menuItemProduct(product(user, 41), 1, 1, 0),
                menuItemProduct(product(user, 51), 1, 2, 0),
                menuItemProduct(product(user, 61), 2, 0, 0),
                menuItemProduct(product(user, 71), 2, 1, 0),
                menuItemProduct(product(user, 81), 2, 2, 0)
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
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(user, 0, this::product),
                filter(user, 1), productPage(user, 3, this::product),
                filter(user, 2), productPage(user, 2, this::product),
                filter(user, 3), productPage(user, 1, this::product),
                filter(user, 4), productPage(user, 3, this::product),
                filter(user, 5), productPage(user, 4, this::product),
                filter(user, 6), productPage(user, 10, this::product),
                filter(user, 7), productPage(user, 15, this::product),
                filter(user, 8), productPage(user, 11, this::product),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
                menuItemProduct(product(user, 0), 0, 0, 0),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 2), 0, 2, 2),
                menuItemProduct(product(user, 1), 1, 0, 1),
                menuItemProduct(product(user, 3), 1, 1, 3),
                menuItemProduct(product(user, 4), 1, 2, 4),
                menuItemProduct(product(user, 10), 2, 0, 10),
                menuItemProduct(product(user, 15), 2, 1, 15),
                menuItemProduct(product(user, 11), 2, 2, 11)
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(product(user, 0), 0, 1, 0);

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
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(product(user, 0), 0, 1, 0);

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
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                tryBuild();

        Menu.MenuItemProduct product =
                menuItemProduct(product(user, 0), 0, 1, 0);

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
        User user = user(1);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                tryBuild();
        Menu.MenuItemProduct product =
                menuItemProduct(product(user, 0), 0, 1, 0);

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(productGroup, null),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(productGroup, new BigDecimal(-10)),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getProductQuantityForDishes(productGroup, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getProductQuantityForDishes(products, menuNumber):
             all arguments is correct
             => return correct result
            """)
    public void getProductQuantityForDishes5() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3).setPrice(new BigDecimal(120)),
                        0, 0, 0),
                menuItemProduct(product(user, 3).setPrice(new BigDecimal(120)),
                        0, 1, 13),
                menuItemProduct(product(user, 3).setPrice(new BigDecimal(120)),
                        1, 2, 7),
                menuItemProduct(product(user, 3).setPrice(new BigDecimal(120)),
                        2, 1, 2)
        );

        Map<MenuItem, BigDecimal> actual = menu.getProductQuantityForDishes(productGroup, BigDecimal.TEN);

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(productGroup, null),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(productGroup, new BigDecimal(-10)),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(productGroup, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(products, menuNumber):
             all arguments is correct
             => return correct result
            """)
    public void getNecessaryQuantity5() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 0),
                menuItemProduct(product(user, 3), 0, 1, 13),
                menuItemProduct(product(user, 3), 1, 2, 7),
                menuItemProduct(product(user, 3), 2, 1, 2)
        );

        BigDecimal actual = menu.getNecessaryQuantity(productGroup, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(550), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackPackageQuantity1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(productGroup, null),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(productGroup, new BigDecimal(-10)),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantity(productGroup, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(products, menuNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantity5() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3).
                        setPackingSize(new BigDecimal(13)).
                        setQuantity(new BigDecimal(16)),
                menuItemProduct(product(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                menuItemProduct(product(user, 3).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantity(productGroup, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(42), actual);
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackPackageQuantityPrice1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(productGroup, null),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(productGroup, new BigDecimal(-10)),
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3),
                menuItemProduct(product(user, 3), 0, 0, 3),
                menuItemProduct(product(user, 3), 0, 1, 3),
                menuItemProduct(product(user, 3), 1, 2, 3),
                menuItemProduct(product(user, 3), 2, 1, 3)
        );

        AssertUtil.assertValidateException(
                () -> menu.getLackPackageQuantityPrice(productGroup, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(products, menuNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantityPrice5() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        Menu.ProductGroup productGroup = productGroup(
                product(user, 3).
                        setPrice(new BigDecimal(56)).
                        setPackingSize(new BigDecimal(13)).
                        setQuantity(new BigDecimal(16)),
                menuItemProduct(product(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 2, 7),
                menuItemProduct(product(user, 3).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 2)
        );

        BigDecimal actual = menu.getLackPackageQuantityPrice(productGroup, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(2352), actual);
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products, menuNumber):
             products is null
             => exception
            """)
    public void getLackProductsPrice1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(product(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(product(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(product(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(product(user, 11).
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(product(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(product(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(product(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(product(user, 11).
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
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(product(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(product(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(product(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(product(user, 11).
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
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
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
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(product(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                emptyMenuItemProduct(1, 2, 4),

                emptyMenuItemProduct(2, 0, 10),
                menuItemProduct(product(user, 100).
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
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();
        List<Menu.MenuItemProduct> menuItems = List.of(
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 0, 0),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        0, 1, 13),
                menuItemProduct(product(user, 2).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(5)),
                        0, 2, 2),

                menuItemProduct(product(user, 1).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        1, 0, 1),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        1, 1, 3),
                menuItemProduct(product(user, 4).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        1, 2, 4),

                menuItemProduct(product(user, 10).
                                setPrice(new BigDecimal(25)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(BigDecimal.ZERO),
                        2, 0, 10),
                menuItemProduct(product(user, 100).
                                setPrice(new BigDecimal(56)).
                                setPackingSize(new BigDecimal(13)).
                                setQuantity(new BigDecimal(16)),
                        2, 1, 15),
                menuItemProduct(product(user, 11).
                                setPrice(new BigDecimal(60)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(BigDecimal.ZERO),
                        2, 2, 11)
        );

        BigDecimal actual = menu.getLackProductsPrice(menuItems, BigDecimal.TEN).orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1960 + 14800 + 2500 + 12000 + 2500 + 36000), actual);
    }

    @Test
    @DisplayName("""
            getMinPrice():
             menu haven't any dishes
             => return empty Optional
            """)
    public void getMinPrice1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getMinPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all menu dishes haven't ingredients
             => return empty Optional
            """)
    public void getMinPrice2() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(dish(user, 1, repository), new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(dish(user, 2, repository), BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(dish(user, 3, repository), BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getMinPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all ingredients of all dishes haven't suitable products
             => return empty Optional
            """)
    public void getMinPrice3() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), Page.empty(),
                filter(user, 1), Page.empty(),
                filter(user, 2), Page.empty(),
                filter(user, 3), Page.empty(),
                filter(user, 4), Page.empty(),
                filter(user, 5), Page.empty(),
                filter(user, 6), Page.empty(),
                filter(user, 7), Page.empty(),
                filter(user, 8), Page.empty(),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getMinPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             some dishes ingredients haven't suitable products
             => return correct result (skip ingredients without suitable products)
            """)
    public void getMinPrice4() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user,0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), Page.empty(),

                filter(user, 3), Page.empty(),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), Page.empty(),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), Page.empty(),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getMinPrice().orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1200 + 3000), actual);
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dishes ingredients have suitable products
             => return correct result
            """)
    public void getMinPrice5() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), productPage(
                        product(user, 10).setPackingSize(new BigDecimal("1.5")).setPrice(new BigDecimal(43)),
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90))
                ),

                filter(user, 3), productPage(
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90)),
                        product(user, 30).setPackingSize(BigDecimal.TEN).setPrice(new BigDecimal(250))
                ),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), productPage(
                        product(user, 31).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(22)),
                        product(user, 41).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(30))
                ),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), productPage(
                        product(user, 101).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22)),
                        product(user, 111).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22))
                ),
                0
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getMinPrice().orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1200 + 3000 + 860 + 180 + 440 + 66), actual);
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             menu haven't any dishes
             => return empty Optional
            """)
    public void getMaxPrice1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getMaxPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all menu dishes haven't ingredients
             => return empty Optional
            """)
    public void getMaxPrice2() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(dish(user, 1, repository), new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(dish(user, 2, repository), BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(dish(user, 3, repository), BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getMaxPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all ingredients of all dishes haven't suitable products
             => return empty Optional
            """)
    public void getMaxPrice3() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), Page.empty(),
                filter(user, 1), Page.empty(),
                filter(user, 2), Page.empty(),
                filter(user, 3), Page.empty(),
                filter(user, 4), Page.empty(),
                filter(user, 5), Page.empty(),
                filter(user, 6), Page.empty(),
                filter(user, 7), Page.empty(),
                filter(user, 8), Page.empty(),
                10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getMaxPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             some dishes ingredients haven't suitable products
             => return correct result (skip ingredients without suitable products)
            """)
    public void getMaxPrice4() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user,0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), Page.empty(),

                filter(user, 3), Page.empty(),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), Page.empty(),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), Page.empty(),
                10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getMaxPrice().orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1500 + 6000), actual);
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dishes ingredients have suitable products
             => return correct result
            """)
    public void getMaxPrice5() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), productPage(
                        product(user, 10).setPackingSize(new BigDecimal("1.5")).setPrice(new BigDecimal(43)),
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90))
                ),

                filter(user, 3), productPage(
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90)),
                        product(user, 30).setPackingSize(BigDecimal.TEN).setPrice(new BigDecimal(250))
                ),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), productPage(
                        product(user, 31).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(22)),
                        product(user, 41).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(30))
                ),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), productPage(
                        product(user, 101).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22)),
                        product(user, 111).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22))
                ),
                10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getMaxPrice().orElseThrow();

        AssertUtil.assertEquals(new BigDecimal(1500 + 540 + 250 + 6000 + 600 + 66), actual);
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             menu haven't any dishes
             => return empty Optional
            """)
    public void getAveragePrice1() {
        User user = user(1);
        Menu menu = menu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getAveragePrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all menu dishes haven't ingredients
             => return empty Optional
            """)
    public void getAveragePrice2() {
        User user = user(1);
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Menu menu = menu(1, user).
                addItem(
                        menuItem(dish(user, 1, repository), new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(dish(user, 2, repository), BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(dish(user, 3, repository), BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getAveragePrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all ingredients of all dishes haven't suitable products
             => return empty Optional
            """)
    public void getAveragePrice3() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), Page.empty(),
                filter(user, 1), Page.empty(),
                filter(user, 2), Page.empty(),
                filter(user, 3), Page.empty(),
                filter(user, 4), Page.empty(),
                filter(user, 5), Page.empty(),
                filter(user, 6), Page.empty(),
                filter(user, 7), Page.empty(),
                filter(user, 8), Page.empty(),
                0, 10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        Optional<BigDecimal> actual = menu.getAveragePrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             some dishes ingredients haven't suitable products
             => return correct result (skip ingredients without suitable products)
            """)
    public void getAveragePrice4() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user,0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), Page.empty(),

                filter(user, 3), Page.empty(),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), Page.empty(),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), Page.empty(),
                0, 10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getAveragePrice().orElseThrow();

        BigDecimal expected = new BigDecimal(1200 + 3000).
                add(new BigDecimal(1500 + 6000)).
                divide(new BigDecimal(2), conf.getMathContext());
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dishes ingredients have suitable products
             => return correct result
            """)
    public void getAveragePrice5() {
        User user = user(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 1), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 2), productPage(
                        product(user, 10).setPackingSize(new BigDecimal("1.5")).setPrice(new BigDecimal(43)),
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90))
                ),

                filter(user, 3), productPage(
                        product(user, 20).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(90)),
                        product(user, 30).setPackingSize(BigDecimal.TEN).setPrice(new BigDecimal(250))
                ),
                filter(user, 4), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 5), productPage(
                        product(user, 51).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(150)),
                        product(user, 61).setPackingSize(new BigDecimal("0.25")).setPrice(new BigDecimal(150))
                ),

                filter(user, 6), productPage(
                        product(user, 31).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(22)),
                        product(user, 41).setPackingSize(new BigDecimal("0.5")).setPrice(new BigDecimal(30))
                ),
                filter(user, 7), productPage(
                        product(user, 0).setPackingSize(new BigDecimal(2)).setPrice(new BigDecimal(50)),
                        product(user, 10).setPackingSize(new BigDecimal(5)).setPrice(new BigDecimal(150))
                ),
                filter(user, 8), productPage(
                        product(user, 101).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22)),
                        product(user, 111).setPackingSize(new BigDecimal(10)).setPrice(new BigDecimal(22))
                ),
                0, 10000
        );
        Menu menu = menu(1, user).
                addItem(
                        menuItem(
                                dish(user, 1, repository,
                                        ingredient(filter(user, 0), new BigDecimal(5), 0),
                                        ingredient(filter(user, 1), new BigDecimal(2), 1),
                                        ingredient(filter(user, 2), new BigDecimal(6), 2)),
                                new BigDecimal(5), 0)
                ).
                addItem(
                        menuItem(
                                dish(user, 2, repository,
                                        ingredient(filter(user, 3), BigDecimal.TEN, 0),
                                        ingredient(filter(user, 4), new BigDecimal(2), 1),
                                        ingredient(filter(user, 5), BigDecimal.TEN, 2)),
                                BigDecimal.ONE, 1)
                ).
                addItem(
                        menuItem(
                                dish(user, 3, repository,
                                        ingredient(filter(user, 6), BigDecimal.ONE, 0),
                                        ingredient(filter(user, 7), BigDecimal.ONE, 1),
                                        ingredient(filter(user, 8), new BigDecimal(3), 2)),
                                BigDecimal.TEN, 2)
                ).
                tryBuild();

        BigDecimal actual = menu.getAveragePrice().orElseThrow();

        BigDecimal expected = new BigDecimal(1200 + 3000 + 860 + 180 + 440 + 66).
                add(new BigDecimal(1500 + 540 + 250 + 6000 + 600 + 66)).
                divide(new BigDecimal(2), conf.getMathContext());
        AssertUtil.assertEquals(expected, actual);
    }


    private User user(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
    }

    private Product.Builder product(User user, int id) {
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

    private Dish dish(User user,
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

        Arrays.stream(ingredients).forEach(builder::addIngredient);

        return builder.tryBuild();
    }

    private DishIngredient.Builder ingredient(Filter filter, BigDecimal quantity, int ingredientIndex) {
        return new DishIngredient.Builder().
                setId(toUUID(ingredientIndex)).
                setName("ingredient#" + ingredientIndex).
                setFilter(filter).
                setQuantity(quantity).
                setConfig(conf);
    }

    private Menu.Builder menu(int id, User user) {
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

    private MenuItem.LoadBuilder menuItem(Dish dish, BigDecimal quantity, int itemId) {
        return new MenuItem.LoadBuilder().
                setId(toUUID(itemId)).
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

    public Menu.ProductGroup productGroup(Product.Builder product,
                                          Menu.MenuItemProduct... items) {
        return new Menu.ProductGroup(product.tryBuild(), List.of(items));
    }

    private ProductRepository mockProductRepository(Filter filter00, Page<Product> page00,
                                                    Filter filter01, Page<Product> page01,
                                                    Filter filter02, Page<Product> page02,
                                                    Filter filter10, Page<Product> page10,
                                                    Filter filter11, Page<Product> page11,
                                                    Filter filter12, Page<Product> page12,
                                                    Filter filter20, Page<Product> page20,
                                                    Filter filter21, Page<Product> page21,
                                                    Filter filter22, Page<Product> page22,
                                                    int... pageNumbers) {
        ProductRepository repository = Mockito.mock(ProductRepository.class);

        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter00)))).
                thenReturn(page00.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter01)))).
                thenReturn(page01.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter02)))).
                thenReturn(page02.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter10)))).
                thenReturn(page10.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter11)))).
                thenReturn(page11.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter12)))).
                thenReturn(page12.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter20)))).
                thenReturn(page20.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter21)))).
                thenReturn(page21.getMetadata().getTotalItems().intValueExact());
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter22)))).
                thenReturn(page22.getMetadata().getTotalItems().intValueExact());

        for(int pageNumber : pageNumbers) {
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter00, pageNumber)))).
                    thenReturn(page00);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter01, pageNumber)))).
                    thenReturn(page01);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter02, pageNumber)))).
                    thenReturn(page02);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter10, pageNumber)))).
                    thenReturn(page10);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter11, pageNumber)))).
                    thenReturn(page11);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter12, pageNumber)))).
                    thenReturn(page12);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter20, pageNumber)))).
                    thenReturn(page20);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter21, pageNumber)))).
                    thenReturn(page21);
            Mockito.when(repository.getProducts(Mockito.eq(criteria(filter22, pageNumber)))).
                    thenReturn(page22);
        }

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

    private Criteria criteria(Filter filter, int pageNumber) {
        return new Criteria().
                setPageable(PageableByNumber.of(30, pageNumber)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria criteriaNumber(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Page<Product> productPage(User user,
                                      int productIndex,
                                      BiFunction<User, Integer, Product.Builder> productFactory) {
        Page.Metadata metadata = PageableByNumber.ofIndex(30, productIndex).
                createPageMetadata(1000, 30);

        int offset = metadata.getOffset().intValue();
        List<Product> products = IntStream.range(0, metadata.getActualSize()).
                mapToObj(i -> productFactory.apply(user, offset + i).tryBuild()).
                toList();

        return metadata.createPage(products);
    }

    private Page<Product> productPage(Product.Builder... products) {
        Page.Metadata metadata = PageableByNumber.of(30 , 0).
                createPageMetadata(products.length, 30);

        List<Product> resultProducts = Arrays.stream(products).
                map(Product.Builder::tryBuild).
                toList();

        return metadata.createPage(resultProducts);
    }

}
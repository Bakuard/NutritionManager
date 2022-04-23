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
            getMenuItemProducts(quantity, constraints):
             quantity is null
             => exception
            """)
    public void getMenuItemProducts1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(null, List.of()),
                "Menu.getMenuItemProducts",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             quantity = 0
             => exception
            """)
    public void getMenuItemProducts2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(BigDecimal.ZERO, List.of()),
                "Menu.getMenuItemProducts",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             quantity < 0
             => exception
            """)
    public void getMenuItemProducts3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(new BigDecimal(-1), List.of()),
                "Menu.getMenuItemProducts",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             constraints is null
             => exception
            """)
    public void getMenuItemProducts4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(BigDecimal.ONE, null),
                "Menu.getMenuItemProducts",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             constraints contains null
             => exception
            """)
    public void getMenuItemProducts5() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(BigDecimal.ONE, Arrays.asList(
                        new Menu.ProductConstraint("dish#1", 0, 0), null
                )),
                "Menu.getMenuItemProducts",
                Constraint.NOT_CONTAINS_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             menu haven't any items
             => return empty list
            """)
    public void getMenuItemProducts6() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.ONE,
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
            getMenuItemProducts(quantity, constraints):
             all menu dishes haven't ingredients
             => return empty list
            """)
    public void getMenuItemProducts7() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(createMenuItem(createDish(user, 1, repository), new BigDecimal(5))).
                addItem(createMenuItem(createDish(user, 2, repository), BigDecimal.ONE)).
                addItem(createMenuItem(createDish(user, 3, repository), BigDecimal.TEN)).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.ONE,
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
            getMenuItemProducts(quantity, constraints):
             all menu dish ingredients haven't suitable products
             => return empty list
            """)
    public void getMenuItemProducts8() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        User user = createUser(1);
        Menu menu = createMenu(1, user).
                addItem(
                        createMenuItem(
                                createDish(user, 1, repository,
                                        createIngredient(categoryFilter(user), new BigDecimal(5)),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(categoryFilter(user), BigDecimal.TEN),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(categoryFilter(user), BigDecimal.ONE),
                                        createIngredient(shopFilter(user), BigDecimal.ONE),
                                        createIngredient(gradeFilter(user), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.ONE,
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
            getMenuItemProducts(quantity, constraints):
             some menu dish ingredients have suitable products,
             => return correct result (skip dish ingredients without suitable products)
            """)
    public void getMenuItemProducts9() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        User user = createUser(1);
        MenuItem.Builder item1, item2, item3;
        Menu menu = createMenu(1, user).
                addItem(
                        item1 = createMenuItem(
                                createDish(user, 1, mockProductRepository(user, 0, 3, 2),
                                        createIngredient(categoryFilter(user), new BigDecimal(5)),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        item2 = createMenuItem(
                                createDish(user, 2, mockProductRepository(user, 1, 1, 4),
                                        createIngredient(categoryFilter(user), BigDecimal.TEN),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        item3 = createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(categoryFilter(user), BigDecimal.ONE),
                                        createIngredient(shopFilter(user), BigDecimal.ONE),
                                        createIngredient(gradeFilter(user), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.TEN,
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 1),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(250)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 2).tryBuild(), new BigDecimal(300)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 1).tryBuild(), new BigDecimal(20)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 4).tryBuild(), new BigDecimal(100))
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             all menu dish ingredients have suitable products,
             there are several ProductConstraint for some ingredients
             => return correct result
            """)
    public void getMenuItemProducts10() {
        User user = createUser(1);
        MenuItem.Builder item1, item2, item3;
        Menu menu = createMenu(1, user).
                addItem(
                        item1 = createMenuItem(
                                createDish(user, 1, mockProductRepository(user, 0, 3, 2),
                                        createIngredient(categoryFilter(user), new BigDecimal(5)),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        item2 = createMenuItem(
                                createDish(user, 2, mockProductRepository(user, 1, 1, 4),
                                        createIngredient(categoryFilter(user), BigDecimal.TEN),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        item3 = createMenuItem(
                                createDish(user, 3, mockProductRepository(user, 10, 15, 11),
                                        createIngredient(categoryFilter(user), BigDecimal.ONE),
                                        createIngredient(shopFilter(user), BigDecimal.ONE),
                                        createIngredient(gradeFilter(user), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.TEN,
                List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#1", 1, 3),
                        new Menu.ProductConstraint("dish#1", 2, 2),
                        new Menu.ProductConstraint("dish#1", 2, 11),
                        new Menu.ProductConstraint("dish#1", 2, 15),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 1),
                        new Menu.ProductConstraint("dish#2", 2, 4),
                        new Menu.ProductConstraint("dish#3", 0, 10),
                        new Menu.ProductConstraint("dish#3", 1, 15),
                        new Menu.ProductConstraint("dish#3", 2, 11)
                )
        );

        List<Menu.MenuItemProduct> expected = List.of(
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(250)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 2).tryBuild(), new BigDecimal(300)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 1).tryBuild(), new BigDecimal(20)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 4).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 10).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 15).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 11).tryBuild(), new BigDecimal(300))
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(quantity, constraints):
             all menu dish ingredients have suitable products,
             no products selected for ingredients
             => return correct result
            """)
    public void getMenuItemProducts11() {
        User user = createUser(1);
        MenuItem.Builder item1, item2, item3;
        Menu menu = createMenu(1, user).
                addItem(
                        item1 = createMenuItem(
                                createDish(user, 1, mockProductRepository(user, 0, 3, 2),
                                        createIngredient(categoryFilter(user), new BigDecimal(5)),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), new BigDecimal(6))),
                                new BigDecimal(5))
                ).
                addItem(
                        item2 = createMenuItem(
                                createDish(user, 2, mockProductRepository(user, 1, 1, 4),
                                        createIngredient(categoryFilter(user), BigDecimal.TEN),
                                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                                        createIngredient(shopFilter(user), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                addItem(
                        item3 = createMenuItem(
                                createDish(user, 3, mockProductRepository(user, 10, 15, 11),
                                        createIngredient(categoryFilter(user), BigDecimal.ONE),
                                        createIngredient(shopFilter(user), BigDecimal.ONE),
                                        createIngredient(gradeFilter(user), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        List<Menu.MenuItemProduct> actual = menu.getMenuItemProducts(
                BigDecimal.TEN,
                List.of()
        );

        List<Menu.MenuItemProduct> expected = List.of(
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(250)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item1.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(300)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(20)),
                createMenuItemProduct(item2.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(100)),
                createMenuItemProduct(item3.tryBuild(), createProduct(user, 0).tryBuild(), new BigDecimal(300))
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(product, products):
             product is null
             => exception
            """)
    public void getNecessaryQuantity1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(null, Map.of()),
                "Menu.getNecessaryQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(product, products):
             products is null
             => exception
            """)
    public void getNecessaryQuantity2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getNecessaryQuantity(createProduct(user, 0).tryBuild(), null),
                "Menu.getNecessaryQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(product, products):
             products is empty
             => return empty Optional
            """)
    public void getNecessaryQuantity3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getNecessaryQuantity(createProduct(user, 0).tryBuild(), Map.of());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(product, products):
             products not contains product
             => return empty Optional
            """)
    public void getNecessaryQuantity4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getNecessaryQuantity(createProduct(user, 0).tryBuild(), products);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(product, products):
             products contains product
             => return correct result
            """)
    public void getNecessaryQuantity5() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getNecessaryQuantity(createProduct(user, 10).tryBuild(), products);

        AssertUtil.assertEquals(new BigDecimal(350), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackQuantity(product, products):
             product is null
             => exception
            """)
    public void getLackQuantity1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackQuantity(null, Map.of()),
                "Menu.getLackQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(product, products):
             products is null
             => exception
            """)
    public void getLackQuantity2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackQuantity(createProduct(user, 0).tryBuild(), null),
                "Menu.getLackQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(product, products):
             products is empty
             => return empty Optional
            """)
    public void getLackQuantity3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getLackQuantity(createProduct(user, 0).tryBuild(), Map.of());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(product, products):
             products not contains product
             => return empty Optional
            """)
    public void getLackQuantity4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getLackQuantity(createProduct(user, 0).tryBuild(), products);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(product, products):
             products contains product
             => return correct result
            """)
    public void getLackQuantity5() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getLackQuantity(createProduct(user, 10).tryBuild(), products);

        AssertUtil.assertEquals(new BigDecimal(64), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(product, products):
             product is null
             => exception
            """)
    public void getLackQuantityPrice1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackQuantityPrice(null, Map.of()),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(product, products):
             products is null
             => exception
            """)
    public void getLackQuantityPrice2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackQuantityPrice(createProduct(user, 0).tryBuild(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(product, products):
             products is empty
             => return empty Optional
            """)
    public void getLackQuantityPrice3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getLackQuantityPrice(createProduct(user, 0).tryBuild(), Map.of());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(product, products):
             products not contains product
             => return empty Optional
            """)
    public void getLackQuantityPrice4() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getLackQuantityPrice(createProduct(user, 0).tryBuild(), products);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(product, products):
             products contains product
             => return correct result
            """)
    public void getLackQuantityPrice5() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getLackQuantityPrice(createProduct(user, 10).tryBuild(), products);

        AssertUtil.assertEquals(new BigDecimal(7040), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products):
             products is null
             => exception
            """)
    public void getLackProductsPrice1() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getLackProductsPrice(null),
                "Menu.getLackProductsPrice",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products):
             products is empty
             => return empty Optional
            """)
    public void getLackProductsPrice2() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        Optional<BigDecimal> actual = menu.getLackProductsPrice(Map.of());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductsPrice(products):
             products is not empty
             => return correct result
            """)
    public void getLackProductsPrice3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();
        MenuItem[] items = createMenuItems(user);
        Map<Product, List<Menu.MenuItemProduct>> products = Map.of(
                createProduct(user, 10).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 10).tryBuild(), new BigDecimal(250)),
                        createMenuItemProduct(items[2], createProduct(user, 10).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 3).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 3).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[1], createProduct(user, 3).tryBuild(), new BigDecimal(20))
                ),
                createProduct(user, 2).tryBuild(), List.of(
                        createMenuItemProduct(items[0], createProduct(user, 2).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 1).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 1).tryBuild(), new BigDecimal(100)),
                        createMenuItemProduct(items[2], createProduct(user, 1).tryBuild(), new BigDecimal(300))
                ),
                createProduct(user, 4).tryBuild(), List.of(
                        createMenuItemProduct(items[1], createProduct(user, 4).tryBuild(), new BigDecimal(100))
                ),
                createProduct(user, 15).tryBuild(), List.of(
                        createMenuItemProduct(items[2], createProduct(user, 15).tryBuild(), new BigDecimal(100))
                )
        );

        Optional<BigDecimal> actual = menu.getLackProductsPrice(products);

        AssertUtil.assertEquals(new BigDecimal(27520), actual.orElseThrow());
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
                setDishName(dish.getName()).
                setDish(() -> dish).
                setQuantity(quantity).
                setConfig(conf);
    }

    private MenuItem[] createMenuItems(User user) {
        MenuItem[] result = new MenuItem[3];

        result[0] = createMenuItem(
                createDish(user, 1, mockProductRepository(user, 10, 3, 2),
                        createIngredient(categoryFilter(user), new BigDecimal(5)),
                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                        createIngredient(shopFilter(user), new BigDecimal(6))
                ),
                new BigDecimal(5)
        ).tryBuild();
        result[1] = createMenuItem(
                createDish(user, 2, mockProductRepository(user, 1, 3, 4),
                        createIngredient(categoryFilter(user), BigDecimal.TEN),
                        createIngredient(gradeFilter(user), new BigDecimal(2)),
                        createIngredient(shopFilter(user), BigDecimal.TEN)),
                BigDecimal.ONE
        ).tryBuild();
        result[2] = createMenuItem(
                createDish(user, 3, mockProductRepository(user, 10, 15, 1),
                        createIngredient(categoryFilter(user), BigDecimal.ONE),
                        createIngredient(shopFilter(user), BigDecimal.ONE),
                        createIngredient(gradeFilter(user), new BigDecimal(3))),
                BigDecimal.TEN
        ).tryBuild();

        return result;
    }

    public Menu.MenuItemProduct createMenuItemProduct(MenuItem item, Product product, BigDecimal necessaryQuantity) {
        return new Menu.MenuItemProduct(
                item,
                product,
                necessaryQuantity,
                conf
        );
    }

    private ProductRepository mockProductRepository(User user,
                                                    int firstIngredientIndex,
                                                    int secondIngredientIndex,
                                                    int thirdIngredientIndex) {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(firstIngredientIndex, categoryFilter(user))))
                ).
                thenReturn(createProductPage(user, firstIngredientIndex));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(secondIngredientIndex, shopFilter(user))))
                ).
                thenReturn(createProductPage(user, secondIngredientIndex));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(thirdIngredientIndex, gradeFilter(user))))
                ).
                thenReturn(createProductPage(user, thirdIngredientIndex));
        return repository;
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private Filter categoryFilter(User user) {
        return Filter.and(
                Filter.anyCategory("categoryA"),
                Filter.user(user.getId())
        );
    }

    private Filter shopFilter(User user) {
        return Filter.and(
                Filter.anyShop("shopA"),
                Filter.user(user.getId())
        );
    }

    private Filter gradeFilter(User user) {
        return Filter.and(
                Filter.anyGrade("gradeA"),
                Filter.user(user.getId())
        );
    }

    private Criteria createCriteria(int itemIndex, Filter filter) {
        return new Criteria().
                setPageable(Pageable.ofIndex(30, itemIndex)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Page<Product> createProductPage(User user,
                                            int itemIndex) {
        int productsNumber = 20;
        List<Product> products = IntStream.range(0, productsNumber).
                mapToObj(i -> createProduct(user, i).tryBuild()).
                toList();

        return Pageable.ofIndex(30, itemIndex).
                createPageMetadata(productsNumber, 30).
                createPage(products);
    }

}
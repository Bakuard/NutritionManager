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
             constraints contain items where ingredientIndex < 0
             => exception
            """)
    public void getMenuItemProducts3() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", -1, 0)
                )),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where ingredientIndex = ingredients number
             => exception
            """)
    public void getMenuItemProducts4() {
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
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", 2, 0)
                )),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where ingredientIndex > ingredients number
             => exception
            """)
    public void getMenuItemProducts5() {
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
                addItem(
                        createMenuItem(
                                createDish(user, 3, repository,
                                        createIngredient(filter(user, 6), BigDecimal.ONE),
                                        createIngredient(filter(user, 7), BigDecimal.ONE),
                                        createIngredient(filter(user, 8), new BigDecimal(3))),
                                BigDecimal.TEN)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", 3, 0)
                )),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where productIndex < 0
             => exception
            """)
    public void getMenuItemProducts6() {
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
                addItem(
                        createMenuItem(
                                createDish(user, 2, repository,
                                        createIngredient(filter(user, 3), BigDecimal.TEN),
                                        createIngredient(filter(user, 4), new BigDecimal(2)),
                                        createIngredient(filter(user, 5), BigDecimal.TEN)),
                                BigDecimal.ONE)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint("dish#2", 1, -1)
                )),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             constraints contain items where dishName is null
             => exception
            """)
    public void getMenuItemProducts7() {
        User user = createUser(1);
        Menu menu = createMenu(1, user).tryBuild();

        AssertUtil.assertValidateException(
                () -> menu.getMenuItemProducts(List.of(
                        new Menu.ProductConstraint("dish#1", 0, 0),
                        new Menu.ProductConstraint(null, -1, 0)
                )),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getMenuItemProducts(constraints):
             menu haven't any items
             => return empty list
            """)
    public void getMenuItemProducts8() {
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
    public void getMenuItemProducts9() {
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
             all menu dish ingredients haven't suitable products
             => return list where MenuItemProduct.product() return empty Optional
            """)
    public void getMenuItemProducts10() {
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
                        new Menu.ProductConstraint("dish#1", 1, 0),
                        new Menu.ProductConstraint("dish#1", 2, 0),
                        new Menu.ProductConstraint("dish#2", 0, 0),
                        new Menu.ProductConstraint("dish#2", 1, 0),
                        new Menu.ProductConstraint("dish#2", 2, 0),
                        new Menu.ProductConstraint("dish#3", 0, 0),
                        new Menu.ProductConstraint("dish#3", 1, 0),
                        new Menu.ProductConstraint("dish#3", 2, 0)
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
    public void getMenuItemProducts11() {
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
                emptyMenuItemProduct(1, 0, 1),
                emptyMenuItemProduct(1, 1, 3),
                emptyMenuItemProduct(1, 2, 4),
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
    public void getMenuItemProducts12() {
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
                        new Menu.ProductConstraint("dish#1", 2, 15),
                        new Menu.ProductConstraint("dish#2", 0, 1),
                        new Menu.ProductConstraint("dish#2", 1, 3),
                        new Menu.ProductConstraint("dish#2", 1, 4),
                        new Menu.ProductConstraint("dish#2", 2, 4),
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
             all menu dish ingredients have suitable products,
             no products selected for ingredients
             => return correct result
            """)
    public void getMenuItemProducts13() {
        User user = createUser(1);
        ProductRepository repository = mockProductRepository(
                filter(user, 0), createProductPage(user, this::createProduct, 0, 2, 2, 4, 5),
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
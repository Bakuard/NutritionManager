package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.validation.Constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class DishTest {

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
            getLackQuantity(productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantity1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ingredient, -1, new BigDecimal("1.5")),
                Dish.class,
                "getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             servingNumber is not positive
             => exception
            """)
    public void getLackQuantity2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ingredient, 0, BigDecimal.ZERO),
                Dish.class,
                "getLackQuantity",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             servingNumber = null
             => exception
            """)
    public void getLackQuantity3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ingredient, 0, null),
                Dish.class,
                "getLackQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex = ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 5, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("8");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 5, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity6() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 6, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("8");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex > ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity7() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 6, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantity8() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 16).
                                createPageMetadata(20, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 16, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("20");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity9() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(ingredient, 16, new BigDecimal("2"));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantityPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ingredient, -1, new BigDecimal("1.5")),
                Dish.class,
                "getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             servingNumber is not positive
             => exception
            """)
    public void getLackQuantityPrice2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ingredient, 0, BigDecimal.ZERO),
                Dish.class,
                "getLackQuantityPrice",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             servingNumber = null
             => exception
            """)
    public void getLackQuantityPrice3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ingredient, 0, null),
                Dish.class,
                "getLackQuantityPrice",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex = ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantityPrice4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 5, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 5, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantityPrice6() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 6, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex > ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice7() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 6, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantityPrice8() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 16).
                                createPageMetadata(20, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 16, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice9() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ingredient, 16, new BigDecimal("2"));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getProductByIndex1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<Product> actual = dish.getProduct(ingredient, 0);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are products matching this ingredient,
             productIndex belongs to interval [0, ingredient products set size - 1]
             => return correct result
            """)
    public void getProductByIndex2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(4)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 4).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<Product> actual = dish.getProduct(ingredient, 4);

        Product expected = createProduct(5, 5).tryBuild();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are products matching this ingredient,
             productIndex = ingredient products number
             => return correct result
            """)
    public void getProductByIndex3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<Product> actual = dish.getProduct(ingredient, 5);

        Product expected = createProduct(5, 5).tryBuild();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are products matching this ingredient,
             productIndex > ingredient products number
             => return correct result
            """)
    public void getProductByIndex4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        Optional<Product> actual = dish.getProduct(ingredient, 6);

        Product expected = createProduct(5, 5).tryBuild();
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             productIndex < 0
             => exception
            """)
    public void getProductByIndex5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        DishIngredient.Builder ingredientBuilder = new DishIngredient.Builder().
                setName("some ingredient").
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
        Dish dish = createDish(1, createUser(1), repository).
                addIngredient(ingredientBuilder).
                tryBuild();
        DishIngredient ingredient = ingredientBuilder.tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(ingredient, -1),
                Dish.class,
                "getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }


    private User createUser(int id) {
        return new User(toUUID(id),
                "User" + id,
                "password" + id,
                "user" + id + "@mail.com");
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private List<Product> createProducts(int userId, int productNumber) {
        ArrayList<Product> products = new ArrayList<>();

        for(int i = 1; i <= productNumber; i++) {
            products.add(createProduct(userId, i).tryBuild());
        }

        return products;
    }

    private Product.Builder createProduct(int userId, int id) {
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(id)).
                setUser(createUser(userId)).
                setCategory("name " + id).
                setShop("shop " + id).
                setGrade("variety " + id).
                setManufacturer("manufacturer " + id).
                setUnit("unitA").
                setPrice(new BigDecimal(id * 10).abs()).
                setPackingSize(new BigDecimal("0.5").multiply(BigDecimal.valueOf(id))).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description " + id).
                setImageUrl("https://nutritionmanager.xyz/products/images?id=" + id).
                addTag("tag " + id).
                addTag("common tag");
    }

    private Dish.Builder createDish(int dishId,
                            User user,
                            ProductRepository repository) {
        return new Dish.Builder().
                setId(toUUID(dishId)).
                setUser(user).
                setName("dish A").
                setServingSize(BigDecimal.TEN).
                setUnit("unit A").
                setDescription("description A").
                setImagePath("https://nutritionmanager.xyz/products/images?id=1").
                setConfig(conf).
                setRepository(repository).
                addTag("tag A").
                addTag("common tag");
    }

    private Criteria createCriteria(int expectedPageNumber) {
        return new Criteria().
                setPageable(Pageable.ofIndex(5, expectedPageNumber)).
                setFilter(
                        Filter.and(
                                Filter.anyCategory("categoryA"),
                                Filter.user(toUUID(1))
                        )
                ).
                setSort(Sort.products().asc("price"));
    }

}
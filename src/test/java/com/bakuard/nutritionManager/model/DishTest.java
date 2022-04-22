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
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantity1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(0, -1, new BigDecimal("1.5")),
                "Dish.getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             servingNumber is not positive
             => exception
            """)
    public void getLackQuantity2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(0, 0, BigDecimal.ZERO),
                "Dish.getLackQuantity",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
           getLackQuantity(ingredientIndex, productIndex, servingNumber):
             servingNumber = null
             => exception
            """)
    public void getLackQuantity3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(0, 0, null),
                "Dish.getLackQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             ingredientIndex < 0
             => exception
            """)
    public void getLackQuantity4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(-1, 0, new BigDecimal("1.5")),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             ingredientIndex = dish ingredients number
             => exception
            """)
    public void getLackQuantity5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(1, 0, new BigDecimal("1.5")),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             ingredientIndex > dish ingredients number
             => exception
            """)
    public void getLackQuantity6() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(2, 0, new BigDecimal("1.5")),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex = ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity7() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 5, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 5, new BigDecimal("2"));

        AssertUtil.assertEquals(new BigDecimal("8"), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity8() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 5, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity9() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 6, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 6, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("8");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex > ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity10() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 6, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantity11() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 16, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 16, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal(8);
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientIndex, productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity12() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantity(0, 16, new BigDecimal("2"));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantityPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(0, -1, new BigDecimal("1.5")),
                "Dish.getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             servingNumber is not positive
             => exception
            """)
    public void getLackQuantityPrice2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(0, 0, BigDecimal.ZERO),
                "Dish.getLackQuantityPrice",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             servingNumber = null
             => exception
            """)
    public void getLackQuantityPrice3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(0, 0, null),
                "Dish.getLackQuantityPrice",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             ingredientIndex < 0
             => exception
            """)
    public void getLackQuantityPrice4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(-1, 0, BigDecimal.TEN),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             ingredientIndex = dish ingredients number
             => exception
            """)
    public void getLackQuantityPrice5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(1, 0, BigDecimal.TEN),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             ingredientIndex > dish ingredients number
             => exception
            """)
    public void getLackQuantityPrice6() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(2, 0, BigDecimal.TEN),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex = ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantityPrice7() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 5, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 5, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice8() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 5, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantityPrice9() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 6, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 6, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex > ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice10() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn( Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 6, BigDecimal.ONE);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantityPrice11() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 16, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 16, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("400");
        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientIndex, productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantityPrice12() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(16, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(0, 16, new BigDecimal("2"));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex < 0
             => exception
            """)
    public void getProduct1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(-1, 0),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex = dish ingredients number
             => exception
            """)
    public void getProduct2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(1, 0),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex > dish ingredients number
             => exception
            """)
    public void getProduct3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(2, 0),
                Constraint.RANGE
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             productIndex < 0
             => exception
            """)
    public void getProduct4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(0, -1),
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getProduct5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<Product> actual = dish.getProduct(0, 0);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex belongs to interval [0, ingredient products set size - 1]
             => return correct result
            """)
    public void getProduct6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(4, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 4, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<Product> actual = dish.getProduct(0, 4);

        Product expected = createProduct(user, 5).tryBuild();
        Assertions.assertEquals(expected, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex = ingredient products number
             => return correct result
            """)
    public void getProduct7() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 5, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<Product> actual = dish.getProduct(0, 5);

        Product expected = createProduct(user, 5).tryBuild();
        Assertions.assertEquals(expected, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex > ingredient products number
             => return correct result
            """)
    public void getProduct8() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 6, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Optional<Product> actual = dish.getProduct(0, 6);

        Product expected = createProduct(user, 5).tryBuild();
        Assertions.assertEquals(expected, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             productIndex < 0
             => exception
            """)
    public void getProduct9() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(0, -1),
                "Dish.getProduct",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             dish haven't any ingredients
             => return 0
            """)
    public void getNumberIngredientCombinations1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertEquals(BigInteger.ZERO, actual);
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             all dish ingredients haven't suitable products
             => return 0
            """)
    public void getNumberIngredientCombinations2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertEquals(BigInteger.ZERO, actual);
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             some dish ingredients have suitable products
             => return correct result
            """)
    public void getNumberIngredientCombinations3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(categoryFilter())))
                ).
                thenReturn(0);
        Mockito.when(
                    repository.getProductsNumber(Mockito.eq(createCriteria(shopFilter())))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(gradeFilter())))
                ).
                thenReturn(5);
        User user = createUser();
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertEquals(BigInteger.valueOf(50), actual);
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             all dish ingredients have suitable products
             => return correct result
            """)
    public void getNumberIngredientCombinations4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(categoryFilter())))
                ).
                thenReturn(2);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(shopFilter())))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(gradeFilter())))
                ).
                thenReturn(5);
        User user = createUser();
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertEquals(BigInteger.valueOf(100), actual);
    }

    @Test
    @DisplayName("""
            getMinPrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getMinPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getMinPrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getMinPrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user, 0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(
                createProductPage(user, 0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(BigDecimal.ZERO, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getMinPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user, 0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(new BigDecimal(100), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getMinPrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user, 0,
                        (u, i) -> createProduct(u, i).setPrice(new BigDecimal(i * 100)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(new BigDecimal(1200), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getMaxPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getMaxPrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getMaxPrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user, 100000,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, gradeFilter())))
        ).thenReturn(
                createProductPage(user, 100000,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(BigDecimal.ZERO, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getMaxPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user, 100000,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, gradeFilter())))
        ).thenReturn(createProductPage(user, 100000, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(new BigDecimal(500), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getMaxPrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(createProductPage(user, 100000, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user, 100000,
                        (u, i) -> createProduct(u, i).setPrice(new BigDecimal(i * 100)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, gradeFilter())))
        ).thenReturn(createProductPage(user, 100000, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(new BigDecimal(6000), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getAveragePrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getAveragePrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getAveragePrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(
                        Mockito.eq(createCriteria(100000, gradeFilter()))
                )
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(BigDecimal.ZERO, actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getAveragePrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, gradeFilter())))
        ).thenReturn(createProductPage(user, 0,  this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(new BigDecimal(300), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getAveragePrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(new BigDecimal(i * 100)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, gradeFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
        ).thenReturn(
                createProductPage(user,
                        0,
                        (u, i) -> createProduct(u, i).setPrice(new BigDecimal(i * 100)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(new BigDecimal(3600), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(servingNumber, ingredients):
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getLackProductPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = createUser();
        Dish dish = createDish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(BigDecimal.TEN, Map.of());

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(servingNumber, ingredients):
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getLackProductPrice2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Pageable.firstEmptyPage());
        User user = createUser();
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(
                BigDecimal.TEN,
                Map.of(0, 0,
                        1, 0,
                        2, 0)
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(servingNumber, ingredients):
             some dish ingredients have suitable products
             => return correct result
            """)
    public void getLackProductPrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
                ).
                thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(1, shopFilter())))
                ).
                thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
                ).
                thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(
                BigDecimal.TEN,
                Map.of(0, 0,
                        1, 1,
                        2, 0)
        );

        AssertUtil.assertEquals(new BigDecimal("4000"), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(servingNumber, ingredients):
             all dish ingredients have suitable products
             => return correct result
            """)
    public void getLackProductPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(4, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 4, this::createProduct));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(1, shopFilter())))
                ).
                thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(0, gradeFilter())))
                ).
                thenReturn(createProductPage(user, 0, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(
                BigDecimal.TEN,
                Map.of(0, 4,
                        1, 1,
                        2, 0)
        );

        AssertUtil.assertEquals(new BigDecimal("6000"), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(servingNumber, ingredients):
             all dish ingredients have suitable products,
             products not listed for some ingredients
             => return correct result
            """)
    public void getLackProductPrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(0, shopFilter())))
                ).
                thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                        repository.getProducts(Mockito.eq(createCriteria(2, gradeFilter())))
                ).
                thenReturn(createProductPage(user, 2, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(
                BigDecimal.TEN,
                Map.of(2, 2)
        );

        AssertUtil.assertEquals(new BigDecimal("6010"), actual.orElseThrow());
    }


    private User createUser() {
        return new User.Builder().
                setId(toUUID(1)).
                setName("User").
                setPassword("password").
                setEmail("user@mail.com").
                tryBuild();
    }

    private Product.Builder createProduct(User user, int id) {
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(id)).
                setUser(user).
                setCategory("category " + id).
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
                setImageUrl("https://nutritionmanager.xyz/dishes/images?id=" + dishId).
                setConfig(conf).
                setRepository(repository).
                addTag("tag A").
                addTag("common tag");
    }

    private DishIngredient.Builder createIngredient(Filter filter, int ingredientIndex) {
        return new DishIngredient.Builder().
                setName("some ingredient " + ingredientIndex).
                setFilter(filter).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
    }
    
    private Criteria createCriteria(int itemIndex, Filter filter) {
        return new Criteria().
                setPageable(Pageable.ofIndex(30, itemIndex)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria createCriteria(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Filter categoryFilter() {
        return Filter.and(
                Filter.anyCategory("categoryA"),
                Filter.user(toUUID(1))
        );
    }

    private Filter shopFilter() {
        return Filter.and(
                Filter.anyShop("shopA"),
                Filter.user(toUUID(1))
        );
    }

    private Filter gradeFilter() {
        return Filter.and(
                Filter.anyGrade("gradeA"),
                Filter.user(toUUID(1))
        );
    }

    private Page<Product> createProductPage(User user,
                                            int itemIndex,
                                            BiFunction<User, Integer, Product.Builder> productFactory) {
        int productsNumber = 5;
        List<Product> products = IntStream.rangeClosed(1, productsNumber).
                mapToObj(i -> productFactory.apply(user ,i).tryBuild()).
                toList();

        return Pageable.ofIndex(30, itemIndex).
                createPageMetadata(productsNumber, 30).
                createPage(products);
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
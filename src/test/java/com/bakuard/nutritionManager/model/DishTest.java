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
            getLackPackageQuantity(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackPackageQuantity1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantity(ip, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             servingNumber is negative
             => exception
            """)
    public void getLackPackageQuantity2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantity(ip, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             ingredientProduct is null
             => exception
            """)
    public void getLackPackageQuantity3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = null;

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantity(ip, new BigDecimal(2)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             servingNumber is zero
             => exception
            """)
    public void getLackPackageQuantity4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantity(ip, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             ingredientProduct.product() is empty Optional
             => return empty Optional
            """)
    public void getLackPackageQuantity5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackPackageQuantity(ip, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantity6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                createProduct(user, 10).setQuantity(new BigDecimal(2)), 0, 10
        );

        Optional<BigDecimal> actual = dish.getLackPackageQuantity(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(18), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackPackageQuantityPrice1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantityPrice(ip, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is negative
             => exception
            """)
    public void getLackPackageQuantityPrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantityPrice(ip, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             ingredientProduct is null
             => exception
            """)
    public void getLackPackageQuantityPrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = null;

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantityPrice(ip, new BigDecimal(2)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is zero
             => exception
            """)
    public void getLackPackageQuantityPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackPackageQuantityPrice(ip, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             ingredientProduct.product() is empty Optional
             => return empty Optional
            """)
    public void getLackPackageQuantityPrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackPackageQuantityPrice(ip, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantityPrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                createProduct(user, 10).setQuantity(new BigDecimal(2)), 0, 10
        );

        Optional<BigDecimal> actual = dish.getLackPackageQuantityPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(1980), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             ingredients is null
             => exception
            """)
    public void getLackProductPrice1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getLackProductPrice(null, BigDecimal.TEN),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackProductPrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 10), 0, 0),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        AssertUtil.assertValidateException(
                () -> dish.getLackProductPrice(ip, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             servingNumber is negate
             => exception
            """)
    public void getLackProductPrice3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 1), 0, 0),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        AssertUtil.assertValidateException(
                () -> dish.getLackProductPrice(ip, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             servingNumber is zero
             => exception
            """)
    public void getLackProductPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 1), 0, 0),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        AssertUtil.assertValidateException(
                () -> dish.getLackProductPrice(ip, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             ingredients is empty list
             => return empty Optional
            """)
    public void getLackProductPrice5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(List.of(), BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredients return empty optional for product
             => return empty Optional
            """)
    public void getLackProductPrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                emptyIngredientProduct(0, 1),
                emptyIngredientProduct(1, 10),
                emptyIngredientProduct(2, 0)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             some ingredients return empty optional for product
             => return correct result (skip all ingredients with empty Optional for product)
            """)
    public void getLackProductPrice7() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                emptyIngredientProduct(0, 1),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(4000), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredient have products
             => return correct result
            """)
    public void getLackProductPrice8() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 10), 0, 10),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(6090), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredient have products,
             several ingredients use the same product
             => return correct result
            """)
    public void getLackProductPrice9() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 1).
                                setPackingSize(new BigDecimal(23)).
                                setPrice(new BigDecimal(550)).
                                setQuantity(new BigDecimal(45)),
                        0, 1),
                ingredientProduct(createProduct(user, 1).
                                setPackingSize(new BigDecimal(23)).
                                setPrice(new BigDecimal(550)).
                                setQuantity(new BigDecimal(45)),
                        0, 1),
                ingredientProduct(createProduct(user, 3).
                                setPackingSize(new BigDecimal(5)).
                                setPrice(new BigDecimal(250)),
                        2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(8850), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex < 0
             => exception
            """)
    public void getProduct1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 0);

        Assertions.assertTrue(actual.product().isEmpty());
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
                        repository.getProducts(Mockito.eq(createCriteria(4, filter(user, 0))))
                ).
                thenReturn(createProductPage(user, 4, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 4);

        Dish.IngredientProduct expected = ingredientProduct(createProduct(user, 4), 0, 4);
        Assertions.assertEquals(expected, actual);
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
                        repository.getProducts(Mockito.eq(createCriteria(5, filter(user, 0))))
                ).
                thenReturn(createProductPage(user, 5, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 5);

        Dish.IngredientProduct expected = ingredientProduct(createProduct(user, 5), 0, 5);
        Assertions.assertEquals(expected, actual);
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
                        repository.getProducts(Mockito.eq(createCriteria(6, filter(user, 0))))
                ).
                thenReturn(createProductPage(user, 6, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 6);

        Dish.IngredientProduct expected = ingredientProduct(createProduct(user, 6), 0, 6);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints is null
             => exception
            """)
    public void getProductForEachIngredient1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain null items
             => exception
            """)
    public void getProductForEachIngredient2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        Arrays.asList(new Dish.ProductConstraint(0, 0), null)
                ),
                Constraint.NOT_CONTAINS_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex < 0
             => exception
            """)
    public void getProductForEachIngredient3() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(0, 0),
                                new Dish.ProductConstraint(-1, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex = ingredients number
             => exception
            """)
    public void getProductForEachIngredient4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(1, 0),
                                new Dish.ProductConstraint(0, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex > ingredients number
             => exception
            """)
    public void getProductForEachIngredient5() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(2, 0),
                                new Dish.ProductConstraint(0, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where productIndex < 0
             => exception
            """)
    public void getProductForEachIngredient6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(0, 0),
                                new Dish.ProductConstraint(0, -1))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION,
                Constraint.IS_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             one of ingredient haven't any products
             => empty item for this ingredient
            """)
    public void getProductForEachIngredient7() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(1, filter(user, 1))))
        ).thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(3, filter(user, 2))))
        ).thenReturn(createProductPage(user, 3, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(0, 0),
                        new Dish.ProductConstraint(1, 1),
                        new Dish.ProductConstraint(2, 3)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                emptyIngredientProduct(0, 0),
                ingredientProduct(createProduct(user, 1), 1, 1),
                ingredientProduct(createProduct(user, 3), 2, 3)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             all ingredients have suitable products,
             there are several ProductConstraint for some ingredients
             => return correct result
            """)
    public void getProductForEachIngredient8() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(1, filter(user, 1))))
        ).thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(3, filter(user, 2))))
        ).thenReturn(createProductPage(user, 3, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(0, 0),
                        new Dish.ProductConstraint(1, 1),
                        new Dish.ProductConstraint(2, 3),
                        new Dish.ProductConstraint(1, 13),
                        new Dish.ProductConstraint(2, 0)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(createProduct(user, 0), 0, 0),
                ingredientProduct(createProduct(user, 1), 1, 1),
                ingredientProduct(createProduct(user, 3), 2, 3)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             all ingredients have suitable products,
             no products selected for ingredients
             => return correct result (default product for ingredients)
            """)
    public void getProductForEachIngredient9() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(10, filter(user, 1))))
        ).thenReturn(createProductPage(user, 10, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(4, filter(user, 2))))
        ).thenReturn(createProductPage(user, 4, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(2, 4)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(createProduct(user, 0), 0, 0),
                ingredientProduct(createProduct(user, 0), 1, 0),
                ingredientProduct(createProduct(user, 4), 2, 4)
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             dish haven't ingredients
             => return empty list
            """)
    public void getProductForEachIngredient10() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                    new Dish.ProductConstraint(0, 0),
                    new Dish.ProductConstraint(1, 1),
                    new Dish.ProductConstraint(2, 3)
                )
        );

        Assertions.assertTrue(actual.isEmpty());
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
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 0))))
                ).
                thenReturn(0);
        Mockito.when(
                    repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 1))))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 2))))
                ).
                thenReturn(5);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 0))))
                ).
                thenReturn(2);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 1))))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(createCriteria(filter(user, 2))))
                ).
                thenReturn(5);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 25, 50, 501)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 44, 45, 46)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(BigDecimal.ZERO).
                                setQuantity(new BigDecimal(1000)),
                        100, 101, 102)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(BigDecimal.TEN).
                                setQuantity(new BigDecimal(1000)),
                        10, 11, 12)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(new BigDecimal(200), actual.orElseThrow());
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
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(570)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        520, 521, 522)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(new BigDecimal(7700), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dish ingredients have suitable products,
             all products cost more than 0,
             several ingredients use the same product
             => return correct result
            """)
    public void getMinPrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(createProductPage(user, 0,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(createProductPage(user, 0,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(10)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        AssertUtil.assertEquals(new BigDecimal(1850), actual.orElseThrow());
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
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 0, 12, 25)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 36, 74, 82)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(BigDecimal.ZERO),
                        10, 11, 12)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(1500)).
                                setPackingSize(new BigDecimal(250)).
                                setQuantity(new BigDecimal(1000)),
                        203, 204, 205)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(new BigDecimal(1500), actual.orElseThrow());
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(createProductPage(user,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(120)).
                        setQuantity(new BigDecimal(1000)),
                0, 1, 2, 3)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(500)).
                                setPackingSize(new BigDecimal(200)).
                                setQuantity(new BigDecimal(1000)),
                        11, 12, 13)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(150)).
                                setQuantity(new BigDecimal(1000)),
                        51, 52, 53)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(new BigDecimal(1350), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dish ingredients have suitable products,
             all products cost more than 0,
             several ingredients use the same product
             => return correct result
            """)
    public void getMaxPrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(createProductPage(user,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(5)),
                12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(600)).
                                setPackingSize(new BigDecimal(7)).
                                setQuantity(new BigDecimal(5)),
                        12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(15)).
                                setQuantity(new BigDecimal(1000)),
                        101, 111, 121)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        AssertUtil.assertEquals(new BigDecimal(2050), actual.orElseThrow());
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
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 0, 12, 25)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 36, 74, 82)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 25, 50, 501)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).setPrice(BigDecimal.ZERO), 44, 45, 46)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(BigDecimal.ZERO),
                        10, 11, 12)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(1500)).
                                setPackingSize(new BigDecimal(250)).
                                setQuantity(new BigDecimal(1000)),
                        203, 204, 205)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(BigDecimal.ZERO).
                                setQuantity(new BigDecimal(1000)),
                        100, 101, 102)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(BigDecimal.TEN).
                                setQuantity(new BigDecimal(1000)),
                        10, 11, 12)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(new BigDecimal(850), actual.orElseThrow());
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
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(createProductPage(user,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(120)).
                        setQuantity(new BigDecimal(1000)),
                0, 1, 2, 3)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(500)).
                                setPackingSize(new BigDecimal(200)).
                                setQuantity(new BigDecimal(1000)),
                        11, 12, 13)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(150)).
                                setQuantity(new BigDecimal(1000)),
                        51, 52, 53)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(570)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        520, 521, 522)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(new BigDecimal(4525), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dish ingredients have suitable products,
             all products cost more than 0,
             several ingredients use the same product
             => return correct result
            """)
    public void getAveragePrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 0))))
        ).thenReturn(createProductPage(user,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(5)),
                12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 1))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(600)).
                                setPackingSize(new BigDecimal(7)).
                                setQuantity(new BigDecimal(5)),
                        12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(100000, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(15)).
                                setQuantity(new BigDecimal(1000)),
                        101, 111, 121)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 0))))
        ).thenReturn(createProductPage(user, 0,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 1))))
        ).thenReturn(createProductPage(user, 0,
                (u, i) -> createProduct(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)))
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, filter(user, 2))))
        ).thenReturn(
                createProductPage(user,
                        (u, i) -> createProduct(u, i).
                                setPrice(new BigDecimal(10)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(filter(user, 0), 0)).
                addIngredient(createIngredient(filter(user, 1), 1)).
                addIngredient(createIngredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        AssertUtil.assertEquals(new BigDecimal(1950), actual.orElseThrow());
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
    
    private Criteria createCriteria(int productIndex, Filter filter) {
        return new Criteria().
                setPageable(Pageable.ofIndex(30, productIndex)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria createCriteria(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Filter filter(User user, int num) {
        return Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("tag" + num), new Tag("common tag")),
                Filter.anyCategory("category " + num),
                Filter.anyGrade("grade " + num)
        );
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

    private Dish.IngredientProduct ingredientProduct(Product.Builder builder,
                                                     int ingredientIndex,
                                                     int productIndex) {
        return new Dish.IngredientProduct(
                Optional.ofNullable(builder.tryBuild()),
                ingredientIndex,
                productIndex
        );
    }

    private Dish.IngredientProduct emptyIngredientProduct(int ingredientIndex,
                                                          int productIndex) {
        return new Dish.IngredientProduct(Optional.empty(), ingredientIndex, productIndex);
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
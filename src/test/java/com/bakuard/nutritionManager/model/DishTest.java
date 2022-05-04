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
            getLackQuantity(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackQuantity1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ip, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             servingNumber is negative
             => exception
            """)
    public void getLackQuantity2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ip, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             ingredientProduct is null
             => exception
            """)
    public void getLackQuantity3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = null;

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ip, new BigDecimal(2)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             servingNumber is zero
             => exception
            """)
    public void getLackQuantity4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantity(ip, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             ingredientProduct.product() is empty Optional
             => return empty Optional
            """)
    public void getLackQuantity5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackQuantity(ip, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackQuantity6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                createProduct(user, 10).setQuantity(new BigDecimal(2)),
                0,
                10
        );

        Optional<BigDecimal> actual = dish.getLackQuantity(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(98), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackQuantityPrice1() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ip, null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is negative
             => exception
            """)
    public void getLackQuantityPrice2() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ip, new BigDecimal(-1)),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientProduct, servingNumber):
             ingredientProduct is null
             => exception
            """)
    public void getLackQuantityPrice3() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = null;

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ip, new BigDecimal(2)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is zero
             => exception
            """)
    public void getLackQuantityPrice4() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(createProduct(user, 10), 0, 1);

        AssertUtil.assertValidateException(
                () -> dish.getLackQuantityPrice(ip, BigDecimal.ZERO),
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(ingredientProduct, servingNumber):
             ingredientProduct.product() is empty Optional
             => return empty Optional
            """)
    public void getLackQuantityPrice5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackQuantityPrice(ip, BigDecimal.TEN);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantity(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackQuantityPrice6() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                createProduct(user, 10).setQuantity(new BigDecimal(2)),
                0,
                10
        );

        Optional<BigDecimal> actual = dish.getLackQuantity(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(9800), actual.orElseThrow());
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                emptyIngredientProduct(0, 1),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(4010), actual.orElseThrow());
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             
            """)
    public void getLackProductPrice8() {
        User user = createUser();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(createProduct(user, 10), 0, 10),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        AssertUtil.assertEquals(new BigDecimal(6010), actual.orElseThrow());
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
                        repository.getProducts(Mockito.eq(createCriteria(4, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 4, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 4);

        Dish.IngredientProduct expected = ingredientProduct(createProduct(user, 5), 0, 4);
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
                        repository.getProducts(Mockito.eq(createCriteria(5, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 5, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
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
                        repository.getProducts(Mockito.eq(createCriteria(6, categoryFilter())))
                ).
                thenReturn(createProductPage(user, 6, this::createProduct));
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        Dish.IngredientProduct actual = dish.getProduct(0, 6);

        Dish.IngredientProduct expected = ingredientProduct(createProduct(user, 5), 0, 6);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints is null
             => exception
            """)
    public void getProductForEachIngredient1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
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
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
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
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(0, 0),
                                new Dish.ProductConstraint(-1, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex = ingredients number
             => exception
            """)
    public void getProductForEachIngredient4() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(1, 0),
                                new Dish.ProductConstraint(0, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex > ingredients number
             => exception
            """)
    public void getProductForEachIngredient5() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(2, 0),
                                new Dish.ProductConstraint(0, 0))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION
        );
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where productIndex < 0
             => exception
            """)
    public void getProductForEachIngredient6() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = createDish(1, createUser(), repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProductForEachIngredient(
                        List.of(new Dish.ProductConstraint(0, 0),
                                new Dish.ProductConstraint(0, -1))
                ),
                Constraint.NOT_CONTAINS_BY_CONDITION
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
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(Pageable.firstEmptyPage());
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(1, categoryFilter())))
        ).thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(3, gradeFilter())))
        ).thenReturn(createProductPage(user, 3, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(1, categoryFilter())))
        ).thenReturn(createProductPage(user, 1, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(3, gradeFilter())))
        ).thenReturn(createProductPage(user, 3, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
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
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(0, categoryFilter())))
        ).thenReturn(createProductPage(user, 0, this::createProduct));
        Mockito.when(
                repository.getProducts(Mockito.eq(createCriteria(4, gradeFilter())))
        ).thenReturn(createProductPage(user, 4, this::createProduct));
        Dish dish = createDish(1, user, repository).
                addIngredient(createIngredient(categoryFilter(), 0)).
                addIngredient(createIngredient(shopFilter(), 1)).
                addIngredient(createIngredient(gradeFilter(), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(2, 4)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(createProduct(user, 0), 0, 0),
                ingredientProduct(createProduct(user, 1), 1, 0),
                ingredientProduct(createProduct(user, 3), 2, 4)
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
package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableById;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class DishTest {

    @Autowired
    private ConfigData conf;

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackPackageQuantity1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackPackageQuantity(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getLackPackageQuantity(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantity6() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                product(user, 10).setQuantity(new BigDecimal(2)), 0, 10
        );

        Optional<BigDecimal> actual = dish.getLackPackageQuantity(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(18));
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             servingNumber is null
             => exception
            """)
    public void getLackPackageQuantityPrice1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(product(user, 10), 0, 1);

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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = emptyIngredientProduct(0, 1);

        Optional<BigDecimal> actual = dish.getLackPackageQuantityPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getLackPackageQuantityPrice(ingredientProduct, servingNumber):
             all arguments is correct
             => return correct result
            """)
    public void getLackPackageQuantityPrice6() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();
        Dish.IngredientProduct ip = ingredientProduct(
                product(user, 10).setQuantity(new BigDecimal(2)), 0, 10
        );

        Optional<BigDecimal> actual = dish.getLackPackageQuantityPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(1980));
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             ingredients is null
             => exception
            """)
    public void getLackProductPrice1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(product(user, 10), 0, 0),
                ingredientProduct(product(user, 1), 1, 0),
                ingredientProduct(product(user, 3), 2, 4)
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(product(user, 1), 0, 0),
                ingredientProduct(product(user, 1), 1, 0),
                ingredientProduct(product(user, 3), 2, 4)
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(product(user, 1), 0, 0),
                ingredientProduct(product(user, 1), 1, 0),
                ingredientProduct(product(user, 3), 2, 4)
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getLackProductPrice(List.of(), BigDecimal.TEN);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredients return empty optional for product
             => return empty Optional
            """)
    public void getLackProductPrice6() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                emptyIngredientProduct(0, 1),
                emptyIngredientProduct(1, 10),
                emptyIngredientProduct(2, 0)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             some ingredients return empty optional for product
             => return correct result (skip all ingredients with empty Optional for product)
            """)
    public void getLackProductPrice7() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                emptyIngredientProduct(0, 1),
                ingredientProduct(product(user, 1), 1, 0),
                ingredientProduct(product(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(4000));
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredient have products
             => return correct result
            """)
    public void getLackProductPrice8() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 1), 1, 0),
                ingredientProduct(product(user, 3), 2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(6090));
    }

    @Test
    @DisplayName("""
            getLackProductPrice(ingredients, servingNumber):
             all ingredient have products,
             several ingredients use the same product
             => return correct result
            """)
    public void getLackProductPrice9() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();
        List<Dish.IngredientProduct> ip = List.of(
                ingredientProduct(product(user, 1).
                                setPackingSize(new BigDecimal(23)).
                                setPrice(new BigDecimal(550)).
                                setQuantity(new BigDecimal(45)),
                        0, 1),
                ingredientProduct(product(user, 1).
                                setPackingSize(new BigDecimal(23)).
                                setPrice(new BigDecimal(550)).
                                setQuantity(new BigDecimal(45)),
                        0, 1),
                ingredientProduct(product(user, 3).
                                setPackingSize(new BigDecimal(5)).
                                setPrice(new BigDecimal(250)),
                        2, 4)
        );

        Optional<BigDecimal> actual = dish.getLackProductPrice(ip, BigDecimal.TEN);

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(8850));
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex < 0
             => return empty Optional
            """)
    public void getProductByIndex1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(-1, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex = dish ingredients number
             => return empty Optional
            """)
    public void getProductByIndex2() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(1, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             ingredientIndex > dish ingredients number
             => return empty Optional
            """)
    public void getProductByIndex3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(2, 0);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             productIndex < 0
             => return item where IngredientProduct.product() return empty Optional
            """)
    public void getProductByIndex4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(6, filter(user, 0))))
                ).
                thenReturn(productPage(user, this::product, 0, 1, 2, 3, 4));
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(0, -1);

        Assertions.assertThat(actual).
                isPresent().
                get().
                extracting(Dish.IngredientProduct::product, InstanceOfAssertFactories.optional(Product.class)).
                isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are not products matching this ingredient
             => return item where IngredientProduct.product() return empty Optional
            """)
    public void getProductByIndex5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Page.empty());
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(0, 0);

        Assertions.assertThat(actual).
                isPresent().
                get().
                extracting(Dish.IngredientProduct::product, InstanceOfAssertFactories.optional(Product.class)).
                isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex belongs to interval [0, ingredient products set size - 1]
             => return correct result
            """)
    public void getProductByIndex6() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(4, filter(user, 0))))
                ).
                thenReturn(productPage(user, 4, this::product));
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(0, 4);

        Dish.IngredientProduct expected = ingredientProduct(product(user, 4), 0, 4);
        Assertions.assertThat(actual).
                isPresent().
                contains(expected);
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex = ingredient products number
             => return item where IngredientProduct.product() return empty Optional
            """)
    public void getProductByIndex7() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(5, filter(user, 0))))
                ).
                thenReturn(productPage(user, this::product, 0, 1, 2, 3, 4));
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(0, 5);

        Assertions.assertThat(actual).
                isPresent().
                get().
                extracting(Dish.IngredientProduct::product, InstanceOfAssertFactories.optional(Product.class)).
                isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientIndex, productIndex):
             there are products matching this ingredient,
             productIndex > ingredient products number
             => return item where IngredientProduct.product() return empty Optional
            """)
    public void getProductByIndex8() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(6, filter(user, 0))))
                ).
                thenReturn(productPage(user, this::product, 0, 1, 2, 3, 4));
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(0, 6);

        Assertions.assertThat(actual).
                isPresent().
                get().
                extracting(Dish.IngredientProduct::product, InstanceOfAssertFactories.optional(Product.class)).
                isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientId, productId):
             ingredientId is null
             => exception
            """)
    public void getProductById1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(6, filter(user, 0))))
                ).
                thenReturn(productPage(user, this::product, 0, 1, 2, 3, 4));
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(null, toUUID(0)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientId, productId):
             productId is null
             => exception
            """)
    public void getProductById2() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProducts(Mockito.eq(criteria(6, filter(user, 0))))
                ).
                thenReturn(productPage(user, this::product, 0, 1, 2, 3, 4));
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dish.getProduct(toUUID(0), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProduct(ingredientId, productId):
             dish doesn't contains ingredient with ingredientId
             => return empty Optional
            """)
    public void getProductById3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(10, filter(user, 0))))
        ).thenReturn(productPageById(user, this::product, 10,0,1,2,3,4,5,6,7,8,9,10));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(11);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(13, filter(user, 1))))
        ).thenReturn(productPageById(user, this::product, 13,11,12,13,14));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(4);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(400, filter(user, 2))))
        ).thenReturn(productPageById(user, this::product, 400,100,200,300,400,500,600,700));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(7);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(toUUID(1000), toUUID(10));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProduct(ingredientId, productId):
             dish doesn't contains product with productId
             => return item where IngredientProduct#product() return empty Optional
            """)
    public void getProductById4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(1000, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(11);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(13, filter(user, 1))))
        ).thenReturn(productPageById(user, this::product, 13,11,12,13,14));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(4);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(400, filter(user, 2))))
        ).thenReturn(productPageById(user, this::product, 400,100,200,300,400,500,600,700));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(7);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(toUUID(0), toUUID(1000));

        Dish.IngredientProduct expected = emptyIngredientProduct(0, -1);
        Assertions.assertThat(actual).
                isPresent().
                contains(expected);
    }

    @Test
    @DisplayName("""
            getProduct(ingredientId, productId):
             dish contains product with productId and ingredient with ingredientId
             => return correct result
            """)
    public void getProductById5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(10, filter(user, 0))))
        ).thenReturn(productPageById(user, this::product, 10,0,1,2,3,4,5,6,7,8,9,10));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(11);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(13, filter(user, 1))))
        ).thenReturn(productPageById(user, this::product, 13,11,12,13,14));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(4);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteriaById(400, filter(user, 2))))
        ).thenReturn(productPageById(user, this::product, 400,100,200,300,400,500,600,700));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(7);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<Dish.IngredientProduct> actual = dish.getProduct(toUUID(0), toUUID(10));

        Dish.IngredientProduct expected = ingredientProduct(product(user, 10),0, 10);
        Assertions.assertThat(actual).
                isPresent().
                contains(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints is null
             => exception
            """)
    public void getProductForEachIngredient1() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
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
             dish haven't ingredients
             => return empty list
            """)
    public void getProductForEachIngredient3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Dish dish = dish(1, user, repository).tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(0, 0),
                        new Dish.ProductConstraint(1, 3),
                        new Dish.ProductConstraint(2, 1)
                )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex < 0
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, 10, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(-1, 3),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 0), 1, 0),
                ingredientProduct(product(user, 1), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex = ingredients number
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, 10, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(3, 3),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 0), 1, 0),
                ingredientProduct(product(user, 1), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where ingredientIndex > ingredients number
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient6() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, 10, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(4, 3),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 0), 1, 0),
                ingredientProduct(product(user, 1), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where productIndex < 0
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient7() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, 10, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(-1, filter(user, 1))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(1, -1),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 0), 1, 0),
                ingredientProduct(product(user, 1), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where productIndex = ingredient products number
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient8() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, this::product, 0,1,2,3,4,5,6,7,8,9,10));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(11);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(4, filter(user, 1))))
        ).thenReturn(productPage(user, this::product, 11,12,13,14));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(4);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, this::product, 100,200,300,400,500,600,700));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(7);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(1, 4),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 11), 1, 0),
                ingredientProduct(product(user, 200), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             constraints contain items where productIndex > ingredient products number
             => return correct result (skip this items and use default value)
            """)
    public void getProductForEachIngredient9() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(10, filter(user, 0))))
        ).thenReturn(productPage(user, this::product, 0,1,2,3,4,5,6,7,8,9,10));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(11);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(4, filter(user, 1))))
        ).thenReturn(productPage(user, this::product, 11,12,13,14));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(4);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 2))))
        ).thenReturn(productPage(user, this::product, 100,200,300,400,500,600,700));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(7);
        Dish dish = dish(1, user(), repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(List.of(
                new Dish.ProductConstraint(0, 10),
                new Dish.ProductConstraint(1, 5),
                new Dish.ProductConstraint(2, 1)
        ));

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 10), 0, 10),
                ingredientProduct(product(user, 11), 1, 0),
                ingredientProduct(product(user, 200), 2, 1)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             one of ingredient haven't any products
             => empty item for this ingredient
            """)
    public void getProductForEachIngredient10() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(0);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 1))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(3, filter(user, 2))))
        ).thenReturn(productPage(user, 3, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
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
                ingredientProduct(product(user, 1), 1, 1),
                ingredientProduct(product(user, 3), 2, 3)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             all ingredients have suitable products,
             there are several ProductConstraint for some ingredients
             => return correct result (use first ProductConstraint for each ingredient)
            """)
    public void getProductForEachIngredient11() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(1, filter(user, 1))))
        ).thenReturn(productPage(user, 1, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(3, filter(user, 2))))
        ).thenReturn(productPage(user, 3, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(0, 0),
                        new Dish.ProductConstraint(1, 1000000),
                        new Dish.ProductConstraint(2, -1),
                        new Dish.ProductConstraint(1, 1),
                        new Dish.ProductConstraint(2, 3),
                        new Dish.ProductConstraint(1, 10),
                        new Dish.ProductConstraint(2, 20)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 0), 0, 0),
                ingredientProduct(product(user, 1), 1, 1),
                ingredientProduct(product(user, 3), 2, 3)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProductForEachIngredient(constraints):
             all ingredients have suitable products,
             no products selected for ingredients
             => return correct result (default product for ingredients)
            """)
    public void getProductForEachIngredient12() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user, 0, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))
        ).thenReturn(1000);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(4, filter(user, 2))))
        ).thenReturn(productPage(user, 4, this::product));
        Mockito.when(
                repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))
        ).thenReturn(1000);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        List<Dish.IngredientProduct> actual = dish.getProductForEachIngredient(
                List.of(
                        new Dish.ProductConstraint(2, 4)
                )
        );

        List<Dish.IngredientProduct> expected = List.of(
                ingredientProduct(product(user, 0), 0, 0),
                ingredientProduct(product(user, 0), 1, 0),
                ingredientProduct(product(user, 4), 2, 4)
        );
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             dish haven't any ingredients
             => return 0
            """)
    public void getNumberIngredientCombinations1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = user();
        Dish dish = dish(1, user, repository).tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             all dish ingredients haven't suitable products
             => return 0
            """)
    public void getNumberIngredientCombinations2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = user();
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             some dish ingredients have suitable products
             => return correct result
            """)
    public void getNumberIngredientCombinations3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(criteria(filter(user, 0))))
                ).
                thenReturn(0);
        Mockito.when(
                    repository.getProductsNumber(Mockito.eq(criteria(filter(user, 1))))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(criteria(filter(user, 2))))
                ).
                thenReturn(5);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertThat(actual).isEqualTo(BigInteger.valueOf(50));
    }

    @Test
    @DisplayName("""
            getNumberIngredientCombinations():
             all dish ingredients have suitable products
             => return correct result
            """)
    public void getNumberIngredientCombinations4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(criteria(filter(user, 0))))
                ).
                thenReturn(2);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(criteria(filter(user, 1))))
                ).
                thenReturn(10);
        Mockito.when(
                        repository.getProductsNumber(Mockito.eq(criteria(filter(user, 2))))
                ).
                thenReturn(5);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        BigInteger actual = dish.getNumberIngredientCombinations();

        Assertions.assertThat(actual).isEqualTo(BigInteger.valueOf(100));
    }

    @Test
    @DisplayName("""
            getMinPrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getMinPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = user();
        Dish dish = dish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getMinPrice2() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Page.empty());
        Mockito.when(repository.getProductsNumber(Mockito.any())).
                thenReturn(0);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getMinPrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getMinPrice3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 25, 50, 501)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 44, 45, 46)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("""
            getMinPrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getMinPrice4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(BigDecimal.ZERO).
                                setQuantity(new BigDecimal(1000)),
                        100, 101, 102)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(BigDecimal.TEN).
                                setQuantity(new BigDecimal(1000)),
                        10, 11, 12)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(200));
    }

    @Test
    @DisplayName("""
            getMinPrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getMinPrice5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(570)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        520, 521, 522)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(7700));
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(10)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMinPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(1850));
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getMaxPrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = user();
        Dish dish = dish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getMaxPrice2() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Page.empty());
        Mockito.when(repository.getProductsNumber(Mockito.any())).
                thenReturn(0);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getMaxPrice3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 0)))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 1)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 2)))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 0, 12, 25)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 36, 74, 82)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getMaxPrice4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 0)))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 1)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 2)))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(BigDecimal.ZERO),
                        10, 11, 12)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(1500)).
                                setPackingSize(new BigDecimal(250)).
                                setQuantity(new BigDecimal(1000)),
                        203, 204, 205)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(1500));
    }

    @Test
    @DisplayName("""
            getMaxPrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getMaxPrice5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 0)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 1)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 2)))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(120)).
                        setQuantity(new BigDecimal(1000)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(500)).
                                setPackingSize(new BigDecimal(200)).
                                setQuantity(new BigDecimal(1000)),
                        11, 12, 13)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(150)).
                                setQuantity(new BigDecimal(1000)),
                        51, 52, 53)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(1350));
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 0)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 1)))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(criteriaNumber(filter(user, 2)))).
                thenReturn(3);
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(5)),
                12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(600)).
                                setPackingSize(new BigDecimal(7)).
                                setQuantity(new BigDecimal(5)),
                        12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(15)).
                                setQuantity(new BigDecimal(1000)),
                        101, 111, 121)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getMaxPrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(2050));
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             dish haven't any ingredients
             => return empty Optional
            """)
    public void getAveragePrice1() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        User user = user();
        Dish dish = dish(1, user, repository).tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dish ingredients haven't suitable products
             => return empty Optional
            """)
    public void getAveragePrice2() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).
                thenReturn(Page.empty());
        Mockito.when(repository.getProductsNumber(Mockito.any())).
                thenReturn(0);
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             some dish ingredients have suitable products,
             all products cost zero
             => return 0
            """)
    public void getAveragePrice3() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);

        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 25, 50, 501)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 44, 45, 46)
        );

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 0, 12, 25)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).setPrice(BigDecimal.ZERO), 36, 74, 82)
        );

        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();


        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             some dish ingredients have suitable products,
             some products cost zero
             => return correct result
            """)
    public void getAveragePrice4() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(0);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(BigDecimal.ZERO).
                                setQuantity(new BigDecimal(1000)),
                        100, 101, 102)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(BigDecimal.TEN).
                                setQuantity(new BigDecimal(1000)),
                        10, 11, 12)
        );

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(Page.empty());
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(BigDecimal.ZERO),
                        10, 11, 12)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(1500)).
                                setPackingSize(new BigDecimal(250)).
                                setQuantity(new BigDecimal(1000)),
                        203, 204, 205)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(850));
    }

    @Test
    @DisplayName("""
            getAveragePrice():
             all dish ingredients have suitable products,
             all products cost more than 0
             => return correct result
            """)
    public void getAveragePrice5() {
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(100)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(570)).
                                setPackingSize(BigDecimal.ONE).
                                setQuantity(new BigDecimal(1000)),
                        520, 521, 522)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(200)).
                                setPackingSize(new BigDecimal(2)).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(120)).
                        setQuantity(new BigDecimal(1000)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(500)).
                                setPackingSize(new BigDecimal(200)).
                                setQuantity(new BigDecimal(1000)),
                        11, 12, 13)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(150)).
                                setQuantity(new BigDecimal(1000)),
                        51, 52, 53)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(4525));
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
        User user = user();
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 0))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 1))))).
                thenReturn(3);
        Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter(user, 2))))).
                thenReturn(3);

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 1))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(550)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(6)),
                0, 1, 2)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(0, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(10)).
                                setPackingSize(new BigDecimal("0.5")).
                                setQuantity(new BigDecimal(1000)),
                        20, 21, 22)
        );

        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 0))))
        ).thenReturn(productPage(user,
                (u, i) -> product(u, i).
                        setPrice(new BigDecimal(600)).
                        setPackingSize(new BigDecimal(7)).
                        setQuantity(new BigDecimal(5)),
                12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 1))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(600)).
                                setPackingSize(new BigDecimal(7)).
                                setQuantity(new BigDecimal(5)),
                        12, 13, 14)
        );
        Mockito.when(
                repository.getProducts(Mockito.eq(criteria(100000, filter(user, 2))))
        ).thenReturn(
                productPage(user,
                        (u, i) -> product(u, i).
                                setPrice(new BigDecimal(250)).
                                setPackingSize(new BigDecimal(15)).
                                setQuantity(new BigDecimal(1000)),
                        101, 111, 121)
        );
        Dish dish = dish(1, user, repository).
                addIngredient(ingredient(filter(user, 0), 0)).
                addIngredient(ingredient(filter(user, 1), 1)).
                addIngredient(ingredient(filter(user, 2), 2)).
                tryBuild();

        Optional<BigDecimal> actual = dish.getAveragePrice();

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(1950));
    }


    private User user() {
        return new User.Builder().
                setId(toUUID(1)).
                setName("User").
                setPassword("password").
                setEmail("user@confirmationMail.com").
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

    private Dish.Builder dish(int dishId,
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

    private DishIngredient.Builder ingredient(Filter filter, int ingredientIndex) {
        return new DishIngredient.Builder().
                setId(toUUID(ingredientIndex)).
                setName("some ingredient " + ingredientIndex).
                setFilter(filter).
                setQuantity(BigDecimal.TEN).
                setConfig(conf);
    }
    
    private Criteria criteria(int productIndex, Filter filter) {
        return new Criteria().
                setPageable(PageableByNumber.ofIndex(30, productIndex)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria criteriaById(int productId, Filter filter) {
        return new Criteria().
                setPageable(PageableById.of(30, toUUID(productId))).
                setSort(Sort.products().asc("price")).
                setFilter(filter);
    }

    private Criteria criteriaNumber(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Criteria criteria(Filter filter) {
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

    private Page<Product> productPage(User user,
                                      BiFunction<User, Integer, Product.Builder> productFactory,
                                      int... productIds) {
        Page.Metadata metadata = PageableByNumber.of(30 , 0).
                createPageMetadata(productIds.length, 30);

        List<Product> products = Arrays.stream(productIds).
                mapToObj(i -> productFactory.apply(user, i).tryBuild()).
                toList();

        return metadata.createPage(products);
    }

    private Page<Product> productPageById(User user,
                                          BiFunction<User, Integer, Product.Builder> productFactory,
                                          int searchedId,
                                          int... productIds) {
        PageableById pageable = PageableById.of(30 , toUUID(searchedId));
        int index = IntStream.range(0, productIds.length).
                filter(i -> productIds[i] == searchedId).
                findFirst().
                orElse(-1);
        Page.Metadata metadata = pageable.createPageMetaData(productIds.length,
                index % productIds.length,
                30);

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
                toUUID(ingredientIndex),
                ingredientIndex,
                productIndex
        );
    }

    private Dish.IngredientProduct emptyIngredientProduct(int ingredientIndex,
                                                          int productIndex) {
        return new Dish.IngredientProduct(
                Optional.empty(),
                toUUID(ingredientIndex),
                ingredientIndex,
                productIndex
        );
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.products.ProductCriteria;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.ProductSort;
import com.bakuard.nutritionManager.model.filters.SortDirection;
import com.bakuard.nutritionManager.model.util.Pageable;

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

class DishIngredientTest {

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
            getNecessaryQuantity(servingNumber):
             servingNumber < 0
             => exception
            """)
    public void getNecessaryQuantity1() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(new BigDecimal("-1.7")),
                DishIngredient.class,
                "getNecessaryQuantity",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(servingNumber):
             servingNumber = 0
             => exception
            """)
    public void getNecessaryQuantity2() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(BigDecimal.ZERO),
                DishIngredient.class,
                "getNecessaryQuantity",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(servingNumber):
             servingNumber > 0
             => correct result
            """)
    public void getNecessaryQuantity3() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        BigDecimal actual = ingredient.getNecessaryQuantity(new BigDecimal("1.7"));

        BigDecimal expected = new BigDecimal("17");
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNecessaryQuantity(servingNumber):
             servingNumber is null
             => exception
            """)
    public void getNecessaryQuantity4() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(null),
                DishIngredient.class,
                "getNecessaryQuantity",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantity1() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantity(-1, new BigDecimal("1.5")),
                DishIngredient.class,
                "getProductByIndex",
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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantity(0, BigDecimal.ZERO),
                DishIngredient.class,
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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantity(0, null),
                DishIngredient.class,
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
                        repository.getProducts(Mockito.eq(createCriteria(1, 5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(5, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 5)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(5, BigDecimal.ONE);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(6, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 6)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(6, BigDecimal.ONE);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 16)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 16).
                                createPageMetadata(20, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(16, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 16)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(16, new BigDecimal("2"));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getLackQuantityPrice(productIndex, servingNumber):
             productIndex < 0
             => exception
            """)
    public void getLackQuantityPrice1() {
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantityPrice(-1, new BigDecimal("1.5")),
                DishIngredient.class,
                "getProductByIndex",
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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantityPrice(0, BigDecimal.ZERO),
                DishIngredient.class,
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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getLackQuantityPrice(0, null),
                DishIngredient.class,
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
                        repository.getProducts(Mockito.eq(createCriteria(1, 5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(5, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 5)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(5, BigDecimal.ONE);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(6, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 6)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(6, BigDecimal.ONE);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 16)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 16).
                                createPageMetadata(20, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(16, new BigDecimal("2"));

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 16)))
                ).
                thenReturn(
                        Pageable.firstEmptyPage()
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantityPrice(16, new BigDecimal("2"));

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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(0);

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
                    repository.getProducts(Mockito.eq(createCriteria(1, 4)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 4).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(4);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 5)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 5).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(5);

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
                        repository.getProducts(Mockito.eq(createCriteria(1, 6)))
                ).
                thenReturn(
                        Pageable.ofIndex(5, 6).
                                createPageMetadata(5, 200).
                                createPage(createProducts(1, 5))
                );
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(6);

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
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                Filter.anyCategory("categoryA"),
                BigDecimal.TEN,
                repository,
                createUser(1),
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getProductByIndex(-1),
                DishIngredient.class,
                "getProductByIndex",
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

    private ProductCriteria createCriteria(int userId, int productIndex) {
        return ProductCriteria.of(
                Pageable.ofIndex(5, productIndex),
                createUser(userId)
        ).setProductSort(new ProductSort(ProductSort.Parameter.PRICE, SortDirection.ASCENDING));
    }

    private List<Product> createProducts(int userId, int productNumber) {
        ArrayList<Product> products = new ArrayList<>();

        for(int i = 1; i <= productNumber; i++) {
            products.add(createProduct(userId, i).tryBuild());
        }

        return products;
    }

    public Product.Builder createProduct(int userId, int id) {
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(id)).
                setUser(createUser(userId)).
                setCategory("name " + id).
                setShop("shop " + id).
                setVariety("variety " + id).
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

}
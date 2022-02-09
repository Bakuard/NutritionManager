package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.ProductCriteria;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.filters.CategoryFilter;
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getNecessaryQuantity(new BigDecimal("-1.7")),
                DishIngredient.class,
                "getNecessaryQuantity",
                ConstraintType.NOT_POSITIVE_VALUE
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getNecessaryQuantity(BigDecimal.ZERO),
                DishIngredient.class,
                "getNecessaryQuantity",
                ConstraintType.NOT_POSITIVE_VALUE
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        BigDecimal actual = ingredient.getNecessaryQuantity(new BigDecimal("1.7"));

        BigDecimal expected = new BigDecimal("17");
        Assertions.assertEquals(0, expected.compareTo(actual));
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getNecessaryQuantity(null),
                DishIngredient.class,
                "getNecessaryQuantity",
                ConstraintType.MISSING_VALUE
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getLackQuantity(-1, new BigDecimal("1.5")),
                DishIngredient.class,
                "getLackQuantity",
                ConstraintType.NEGATIVE_VALUE
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getLackQuantity(0, BigDecimal.ZERO),
                DishIngredient.class,
                "getLackQuantity",
                ConstraintType.NOT_POSITIVE_VALUE
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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                Mockito.mock(ProductRepository.class),
                createDefaultUser(1),
                conf
        );

        AssertUtil.assertValidateExcThrows(
                () -> ingredient.getLackQuantity(0, null),
                DishIngredient.class,
                "getLackQuantity",
                ConstraintType.MISSING_VALUE
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

        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                repository,
                createDefaultUser(1),
                conf
        );

        Optional<BigDecimal> actual = ingredient.getLackQuantity(5, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("2800");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(0, expected.compareTo(actual.get()));
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity5() {

    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity6() {

    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex > ingredient products set size,
             there are not products matching this ingredient
             => return empty Optional
            """)
    public void getLackQuantity7() {

    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantity8() {

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
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                repository,
                createDefaultUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(0);

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are products matching this ingredient
             => return correct result
            """)
    public void getProductByIndex2() {
        ProductRepository repository = Mockito.mock(ProductRepository.class);
        Mockito.when(repository.getProducts(Mockito.any())).thenReturn(Pageable.firstEmptyPage());
        DishIngredient ingredient = new DishIngredient(
                "some ingredient",
                CategoryFilter.of("categoryA"),
                BigDecimal.TEN,
                repository,
                createDefaultUser(1),
                conf
        );

        Optional<Product> actual = ingredient.getProductByIndex(4);

        Product expected = defaultProduct(createDefaultUser(5), 5).tryBuild();
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

    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             there are products matching this ingredient,
             productIndex > ingredient products number
             => return correct result
            """)
    public void getProductByIndex4() {

    }

    @Test
    @DisplayName("""
            getProductByIndex(productIndex):
             productIndex < 0
             => exception
            """)
    public void getProductByIndex5() {

    }


    private User createDefaultUser(int id) {
        return new User(toUUID(id),
                "User" + id,
                "password" + id,
                "user" + id + "@mail.com");
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private List<Product> defaultProducts(User user, int productNumber) {
        ArrayList<Product> products = new ArrayList<>();

        for(int i = 1; i <= productNumber; i++) {
            products.add(defaultProduct(user, i).tryBuild());
        }

        return products;
    }

    public Product.Builder defaultProduct(User user, int id) {
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(id)).
                setUser(user).
                setCategory("name " + id).
                setShop("shop " + id).
                setVariety("variety " + id).
                setManufacturer("manufacturer " + id).
                setUnit("unitA").
                setPrice(new BigDecimal((id + 1) * 10)).
                setPackingSize(new BigDecimal("0.5").multiply(BigDecimal.valueOf(id))).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description " + id).
                setImagePath("some image path " + id).
                addTag("tag " + id).
                addTag("common tag");
    }

}
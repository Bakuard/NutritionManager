package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.filters.CategoryFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
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
             servingNumber = 0 (is not positive)
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

        BigDecimal actual = ingredient.getLackQuantity(5, new BigDecimal("2"));

        BigDecimal expected = new BigDecimal("");
        Assertions.assertEquals(0, expected.compareTo(actual));
    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex = ingredient products set size,
             there are not product in DB
             => calculate result for last product
            """)
    public void getLackQuantity5() {

    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex > ingredient products set size
             => calculate result for last product
            """)
    public void getLackQuantity5() {

    }

    @Test
    @DisplayName("""
            getLackQuantity(productIndex, servingNumber):
             productIndex belongs to interval [0, ingredient products set size - 1],
             servingNumber is positive value
             => return correct result
            """)
    public void getLackQuantity6() {

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

}
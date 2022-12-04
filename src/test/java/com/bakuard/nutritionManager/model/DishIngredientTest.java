package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.Constraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

class DishIngredientTest {

    private AppConfigData conf = AppConfigData.builder().
            setNumberPrecision("16").
            setNumberRoundingMod("CEILING").
            setNumberScale("6").
            build();

    @Test
    @DisplayName("""
            getNecessaryQuantity(servingNumber):
             servingNumber < 0
             => exception
            """)
    public void getNecessaryQuantity1() {
        DishIngredient ingredient = new DishIngredient(
                toUUID(0),
                "some ingredient",
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(new BigDecimal("-1.7")),
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
                toUUID(0),
                "some ingredient",
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(BigDecimal.ZERO),
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
                toUUID(0),
                "some ingredient",
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
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
                toUUID(0),
                "some ingredient",
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
                conf
        );

        AssertUtil.assertValidateException(
                () -> ingredient.getNecessaryQuantity(null),
                Constraint.NOT_NULL
        );
    }


    private User createUser(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
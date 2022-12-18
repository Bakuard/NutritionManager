package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.Constraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class DishIngredientTest {

    @Autowired
    private ConfigData conf;

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

        Assertions.assertThat(actual).isEqualByComparingTo(new BigDecimal("17"));
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
                setEmail("user" + userId + "@confirmationMail.com").
                tryBuild();
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
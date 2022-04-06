package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.Pageable;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
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
                Filter.and(
                        Filter.anyCategory("categoryA"),
                        Filter.user(toUUID(1))
                ),
                BigDecimal.TEN,
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
                DishIngredient.class,
                "getNecessaryQuantity",
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
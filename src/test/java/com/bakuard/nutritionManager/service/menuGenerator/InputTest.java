package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.validation.Constraint;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

class InputTest {

    private static AppConfigData conf;

    private ProductRepository productRepository;
    private DishRepository dishRepository;
    private MenuRepository menuRepository;
    private List<Product> products;
    private List<Dish> dishes;
    private List<Menu> menus;
    private User user;

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

    @BeforeEach
    public void beforeEach() {
        user = user(1);

        products = products(user);
        productRepository = mockProductRepository(user, products);

        dishes = dishes(user, productRepository);
        dishRepository = mockDishRepository(user, dishes);

        menus = menus(user, dishes);
        menuRepository = mockMenuRepository(user, menus);
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             user is null
             => exception
            """)
    public void buildInput1() {
        Input.Builder builder = new Input.Builder().
                setUser(null).
                setGeneratedMenuName("New menu #1").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.user[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             dishRepository is null
             => exception
            """)
    public void buildInput2() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("New menu #1").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(null).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.dishRepository[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             menuRepository is null
             => exception
            """)
    public void buildInput3() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("New menu #1").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(null);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.menuRepository[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             generatedMenuName is null
             => exception
            """)
    public void buildInput4() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName(null).
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.generatedMenuName[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             menu with generatedMenuName already exists
             => exception
            """)
    public void buildInput5() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("???????? ?????? ???????????? ??????????-2022-01-22-??16").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.generatedMenuName[IS_TRUE]",
                Constraint.IS_TRUE
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             maxPrice is null
             => exception
            """)
    public void buildInput6() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(null).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.maxPrice[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             maxPrice < 0
             => exception
            """)
    public void buildInput7() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(-1)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.maxPrice[NOT_NEGATIVE_VALUE]",
                Constraint.NOT_NEGATIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             minMealsNumber not positive
             => exception
            """)
    public void buildInput8() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(0).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.minMeals[POSITIVE_VALUE]",
                Constraint.POSITIVE_VALUE
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             servingNumberPerMeal is null
             => exception
            """)
    public void buildInput9() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(null).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.servingNumberPerMeal[NOT_NULL]",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has null category
             => exception
            """)
    public void buildInput10() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint(null, "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.category[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has a non-existed category
             => exception
            """)
    public void buildInput11() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("UNKNOWN CATEGORY", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.category[ANY_MATCH]", Constraint.ANY_MATCH),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has null condition
             => exception
            """)
    public void buildInput12() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", null, BigDecimal.ONE).
                addProductConstraint("??????????????", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.condition[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has a non-existed condition
             => exception
            """)
    public void buildInput13() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "UNKNOWN RELATION", BigDecimal.ONE).
                addProductConstraint("??????????????", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.condition[ANY_MATCH]", Constraint.ANY_MATCH),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has null quantity
             => exception
            """)
    public void buildInput14() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", null).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.quantity[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of product constraint has negative quantity
             => exception
            """)
    public void buildInput15() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", new BigDecimal(-1)).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.product.quantity[NOT_NEGATIVE_VALUE]", Constraint.NOT_NEGATIVE_VALUE),
                new Pair<>("Input.products[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish constraint has null tag
             => exception
            """)
    public void buildInput16() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint(null, "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.dishTag[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish constraint has a non-existed tag
             => exception
            """)
    public void buildInput17() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("UNKNOWN TAG", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.dishTag[ANY_MATCH]", Constraint.ANY_MATCH),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish constraint has null condition
             => exception
            """)
    public void buildInput18() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", null, BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.condition[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish constraint has a non-existed condition
             => exception
            """)
    public void buildInput19() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "UNKNOWN RELATION", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.condition[ANY_MATCH]", Constraint.ANY_MATCH),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish quantity has null quantity
             => exception
            """)
    public void buildInput20() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", null).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.quantity[NOT_NULL]", Constraint.NOT_NULL),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             one of dish quantity has negative quantity
             => exception
            """)
    public void buildInput21() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(-1)).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                new Pair<>("Input.dish.quantity[NOT_NEGATIVE_VALUE]", Constraint.NOT_NEGATIVE_VALUE),
                new Pair<>("Input.dishes[DOES_NOT_THROW]", Constraint.DOES_NOT_THROW)
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             user haven't any dishes
             => exception
            """)
    public void buildInput22() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(mockDishRepository(user, List.of())).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.allUserDishes[NOT_EMPTY_COLLECTION]", Constraint.NOT_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             all user dishes haven't any suitable products
             => exception
            """)
    public void buildInput23() {
        productRepository = mockProductRepository(user, List.of());
        List<Dish> dishes = dishes(user, productRepository);
        dishRepository = mockDishRepository(user, dishes);
        List<Menu> menus = menus(user, dishes);
        menuRepository = mockMenuRepository(user, menus);

        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.allUserDishes[NOT_EMPTY_COLLECTION]", Constraint.NOT_EMPTY_COLLECTION
        );
    }

    @Test
    @DisplayName("""
            Input.Builder.build():
             generatedMenuName is blank
             => exception
            """)
    public void buildInput24() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("     ").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::tryBuild,
                "Input.generatedMenuName[NOT_BLANK]",
                Constraint.NOT_BLANK
        );
    }

    @Test
    @DisplayName("""
            getMinServingNumber():
             => return correct result
            """)
    public void getMinServingNumber1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        AssertUtil.assertEquals(new BigDecimal(30), input.getMinServingNumber());
    }

    @Test
    @DisplayName("""
            hasTag(dish, tag):
             dish doesn't contain tag
             => return false
            """)
    public void hasTag1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertFalse(input.hasTag(dishes.get(0), new Tag("??????????????")));
    }

    @Test
    @DisplayName("""
            hasTag(dish, tag):
             dish contains tag
             => return true
            """)
    public void hasTag2() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertTrue(input.hasTag(dishes.get(0), new Tag("????????????????")));
    }

    @Test
    @DisplayName("""
            getQuantity(dish, productCategory):
             productCategory used for dish
             => return correct result (more then 0)
            """)
    public void getQuantity1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertAll(
                () -> AssertUtil.assertEquals(new BigDecimal("0.03"),
                        input.getQuantity(dishes.get(0), "????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.5"),
                        input.getQuantity(dishes.get(0), "??????????????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.2"),
                        input.getQuantity(dishes.get(0), "???????????????????????? ??????????")),

                () -> AssertUtil.assertEquals(new BigDecimal("2"),
                        input.getQuantity(dishes.get(1), "????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.1"),
                        input.getQuantity(dishes.get(1), "??????????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.35"),
                        input.getQuantity(dishes.get(1), "???????????????????????? ??????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("4"),
                        input.getQuantity(dishes.get(1), "??????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.06"),
                        input.getQuantity(dishes.get(1), "????????")),

                () -> AssertUtil.assertEquals(new BigDecimal("0.03"),
                        input.getQuantity(dishes.get(2), "????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.1"),
                        input.getQuantity(dishes.get(2), "??????????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("0.1"),
                        input.getQuantity(dishes.get(2), "???????????????????????? ??????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("4"),
                        input.getQuantity(dishes.get(2), "??????")),
                () -> AssertUtil.assertEquals(new BigDecimal("1"),
                        input.getQuantity(dishes.get(2), "????????")),
                () -> AssertUtil.assertEquals(new BigDecimal("1"),
                        input.getQuantity(dishes.get(2), "??????????"))
        );
    }

    @Test
    @DisplayName("""
            getQuantity(dish, productCategory):
             productCategory isn't used for dish
             => return 0
            """)
    public void getQuantity2() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertAll(
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(0), "????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(0), "??????????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(0), "??????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(0), "????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(0), "??????????")),

                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(1), "??????????????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(1), "????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(1), "??????????")),

                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(2), "??????????????????")),
                () -> AssertUtil.assertEquals(BigDecimal.ZERO,
                        input.getQuantity(dishes.get(2), "????????"))
        );
    }

    @Test
    @DisplayName("""
            getConstraintsByAllDishTags():
             all tags have constraints
             => return correct result
            """)
    public void getConstraintsByAllDishTags1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("??????????????", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("??????", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        List<Input.DishTagConstraint> expected = List.of(
                tagConstraint("????????????????", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                tagConstraint("??????????????", Relationship.GREATER_OR_EQUAL, new BigDecimal(5)),
                tagConstraint("??????", Relationship.GREATER_OR_EQUAL, new BigDecimal(8))
        );
        Assertions.assertEquals(expected, input.getConstraintsByAllDishTags());
    }

    @Test
    @DisplayName("""
            getConstraintsByAllDishTags():
             some tags haven't constraints
             => return correct result
            """)
    public void getConstraintsByAllDishTags2() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(2)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        List<Input.DishTagConstraint> expected = List.of(
                tagConstraint("????????????????", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                tagConstraint("??????????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                tagConstraint("??????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO)
        );
        Assertions.assertEquals(expected, input.getConstraintsByAllDishTags());
    }

    @Test
    @DisplayName("""
            getConstraintsByAllProducts():
             all product categories have constraints
             => return correct result
            """)
    public void getConstraintsByAllProducts1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("??????????????", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("??????", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Set<Input.ProductConstraint> expected = Set.of(
                productConstraint("????????", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                productConstraint("??????????????????", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                productConstraint("???????????????????????? ??????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ONE),
                productConstraint("??????????????", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                productConstraint("??????", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                productConstraint("????????", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                productConstraint("??????????", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                productConstraint("????????", Relationship.GREATER_OR_EQUAL,new BigDecimal(3))
        );
        Assertions.assertEquals(expected, new HashSet<>(input.getConstraintsByAllProducts()));
    }

    @Test
    @DisplayName("""
            getConstraintsByAllProducts():
            some product categories haven't constraints
             => return correct result
            """)
    public void getConstraintsByAllProducts2() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("??????????????", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("??????", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Set<Input.ProductConstraint> expected = Set.of(
                productConstraint("????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                productConstraint("??????????????????", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                productConstraint("???????????????????????? ??????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ONE),
                productConstraint("??????????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                productConstraint("??????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                productConstraint("????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                productConstraint("??????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                productConstraint("????????", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO)
        );
        Assertions.assertEquals(expected, new HashSet<>(input.getConstraintsByAllProducts()));
    }

    @Test
    @DisplayName("""
            getAllDishMinPrices():
             => return correct result
            """)
    public void getAllDishMinPrices1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("??????????????", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("??????", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Set<Input.DishMinPrice> expected = Set.of(
                dishMinPrice(dishes.get(0), new BigDecimal("227")),
                dishMinPrice(dishes.get(1), new BigDecimal("444")),
                dishMinPrice(dishes.get(2), new BigDecimal("748"))
        );
        Assertions.assertEquals(expected, new HashSet<>(input.getAllDishMinPrices()));
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private User user(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
    }

    private List<Product> products(User user) {
        List<Product> result = new ArrayList<>();

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(1)).
                        setUser(user).
                        setCategory("????????").
                        setShop("Globus").
                        setGrade("????????????????????????").
                        setManufacturer("???????? ??????????????").
                        setPrice(new BigDecimal(75)).
                        setPackingSize(new BigDecimal("0.5")).
                        setUnit("????").
                        setQuantity(BigDecimal.ZERO).
                        addTag("????????????").
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(2)).
                        setUser(user).
                        setCategory("??????????????????").
                        setShop("Globus").
                        setGrade("??????????????").
                        setManufacturer("???????? ????????????????").
                        setPrice(new BigDecimal(40)).
                        setPackingSize(new BigDecimal(1)).
                        setUnit("????").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(3)).
                        setUser(user).
                        setCategory("???????????????????????? ??????????").
                        setShop("Globus").
                        setGrade("????????????????????????").
                        setManufacturer("???????? ??????????????").
                        setPrice(new BigDecimal(112)).
                        setPackingSize(new BigDecimal(1)).
                        setUnit("????????").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(4)).
                        setUser(user).
                        setCategory("??????????????").
                        setShop("Globus").
                        setGrade("????????????????????").
                        setManufacturer("???????? ????????").
                        setPrice(new BigDecimal(25)).
                        setPackingSize(new BigDecimal("0.1")).
                        setUnit("????").
                        setQuantity(new BigDecimal(5)).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(5)).
                        setUser(user).
                        setCategory("??????").
                        setShop("????????????????").
                        setGrade("????????????????").
                        setManufacturer("???????? ????????????????????").
                        setPrice(new BigDecimal(43)).
                        setPackingSize(BigDecimal.ONE).
                        setUnit("????").
                        setQuantity(new BigDecimal("1.5")).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(6)).
                        setUser(user).
                        setCategory("????????").
                        setShop("Globus").
                        setGrade("??????????").
                        setManufacturer("???????? ??????").
                        setPrice(new BigDecimal(24)).
                        setPackingSize(BigDecimal.ONE).
                        setUnit("??????????").
                        setQuantity(new BigDecimal("1.5")).
                        addTag("????????????????").
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(7)).
                        setUser(user).
                        setCategory("??????????").
                        setShop("????????????????").
                        setGrade("??????????????????").
                        setManufacturer("???????? ????????").
                        setPrice(new BigDecimal(85)).
                        setPackingSize(new BigDecimal("0.25")).
                        setUnit("????").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(8)).
                        setUser(user).
                        setCategory("????????").
                        setShop("????????????????").
                        setGrade("????????????????").
                        setManufacturer("???????? ????????????????????").
                        setPrice(new BigDecimal(60)).
                        setPackingSize(BigDecimal.TEN).
                        setUnit("????").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        return result;
    }

    private List<Dish> dishes(User user, ProductRepository productRepository) {
        List<Dish> result = new ArrayList<>();

        result.add(
                new Dish.Builder().
                        setId(toUUID(1)).
                        setUser(user).
                        setName("???????????????? ????????????????").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("????").
                        setDescription("?????????????? ?? ?????????????? ?? ?????????????????????????? ??????????").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("????????????????").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1001)).
                                        setName("ingredient 1001").
                                        setQuantity(new BigDecimal("0.03")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1002)).
                                        setName("ingredient 1002").
                                        setQuantity(new BigDecimal("0.5")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????????????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1003)).
                                        setName("ingredient 1003").
                                        setQuantity(new BigDecimal("0.2")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("???????????????????????? ??????????")
                                                )
                                        )
                        ).
                        tryBuild()
        );

        result.add(
                new Dish.Builder().
                        setId(toUUID(2)).
                        setUser(user).
                        setName("?????????????? ????????????").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("????").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("????????????????").
                        addTag("??????????????").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1004)).
                                        setName("ingredient 1004").
                                        setQuantity(new BigDecimal(2)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1005)).
                                        setName("ingredient 1005").
                                        setQuantity(new BigDecimal("0.1")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1006)).
                                        setName("ingredient 1006").
                                        setQuantity(new BigDecimal("0.35")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("???????????????????????? ??????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1007)).
                                        setName("ingredient 1007").
                                        setQuantity(new BigDecimal(4)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????"),
                                                        Filter.anyGrade("????????????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1008)).
                                        setName("ingredient 1008").
                                        setQuantity(new BigDecimal("0.06")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("????????")
                                                )
                                        )
                        ).
                        tryBuild()
        );

        result.add(
                new Dish.Builder().
                        setId(toUUID(3)).
                        setUser(user).
                        setName("?????????????? ??????").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("????????").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("??????").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1009)).
                                        setName("ingredient 1009").
                                        setQuantity(new BigDecimal("0.03")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1010)).
                                        setName("ingredient 1010").
                                        setQuantity(new BigDecimal("0.1")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1011)).
                                        setName("ingredient 1011").
                                        setQuantity(new BigDecimal("0.1")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("???????????????????????? ??????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1012)).
                                        setName("ingredient 1012").
                                        setQuantity(new BigDecimal(4)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????"),
                                                        Filter.anyGrade("????????????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1013)).
                                        setName("ingredient 1013").
                                        setQuantity(new BigDecimal(1)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("????????"),
                                                        Filter.anyGrade("??????????")
                                                )
                                        )
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1014)).
                                        setName("ingredient 1014").
                                        setQuantity(new BigDecimal(1)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("??????????"),
                                                        Filter.anyGrade("??????????????????")
                                                )
                                        )
                        ).
                        tryBuild()
        );

        return result;
    }

    private List<Menu> menus(User user, List<Dish> dishes) {
        List<Menu> result = new ArrayList<>();

        result.add(
                new Menu.Builder().
                        setId(toUUID(1)).
                        setUser(user).
                        setName("???????? ?????? ???????????? ??????????-2022-01-22-??16").
                        setConfig(conf).
                        addItem(createMenuItem(dishes.get(0), BigDecimal.ONE, 100001)).
                        addItem(createMenuItem(dishes.get(2), new BigDecimal(2), 100002)).
                        tryBuild()
        );

        return result;
    }

    private MenuItem.LoadBuilder createMenuItem(Dish dish, BigDecimal quantity, int itemId) {
        return new MenuItem.LoadBuilder().
                setId(toUUID(itemId)).
                setConfig(conf).
                setDish(dish).
                setQuantity(quantity);
    }

    private ProductRepository mockProductRepository(User user, List<Product> products) {
        ProductRepository repository = Mockito.mock(ProductRepository.class);

        List<Filter> filters = List.of(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("??????????????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("???????????????????????? ??????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("??????????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("??????"),
                        Filter.anyGrade("????????????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("????????"),
                        Filter.anyGrade("??????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("??????????"),
                        Filter.anyGrade("??????????????????")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("????????")
                )
        );

        for(int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            if(!products.isEmpty()) {
                Product product = products.get(i);
                Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter)))).
                        thenReturn(1);
                Mockito.when(repository.getProducts(Mockito.eq(productCriteria(filter)))).
                        thenReturn(productPage(product));
            } else {
                Mockito.when(repository.getProductsNumber(Mockito.eq(criteriaNumber(filter)))).
                        thenReturn(0);
                Mockito.when(repository.getProducts(Mockito.eq(productCriteria(filter)))).
                        thenReturn(Page.empty());
            }
        }

        return repository;
    }

    private DishRepository mockDishRepository(User user, List<Dish> dishes) {
        DishRepository repository = Mockito.mock(DishRepository.class);
        Filter filter = Filter.user(user.getId());

        Mockito.when(repository.getDishesNumber(criteriaNumber(filter))).
                thenReturn(dishes.size());
        Mockito.when(repository.getDishes(dishCriteria(filter))).
                thenReturn(dishPage(dishes.toArray(length -> new Dish[length])));

        return repository;
    }

    private MenuRepository mockMenuRepository(User user, List<Menu> menus) {
        MenuRepository repository = Mockito.mock(MenuRepository.class);

        for(Menu menu : menus) {
            Filter filter = Filter.and(
                    Filter.user(user.getId()),
                    Filter.anyMenu(menu.getName())
            );

            Mockito.when(repository.getMenusNumber(criteriaNumber(filter))).
                    thenReturn(1);
        }

        return repository;
    }

    private Page<Product> productPage(Product... products) {
        Page.Metadata metadata = PageableByNumber.of(30 , 0).
                createPageMetadata(products.length, 30);

        List<Product> resultProducts = Arrays.asList(products);

        return metadata.createPage(resultProducts);
    }

    private Page<Dish> dishPage(Dish... dishes) {
        Page.Metadata metadata = PageableByNumber.of(30 , 0).
                createPageMetadata(dishes.length, 30);

        List<Dish> resultDishes = Arrays.asList(dishes);

        return metadata.createPage(resultDishes);
    }

    private Criteria criteriaNumber(Filter filter) {
        return new Criteria().setFilter(filter);
    }

    private Criteria productCriteria(Filter filter) {
        return new Criteria().
                setPageable(PageableByNumber.of(30, 0)).
                setFilter(filter).
                setSort(Sort.products().asc("price"));
    }

    private Criteria dishCriteria(Filter filter) {
        return new Criteria().
                setPageable(PageableByNumber.of(30, 0)).
                setFilter(filter).
                setSort(Sort.dishDefaultSort());
    }

    private Input.DishTagConstraint tagConstraint(String dishTag, Relationship relation, BigDecimal quantity) {
        return new Input.DishTagConstraint(new Tag(dishTag), relation, quantity);
    }

    private Input.ProductConstraint productConstraint(String productCategory, Relationship relation, BigDecimal quantity) {
        return new Input.ProductConstraint(productCategory, relation, quantity);
    }

    private Input.DishMinPrice dishMinPrice(Dish dish, BigDecimal minPrice) {
        return new Input.DishMinPrice(dish, minPrice.setScale(conf.getNumberScale(), conf.getRoundingMode()));
    }

}
package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.validation.Constraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class InputTest {

    @Autowired
    private ConfigData conf;
    private ProductRepository productRepository;
    private DishRepository dishRepository;
    private MenuRepository menuRepository;
    private List<Product> products;
    private List<Dish> dishes;
    private List<Menu> menus;
    private User user;

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
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
                setGeneratedMenuName("Обед для группы Ленин-2022-01-22-А16").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
             minMealsNumber not positive
             => exception
            """)
    public void buildInput6() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(0).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput7() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(null).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput8() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint(null, "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput9() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("UNKNOWN CATEGORY", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput10() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", null, BigDecimal.ONE).
                addProductConstraint("Крахмал", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput11() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "UNKNOWN RELATION", BigDecimal.ONE).
                addProductConstraint("Крахмал", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput12() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", null).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput13() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", new BigDecimal(-1)).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput14() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint(null, "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput15() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("UNKNOWN TAG", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput16() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", null, BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput17() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "UNKNOWN RELATION", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput18() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", null).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput19() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(-1)).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput20() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput21() {
        productRepository = mockProductRepository(user, List.of());
        List<Dish> dishes = dishes(user, productRepository);
        dishRepository = mockDishRepository(user, dishes);
        List<Menu> menus = menus(user, dishes);
        menuRepository = mockMenuRepository(user, menus);

        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
    public void buildInput22() {
        Input.Builder builder = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("     ").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getMinServingNumber()).isEqualByComparingTo(new BigDecimal(30));
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.hasTag(dishes.get(0), new Tag("закуска"))).isFalse();
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.hasTag(dishes.get(0), new Tag("жаренное"))).isTrue();
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(List.of(
                        input.getQuantity(dishes.get(0), "соль"),
                        input.getQuantity(dishes.get(0), "Картофель"),
                        input.getQuantity(dishes.get(0), "Растительное масло"),

                        input.getQuantity(dishes.get(1), "Яйца"),
                        input.getQuantity(dishes.get(1), "Крахмал"),
                        input.getQuantity(dishes.get(1), "Растительное масло"),
                        input.getQuantity(dishes.get(1), "Лук"),
                        input.getQuantity(dishes.get(1), "соль"),

                        input.getQuantity(dishes.get(2), "соль"),
                        input.getQuantity(dishes.get(2), "Крахмал"),
                        input.getQuantity(dishes.get(2), "Растительное масло"),
                        input.getQuantity(dishes.get(2), "Лук"),
                        input.getQuantity(dishes.get(2), "Хлеб"),
                        input.getQuantity(dishes.get(2), "Масло")
                )).
                usingElementComparator(Comparator.naturalOrder()).
                containsExactly(
                        new BigDecimal("0.03"),
                        new BigDecimal("0.5"),
                        new BigDecimal("0.2"),

                        new BigDecimal("2"),
                        new BigDecimal("0.1"),
                        new BigDecimal("0.35"),
                        new BigDecimal("4"),
                        new BigDecimal("0.06"),

                        new BigDecimal("0.03"),
                        new BigDecimal("0.1"),
                        new BigDecimal("0.1"),
                        new BigDecimal("4"),
                        new BigDecimal("1"),
                        new BigDecimal("1")
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("закуска", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("суп", "greaterOrEqual", BigDecimal.ZERO).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(List.of(
                        input.getQuantity(dishes.get(0), "Яйца"),
                        input.getQuantity(dishes.get(0), "Крахмал"),
                        input.getQuantity(dishes.get(0), "Лук"),
                        input.getQuantity(dishes.get(0), "Хлеб"),
                        input.getQuantity(dishes.get(0), "Масло"),

                        input.getQuantity(dishes.get(1), "Картофель"),
                        input.getQuantity(dishes.get(1), "Хлеб"),
                        input.getQuantity(dishes.get(1), "Масло"),

                        input.getQuantity(dishes.get(2), "Картофель"),
                        input.getQuantity(dishes.get(2), "Яйца")
                )).
                usingElementComparator(Comparator.naturalOrder()).
                containsExactly(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,

                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,

                        BigDecimal.ZERO,
                        BigDecimal.ZERO
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("закуска", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("суп", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getConstraintsByAllDishTags()).
                containsExactly(
                        tagConstraint("жаренное", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                        tagConstraint("закуска", Relationship.GREATER_OR_EQUAL, new BigDecimal(5)),
                        tagConstraint("суп", Relationship.GREATER_OR_EQUAL, new BigDecimal(8))
                );
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(2)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getConstraintsByAllDishTags()).
                containsExactly(
                        tagConstraint("жаренное", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                        tagConstraint("закуска", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        tagConstraint("суп", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO)
                );
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("закуска", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("суп", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getConstraintsByAllProducts()).
                containsExactlyInAnyOrder(
                        productConstraint("соль", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                        productConstraint("Картофель", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                        productConstraint("Растительное масло", Relationship.GREATER_OR_EQUAL, BigDecimal.ONE),
                        productConstraint("Крахмал", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                        productConstraint("Лук", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                        productConstraint("Хлеб", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                        productConstraint("Масло", Relationship.GREATER_OR_EQUAL, BigDecimal.TEN),
                        productConstraint("Яйца", Relationship.GREATER_OR_EQUAL,new BigDecimal(3))
                );
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
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("закуска", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("суп", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getConstraintsByAllProducts()).
                containsExactlyInAnyOrder(
                        productConstraint("соль", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        productConstraint("Картофель", Relationship.GREATER_OR_EQUAL, new BigDecimal(2)),
                        productConstraint("Растительное масло", Relationship.GREATER_OR_EQUAL, BigDecimal.ONE),
                        productConstraint("Крахмал", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        productConstraint("Лук", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        productConstraint("Хлеб", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        productConstraint("Масло", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO),
                        productConstraint("Яйца", Relationship.GREATER_OR_EQUAL, BigDecimal.ZERO)
                );
    }

    @Test
    @DisplayName("""
            getAllDishMinPrices():
             => return correct result
            """)
    public void getAllDishMinPrices1() {
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("Новое меню").
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("соль", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Картофель", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Растительное масло", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("Крахмал", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Лук", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("Хлеб", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Масло", "greaterOrEqual", BigDecimal.TEN).
                addProductConstraint("Яйца", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("жаренное", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("закуска", "greaterOrEqual", new BigDecimal(5)).
                addDishConstraint("суп", "greaterOrEqual", new BigDecimal(8)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Assertions.assertThat(input.getAllDishMinPrices()).
                containsExactly(
                        dishMinPrice(dishes.get(0), new BigDecimal("227")),
                        dishMinPrice(dishes.get(1), new BigDecimal("444")),
                        dishMinPrice(dishes.get(2), new BigDecimal("748"))
                );
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private User user(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@confirmationMail.com").
                tryBuild();
    }

    private List<Product> products(User user) {
        List<Product> result = new ArrayList<>();

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(1)).
                        setUser(user).
                        setCategory("соль").
                        setShop("Globus").
                        setGrade("йодированная").
                        setManufacturer("ОсОО Морсоль").
                        setPrice(new BigDecimal(75)).
                        setPackingSize(new BigDecimal("0.5")).
                        setUnit("кг").
                        setQuantity(BigDecimal.ZERO).
                        addTag("мелкая").
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(2)).
                        setUser(user).
                        setCategory("Картофель").
                        setShop("Globus").
                        setGrade("молодой").
                        setManufacturer("ОсОО Беларусь").
                        setPrice(new BigDecimal(40)).
                        setPackingSize(new BigDecimal(1)).
                        setUnit("кг").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(3)).
                        setUser(user).
                        setCategory("Растительное масло").
                        setShop("Globus").
                        setGrade("подсолнечное").
                        setManufacturer("ОсОО Тайвань").
                        setPrice(new BigDecimal(112)).
                        setPackingSize(new BigDecimal(1)).
                        setUnit("литр").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(4)).
                        setUser(user).
                        setCategory("Крахмал").
                        setShop("Globus").
                        setGrade("кукурузный").
                        setManufacturer("ОсОО Риха").
                        setPrice(new BigDecimal(25)).
                        setPackingSize(new BigDecimal("0.1")).
                        setUnit("кг").
                        setQuantity(new BigDecimal(5)).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(5)).
                        setUser(user).
                        setCategory("Лук").
                        setShop("Народный").
                        setGrade("репчатый").
                        setManufacturer("ОсОО Зеленоград").
                        setPrice(new BigDecimal(43)).
                        setPackingSize(BigDecimal.ONE).
                        setUnit("кг").
                        setQuantity(new BigDecimal("1.5")).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(6)).
                        setUser(user).
                        setCategory("Хлеб").
                        setShop("Globus").
                        setGrade("белый").
                        setManufacturer("ОсОО Нан").
                        setPrice(new BigDecimal(24)).
                        setPackingSize(BigDecimal.ONE).
                        setUnit("булка").
                        setQuantity(new BigDecimal("1.5")).
                        addTag("кирпичек").
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(7)).
                        setUser(user).
                        setCategory("Масло").
                        setShop("Народный").
                        setGrade("сливочное").
                        setManufacturer("ОсОО Риха").
                        setPrice(new BigDecimal(85)).
                        setPackingSize(new BigDecimal("0.25")).
                        setUnit("кг").
                        setQuantity(BigDecimal.ZERO).
                        tryBuild()
        );

        result.add(
                new Product.Builder().
                        setAppConfiguration(conf).
                        setId(toUUID(8)).
                        setUser(user).
                        setCategory("Яйца").
                        setShop("Народный").
                        setGrade("домашние").
                        setManufacturer("ОсОО Птицеферма").
                        setPrice(new BigDecimal(60)).
                        setPackingSize(BigDecimal.TEN).
                        setUnit("шт").
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
                        setName("Картошка жаренная").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("кг").
                        setDescription("простое и быстрое в приготволении блюдо").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("жаренное").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1001)).
                                        setName("ingredient 1001").
                                        setQuantity(new BigDecimal("0.03")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("соль")
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
                                                        Filter.anyCategory("Картофель")
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
                                                        Filter.anyCategory("Растительное масло")
                                                )
                                        )
                        ).
                        tryBuild()
        );

        result.add(
                new Dish.Builder().
                        setId(toUUID(2)).
                        setUser(user).
                        setName("Луковые кольца").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("кг").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("жаренное").
                        addTag("закуска").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1004)).
                                        setName("ingredient 1004").
                                        setQuantity(new BigDecimal(2)).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("Яйца")
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
                                                        Filter.anyCategory("Крахмал")
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
                                                        Filter.anyCategory("Растительное масло")
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
                                                        Filter.anyCategory("Лук"),
                                                        Filter.anyGrade("репчатый")
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
                                                        Filter.anyCategory("соль")
                                                )
                                        )
                        ).
                        tryBuild()
        );

        result.add(
                new Dish.Builder().
                        setId(toUUID(3)).
                        setUser(user).
                        setName("Луковый суп").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("литр").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("суп").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1009)).
                                        setName("ingredient 1009").
                                        setQuantity(new BigDecimal("0.03")).
                                        setConfig(conf).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.anyCategory("соль")
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
                                                        Filter.anyCategory("Крахмал")
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
                                                        Filter.anyCategory("Растительное масло")
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
                                                        Filter.anyCategory("Лук"),
                                                        Filter.anyGrade("репчатый")
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
                                                        Filter.anyCategory("Хлеб"),
                                                        Filter.anyGrade("белый")
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
                                                        Filter.anyCategory("Масло"),
                                                        Filter.anyGrade("сливочное")
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
                        setName("Обед для группы Ленин-2022-01-22-А16").
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
                        Filter.anyCategory("соль")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Картофель")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Растительное масло")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Крахмал")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Лук"),
                        Filter.anyGrade("репчатый")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Хлеб"),
                        Filter.anyGrade("белый")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Масло"),
                        Filter.anyGrade("сливочное")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyCategory("Яйца")
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
        return new Input.DishMinPrice(dish, minPrice.setScale(conf.decimal().numberScale(), conf.decimal().roundingMode()));
    }

}
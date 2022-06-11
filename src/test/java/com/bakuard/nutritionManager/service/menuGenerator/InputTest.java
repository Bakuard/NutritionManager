package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.Constraint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class InputTest {

    private static AppConfigData conf;

    private ProductRepository productRepository;
    private DishRepository dishRepository;
    private MenuRepository menuRepository;

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
        User user = user(1);

        List<Product> products = products(user);
        productRepository = Mockito.mock(ProductRepository.class);

        List<Dish> dishes = dishes(user, productRepository);
        dishRepository = Mockito.mock(DishRepository.class);

        List<Menu> menus = menus(user, dishes);
        menuRepository = Mockito.mock(MenuRepository.class);
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
                addProductConstraint("potato", "greaterOrEqual", new BigDecimal(5)).
                addProductConstraint("oil", "lessOrEqual", new BigDecimal("0.5")).
                addProductConstraint("onion", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("fried", "lessOrEqual", BigDecimal.ONE).
                addDishConstraint("soup", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("hearty", "greaterOrEqual", new BigDecimal(3)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::build,
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
                setUser(user(1)).
                setGeneratedMenuName("New menu #1").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("potato", "greaterOrEqual", new BigDecimal(5)).
                addProductConstraint("oil", "lessOrEqual", new BigDecimal("0.5")).
                addProductConstraint("onion", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("fried", "lessOrEqual", BigDecimal.ONE).
                addDishConstraint("soup", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("hearty", "greaterOrEqual", new BigDecimal(3)).
                setDishRepository(null).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::build,
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
                setUser(user(1)).
                setGeneratedMenuName("New menu #1").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("potato", "greaterOrEqual", new BigDecimal(5)).
                addProductConstraint("oil", "lessOrEqual", new BigDecimal("0.5")).
                addProductConstraint("onion", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("fried", "lessOrEqual", BigDecimal.ONE).
                addDishConstraint("soup", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("hearty", "greaterOrEqual", new BigDecimal(3)).
                setDishRepository(dishRepository).
                setMenuRepository(null);

        AssertUtil.assertValidateException(
                builder::build,
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
                setUser(user(1)).
                setGeneratedMenuName(null).
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(10).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("potato", "greaterOrEqual", new BigDecimal(5)).
                addProductConstraint("oil", "lessOrEqual", new BigDecimal("0.5")).
                addProductConstraint("onion", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("fried", "lessOrEqual", BigDecimal.ONE).
                addDishConstraint("soup", "greaterOrEqual", new BigDecimal(2)).
                addDishConstraint("hearty", "greaterOrEqual", new BigDecimal(3)).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository);

        AssertUtil.assertValidateException(
                builder::build,
                "Input.generatedMenuName[NOT_NULL]",
                Constraint.NOT_NULL
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
                        addTag("быстро готовится").
                        addTag("не дорогое").
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
                        setId(toUUID(2)).
                        setUser(user).
                        setName("Луковый суп").
                        setServingSize(new BigDecimal("0.5")).
                        setUnit("литр").
                        setConfig(conf).
                        setRepository(productRepository).
                        addTag("быстро готовится").
                        addTag("не дорогое").
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

}
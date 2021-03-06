package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class MenuGeneratorServiceTest {

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
            generate(input):
             input is null
             => exception
            """)
    public void generate1() {
        MenuGeneratorService service = new MenuGeneratorService(conf);

        AssertUtil.assertValidateException(
                () -> service.generate(null),
                "MenuGeneratorService.input[NOT_NULL]", Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            generate(input):
             input is not null,
             solution exists
             => return correct result
            """)
    public void generate2() {
        MenuGeneratorService service = new MenuGeneratorService(conf);
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(2).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "lessOrEqual", new BigDecimal(4)).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("????????", "greaterOrEqual", new BigDecimal(3)).
                addDishConstraint("????????????????", "lessOrEqual", BigDecimal.TEN).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ONE).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Menu actual = service.generate(input);

        Assertions.assertAll(
                () -> Assertions.assertEquals("?????????? ????????", actual.getName()),
                () -> AssertUtil.assertEquals(dishes.get(0),
                        actual.tryGetItem("???????????????? ????????????????").getDish()),
                () -> AssertUtil.assertEquals(dishes.get(1),
                        actual.tryGetItem("?????????????? ????????????").getDish()),
                () -> AssertUtil.assertEquals(dishes.get(2),
                        actual.tryGetItem("?????????????? ??????").getDish()),
                () -> AssertUtil.assertEquals(new BigDecimal(4),
                        actual.tryGetItem("???????????????? ????????????????").getNecessaryQuantity(BigDecimal.ONE)),
                () -> AssertUtil.assertEquals(new BigDecimal(2),
                        actual.tryGetItem("?????????????? ????????????").getNecessaryQuantity(BigDecimal.ONE)),
                () -> AssertUtil.assertEquals(new BigDecimal(1),
                        actual.tryGetItem("?????????????? ??????").getNecessaryQuantity(BigDecimal.ONE))
        );
    }

    @Test
    @DisplayName("""
            generate(input):
             input is not null,
             solution exists,
             optimal solution doesn't include some dishes
             => return correct result (without not included dishes)
            """)
    public void generate3() {
        MenuGeneratorService service = new MenuGeneratorService(conf);
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(2).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "lessOrEqual", new BigDecimal(4)).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("????????????????", "lessOrEqual", BigDecimal.TEN).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ONE).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        Menu actual = service.generate(input);

        Assertions.assertAll(
                () -> Assertions.assertEquals("?????????? ????????", actual.getName()),
                () -> AssertUtil.assertEquals(dishes.get(0),
                        actual.tryGetItem("???????????????? ????????????????").getDish()),
                () -> Assertions.assertTrue(actual.getMenuItem("?????????????? ????????????").isEmpty()),
                () -> AssertUtil.assertEquals(dishes.get(2),
                        actual.tryGetItem("?????????????? ??????").getDish()),
                () -> AssertUtil.assertEquals(new BigDecimal(5),
                        actual.tryGetItem("???????????????? ????????????????").getNecessaryQuantity(BigDecimal.ONE)),
                () -> AssertUtil.assertEquals(new BigDecimal(1),
                        actual.tryGetItem("?????????????? ??????").getNecessaryQuantity(BigDecimal.ONE))
        );
    }

    @Test
    @DisplayName("""
            generate(input):
             input is not null,
             solution not exists
             => exception
            """)
    public void generate4() {
        MenuGeneratorService service = new MenuGeneratorService(conf);
        Input input = new Input.Builder().
                setUser(user).
                setGeneratedMenuName("?????????? ????????").
                setMaxPrice(new BigDecimal(2700)).
                setMinMealsNumber(2).
                setServingNumberPerMeal(new BigDecimal(3)).
                addProductConstraint("????????", "lessOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????????????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("???????????????????????? ??????????", "lessOrEqual", new BigDecimal(4)).
                addProductConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????", "greaterOrEqual", new BigDecimal(2)).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addProductConstraint("??????????", "greaterOrEqual", BigDecimal.ONE).
                addProductConstraint("????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("????????????????", "lessOrEqual", BigDecimal.TEN).
                addDishConstraint("??????????????", "greaterOrEqual", BigDecimal.ZERO).
                addDishConstraint("??????", "greaterOrEqual", BigDecimal.ONE).
                setDishRepository(dishRepository).
                setMenuRepository(menuRepository).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> service.generate(input),
                "MenuGeneratorService.generate[SOLUTION_EXISTS]", Constraint.SOLUTION_EXISTS
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

}
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.*;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.validation.Constraint;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.*;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

class MenuRepositoryTest {

    private static HikariDataSource dataSource;
    private static ProductRepositoryPostgres productRepository;
    private static DishRepository dishRepository;
    private static MenuRepository menuRepository;
    private static UserRepository userRepository;
    private static DataSourceTransactionManager transactionManager;
    private static AppConfigData appConfiguration;

    static {
        try {
            appConfiguration = new AppConfigData(
                    "/config/appConfig.properties",
                    "/config/security.properties"
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void beforeAll() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        hikariConfig.setUsername(appConfiguration.getDatabaseUser());
        hikariConfig.setPassword(appConfiguration.getDatabasePassword());
        hikariConfig.addDataSourceProperty("databaseName", appConfiguration.getDatabaseName());
        hikariConfig.setAutoCommit(false);
        hikariConfig.addDataSourceProperty("portNumber", "5432");
        hikariConfig.addDataSourceProperty("serverName", "localhost");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setPoolName("hikariPool");
        dataSource = new HikariDataSource(hikariConfig);

        transactionManager = new DataSourceTransactionManager(dataSource);

        userRepository = new UserRepositoryPostgres(dataSource);
        productRepository = new ProductRepositoryPostgres(dataSource, appConfiguration);
        dishRepository = new DishRepositoryPostgres(dataSource, appConfiguration, productRepository);
        menuRepository = new MenuRepositoryPostgres(dataSource, appConfiguration, dishRepository);
    }

    @BeforeEach
    void beforeEach() {
        try(Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute(
                    "CREATE SCHEMA IF NOT EXISTS public AUTHORIZATION " +
                            appConfiguration.getDatabaseUser() + ";");
            conn.commit();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }

        Flyway.configure().
                locations("classpath:db").
                dataSource(dataSource).
                load().
                migrate();
    }

    @AfterEach
    void afterEach() {
        try(Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("DROP SCHEMA public CASCADE;");
            conn.commit();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void afterAll() {
        dataSource.close();
    }

    @Test
    @DisplayName("""
            save(menu):
             menu is null
             => exception
            """)
    public void save1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.save(null),
                "MenuRepositoryPostgres.save",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            save(menu):
             no menus in DB
             => return true
            """)
    public void save2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu menu = createMenu(user, 1, dishes).tryBuild();

        boolean actual = commit(() -> menuRepository.save(menu));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             no menus in DB
             => add menu
            """)
    public void save3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu expected = createMenu(user, 1, dishes).tryBuild();

        commit(() -> menuRepository.save(expected));
        Menu actual = menuRepository.tryGetById(user.getId(), toUUID(1));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id not exists
             => return true
            """)
    public void save4() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu menu = createMenu(user, 100, dishes).tryBuild();

        boolean actual = commit(() -> menuRepository.save(menu));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id not exists
             => add menu
            """)
    public void save5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu expected = createMenu(user, 100, dishes).tryBuild();

        commit(() -> menuRepository.save(expected));
        Menu actual = menuRepository.tryGetById(user.getId(), toUUID(100));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id not exists,
             user has a menu with the same name and other id
             => exception
            """)
    public void save6() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu menu = createMenu(user, 100, dishes).
                setName("Menu#0").
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(menu)),
                "MenuRepositoryPostgres.save",
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             menu state was changed
             => return true
            """)
    public void save7() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu menu = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(menu));

        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("updated menu name").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 1000)).
                                setDishName("dish#1000").
                                setQuantity(new BigDecimal("550.7"))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> dishes.get(0)).
                                setDishName(dishes.get(0).getName()).
                                setQuantity(new BigDecimal(100))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 10000)).
                                setDishName("dish#10000").
                                setQuantity(new BigDecimal("100.05"))
                ).
                tryBuild();
        boolean actual = commit(() -> menuRepository.save(updatedMenu));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             menu state was changed
             => update menu
             """)
    public void save8() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("updated menu name").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 1000)).
                                setDishName("dish#1000").
                                setQuantity(new BigDecimal("550.7"))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> dishes.get(0)).
                                setDishName(dishes.get(0).getName()).
                                setQuantity(new BigDecimal(100))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 10000)).
                                setDishName("dish#10000").
                                setQuantity(new BigDecimal("100.05"))
                ).
                tryBuild();
        commit(() -> menuRepository.save(updatedMenu));
        Menu actual = menuRepository.tryGetById(user.getId(), updatedMenu.getId());

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             menu state was changed,
             user has menu with the same name and other id
             => exception
            """)
    public void save9() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("Menu#0").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 1000)).
                                setDishName("dish#1000").
                                setQuantity(new BigDecimal("550.7"))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> dishes.get(0)).
                                setDishName(dishes.get(0).getName()).
                                setQuantity(new BigDecimal(100))
                ).
                addItem(
                        new MenuItem.Builder().
                                setConfig(appConfiguration).
                                setDish(() -> createDish(user, 10000)).
                                setDishName("dish#10000").
                                setQuantity(new BigDecimal("100.05"))
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(updatedMenu)),
                "MenuRepositoryPostgres.save",
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             menu state wasn't changed,
             => return false
            """)
    public void save10() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        boolean actual = commit(() -> menuRepository.save(expected));

        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             menu state wasn't changed,
             => don't update menu
            """)
    public void save11() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        commit(() -> menuRepository.save(expected));
        Menu actual = menuRepository.tryGetById(user.getId(), toUUID(100));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             menuId is null
             => exception
            """)
    public void tryRemove1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryRemove(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             userId is null
             => exception
            """)
    public void tryRemove2() {
        AssertUtil.assertValidateException(
                () -> menuRepository.tryRemove(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             menu with such id not exists in DB
             => exception
            """)
    public void tryRemove3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryRemove(user.getId(), toUUID(100)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             menu with such id exists in DB,
             user is not owner of this menu
             => exception
            """)
    public void tryRemove4() {
        User user = createAndSaveUser(1);
        User otherUser = createAndSaveUser(2);
        List<Dish> dishes = createAndSaveDishes(user);
        createAndSaveMenus(user, dishes);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryRemove(otherUser.getId(), toUUID(1)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             menu with such id exists in DB,
             user is owner of this menu
             => remove menu
            """)
    public void tryRemove5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        List<Menu> menus = createAndSaveMenus(user, dishes);

        commit(() -> menuRepository.tryRemove(user.getId(), toUUID(0)));
        Optional<Menu> actual = menuRepository.getById(user.getId(), toUUID(0));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            tryRemove(userId, menuId):
             menu with such id exists in DB,
             user is owner of this menu
             => return removed menu
            """)
    public void tryRemove6() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        List<Menu> menus = createAndSaveMenus(user, dishes);

        Menu actual = commit(() -> menuRepository.tryRemove(user.getId(), toUUID(0)));

        Menu expected = menus.get(0);
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getById(userId, menuId):
             menuId is null
             => exception
            """)
    public void getById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.getById(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(userId, menuId):
             userId is null
             => exception
            """)
    public void getById2() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getById(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(userId, menuId):
             not exists menu with such id
             => return empty Optional
            """)
    public void getById3() {
        User user = createAndSaveUser(1);

        Optional<Menu> actual = menuRepository.getById(user.getId(), toUUID(100));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getById(userId, menuId):
             menu with such id exists in DB,
             user is not owner of this menu
             => return empty Optional
            """)
    public void getById4() {
        User user = createAndSaveUser(1);
        User otherUser = createAndSaveUser(2);
        List<Dish> dishes = createAndSaveDishes(user);
        commit(() -> menuRepository.save(createMenu(user, 1, dishes).tryBuild()));

        Optional<Menu> actual = menuRepository.getById(otherUser.getId(), toUUID(1));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getById(userId, menuId):
             menu with such id exists in DB,
             user is owner of this menu
             => return correct result
            """)
    public void getById5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu actual = menuRepository.getById(user.getId(), toUUID(100)).orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryGetById(userId, menuId):
             menuId is null
             => exception
            """)
    public void tryGetById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetById(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, menuId):
             userId is null
             => exception
            """)
    public void tryGetById2() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetById(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, menuId):
             not exists menu with such id
             => exception
            """)
    public void tryGetById3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetById(user.getId(), toUUID(100)),
                "MenuRepositoryPostgres.tryGetById"
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, menuId):
             exists menu with such id,
             user is not owner of this menu
             => exception
            """)
    public void tryGetById4() {
        User user = createAndSaveUser(1);
        User otherUser = createAndSaveUser(2);
        List<Dish> dishes = createAndSaveDishes(user);
        commit(() -> menuRepository.save(createMenu(user, 1, dishes).tryBuild()));

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetById(otherUser.getId(), toUUID(1)),
                "MenuRepositoryPostgres.tryGetById"
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, menuId):
             exists menu with such id,
             user is owner of this menu
             => return correct result
            """)
    public void tryGetById5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu actual = menuRepository.tryGetById(user.getId(), toUUID(100));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             name is null
             => exception
            """)
    public void getByName1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.getByName(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             userId is null
             => exception
            """)
    public void getByName2() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getByName(null, "some menu"),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             not exists menu with such name
             => return empty Optional
            """)
    public void getByName3() {
        User user = createAndSaveUser(1);

        Optional<Menu> actual = menuRepository.getByName(user.getId(), "unknown menu");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             menu with such name exists in DB,
             user is not owner of this menu
             => return empty Optional
            """)
    public void getByName4() {
        User user = createAndSaveUser(1);
        User otherUser = createAndSaveUser(2);
        List<Dish> dishes = createAndSaveDishes(user);
        commit(() -> menuRepository.save(createMenu(user, 1, dishes).tryBuild()));

        Optional<Menu> actual = menuRepository.getByName(otherUser.getId(), "Menu#1");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             menu with such name exists in DB,
             user is owner of this menu
             => return correct result
            """)
    public void getByName5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu actual = menuRepository.getByName(user.getId(), "Menu#100").orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             name is null
             => exception
            """)
    public void tryGetByName1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetByName(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             userId is null
             => exception
            """)
    public void tryGetByName2() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetByName(null, "some menu"),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             not exists menu with such name
             => exception
            """)
    public void tryGetByName3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetByName(user.getId(), "unknown menu"),
                "MenuRepositoryPostgres.tryGetByName"
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             exists menu with such name,
             user is not owner of this menu
             => exception
            """)
    public void tryGetByName4() {
        User user = createAndSaveUser(1);
        User otherUser = createAndSaveUser(2);
        List<Dish> dishes = createAndSaveDishes(user);
        commit(() -> menuRepository.save(createMenu(user, 1, dishes).tryBuild()));

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetByName(otherUser.getId(), "Menu#1"),
                "MenuRepositoryPostgres.tryGetByName"
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             exists menu with such name,
             user is owner of this menu
             => return correct result
            """)
    public void tryGetByName5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);
        Menu expected = createMenu(user, 100, dishes).tryBuild();
        commit(() -> menuRepository.save(expected));

        Menu actual = menuRepository.tryGetByName(user.getId(), "Menu#100");

        AssertUtil.assertEquals(expected, actual);
    }


    private <T>T commit(Supplier<T> supplier) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            T value = supplier.get();
            transactionManager.commit(status);
            return value;
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    private void commit(Action action) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            action.act();
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private User createAndSaveUser(int userId) {
        User user = new User.Builder().
                setId(toUUID(userId)).
                setName("User#" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
        commit(() -> userRepository.save(user));
        return user;
    }

    private List<Dish> createAndSaveDishes(User user) {
        List<Dish> result = IntStream.range(0, 5).
                mapToObj(i -> createDish(user, i)).
                toList();

        commit(() -> result.forEach(dish -> dishRepository.save(dish)));

        return result;
    }

    private Menu.Builder createMenu(User user, int menuId, List<Dish> dishes) {
        Menu.Builder builder = new Menu.Builder().
                setId(toUUID(menuId)).
                setUser(user).
                setName("Menu#" + menuId).
                setDescription("Description for menu#" + menuId).
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=" + menuId).
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("tag#" + menuId);

        IntStream.range(0, dishes.size()).
                forEach(i -> builder.addItem(
                                new MenuItem.Builder().
                                        setConfig(appConfiguration).
                                        setDish(() -> dishes.get(i)).
                                        setDishName(dishes.get(i).getName()).
                                        setQuantity(new BigDecimal((i + 1) * 100))
                        )
                );

        return builder;
    }

    private List<Menu> createAndSaveMenus(User user, List<Dish> dishes) {
        List<Menu> menus = IntStream.range(0, 5).
                mapToObj(i -> createMenu(user, i, dishes).tryBuild()).
                toList();

        commit(() -> menus.forEach(menu -> menuRepository.save(menu)));

        return menus;
    }


    private Dish createDish(User user, int dishId) {
        return new Dish.Builder().
                setId(toUUID(dishId)).
                setUser(user).
                setName("dish#" + dishId).
                setServingSize(BigDecimal.ONE).
                setUnit("unit A").
                setDescription("description for dish#" + dishId).
                setImageUrl("https://nutritionmanager.xyz/dishes/images?id=" + dishId).
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag A").
                addTag("common tag").
                addTag("tag 2").
                addTag("2 tag").
                addTag("tag 1").
                addTag("1 tag").
                addIngredient("ingredient 1",
                        Filter.orElse(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new com.bakuard.nutritionManager.model.Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                ),
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new com.bakuard.nutritionManager.model.Tag("tag B"))
                                )
                        ),
                        BigDecimal.TEN).
                addIngredient("ingredient 2",
                        Filter.orElse(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new com.bakuard.nutritionManager.model.Tag("value 1")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A")
                                ),
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyManufacturer("manufacturer B")
                                )
                        ),
                        new BigDecimal("2.5")).
                addIngredient("ingredient 3",
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new com.bakuard.nutritionManager.model.Tag("value 1"), new Tag("value 2")),
                                Filter.anyCategory("name A"),
                                Filter.anyShop("shop B"),
                                Filter.anyGrade("variety B")
                        ),
                        new BigDecimal("0.1")).
                tryBuild();
    }

}
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.DishRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.MenuRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

class MenuRepositoryTest {

    private static HikariDataSource dataSource;
    private static ProductRepositoryPostgres productRepository;
    private static DishRepositoryPostgres dishRepository;
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
        menuRepository = new MenuRepositoryPostgres(dataSource, appConfiguration, dishRepository, productRepository);
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
        Menu menu = createMenu(user, 1);

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
        Menu expected = createMenu(user, 1);

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
        createAndSaveMenus(user);
        Menu menu = createMenu(user, 100);

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
        createAndSaveMenus(user);
        Menu expected = createMenu(user, 100);

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
        createAndSaveMenus(user);
        Menu menu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("Menu#0").
                setDescription("Description for menu#100").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=100").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("tag#100").
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(menu)),
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
        createAndSaveMenus(user);
        Menu menu = createMenu(user, 100);
        commit(() -> menuRepository.save(menu));

        Dish dish0 = createDish(user, 0, 0, 1, 2);
        Dish dish1000 = createDish(user, 1000, 3, 4, 5);
        Dish dish10000 = createDish(user, 10000, 6, 7, 8);
        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("updated menu name").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(createMenuItem(dish0, new BigDecimal(107), 0)).
                addItem(createMenuItem(dish1000, new BigDecimal("12.5"), 1)).
                addItem(createMenuItem(dish10000, new BigDecimal(7), 2)).
                tryBuild();
        commit(() -> {
            dishRepository.save(dish0);
            dishRepository.save(dish1000);
            dishRepository.save(dish10000);
        });
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
        createAndSaveMenus(user);
        Menu expected = createMenu(user, 100);
        commit(() -> menuRepository.save(expected));

        Dish dish0 = createDish(user, 0, 0, 1, 2);
        Dish dish1000 = createDish(user, 1000, 3, 4, 5);
        Dish dish10000 = createDish(user, 10000, 6, 7, 8);
        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("updated menu name").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(createMenuItem(dish0, new BigDecimal(107), 0)).
                addItem(createMenuItem(dish1000, new BigDecimal("12.5"), 1)).
                addItem(createMenuItem(dish10000, new BigDecimal(7), 2)).
                tryBuild();
        commit(() -> {
            dishRepository.save(dish0);
            dishRepository.save(dish1000);
            dishRepository.save(dish10000);
            menuRepository.save(updatedMenu);
        });
        Menu actual = menuRepository.tryGetById(user.getId(), updatedMenu.getId());

        AssertUtil.assertEquals(updatedMenu, actual);
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
        createAndSaveMenus(user);
        Menu expected = createMenu(user, 100);
        commit(() -> menuRepository.save(expected));

        Dish dish0 = createDish(user, 0, 0, 1, 2);
        Dish dish1000 = createDish(user, 1000, 3, 4, 5);
        Dish dish10000 = createDish(user, 10000, 6, 7, 8);
        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(100)).
                setUser(user).
                setName("Menu#0").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=newMenuImage").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("new unique tag").
                addItem(createMenuItem(dish0, new BigDecimal(107), 0)).
                addItem(createMenuItem(dish1000, new BigDecimal("12.5"), 1)).
                addItem(createMenuItem(dish10000, new BigDecimal(7), 2)).
                tryBuild();
        commit(() -> {
            dishRepository.save(dish0);
            dishRepository.save(dish1000);
            dishRepository.save(dish10000);
        });

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(updatedMenu)),
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
        createAndSaveMenus(user);
        Menu expected = createMenu(user, 100);
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
        createAndSaveMenus(user);
        Menu expected = createMenu(user, 100);
        commit(() -> menuRepository.save(expected));

        commit(() -> menuRepository.save(expected));
        Menu actual = menuRepository.tryGetById(user.getId(), toUUID(100));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id not exists,
             user doesn't have some of menu item dishes
             => exception
            """)
    public void save12() {
        User otherUser = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(otherUser, 1000, 0, 1, 2)));
        User user = createAndSaveUser(2);
        Menu menu = new Menu.Builder().
                setId(toUUID(1)).
                setUser(user).
                setName("Menu#1").
                setDescription("Description for menu#1").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=1").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("tag#1").
                addItem(
                        createMenuItem(createDish(user, 1000, 0, 1, 2),
                                new BigDecimal("10.1"), 0)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(menu)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(menu):
             there are menus in DB,
             menu id exists,
             user doesn't have some of menu item dishes
             => exception
            """)
    public void save13() {
        User otherUser = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(otherUser, 1000, 0, 1, 2)));
        User user = createAndSaveUser(2);
        commit(() -> menuRepository.save(createMenu(user, 1)));
        Menu updatedMenu = new Menu.Builder().
                setId(toUUID(1)).
                setUser(user).
                setName("Menu#1").
                setDescription("Description for menu#1").
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=1").
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("tag#1").
                addItem(
                        createMenuItem(createDish(user, 1000, 0, 1, 2),
                                new BigDecimal("10.1"), 0)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> menuRepository.save(updatedMenu)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
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
        commit(() -> menuRepository.save(createMenu(user, 1)));

        AssertUtil.assertValidateException(
                () -> menuRepository.tryRemove(user.getId(), toUUID(100)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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
        commit(() -> menuRepository.save(createMenu(user, 1)));

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
        commit(() -> menuRepository.save(createMenu(user, 0)));

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
        Menu expected = createMenu(user, 0);
        commit(() -> menuRepository.save(expected));

        Menu actual = commit(() -> menuRepository.tryRemove(user.getId(), toUUID(0)));

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
        commit(() -> menuRepository.save(createMenu(user, 1)));

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
        Menu expected = createMenu(user, 100);
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
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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

        commit(() -> menuRepository.save(createMenu(user, 1)));

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetById(otherUser.getId(), toUUID(1)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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
        Menu expected = createMenu(user, 100);
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
        commit(() -> menuRepository.save(createMenu(user, 1)));

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
        Menu expected = createMenu(user, 100);
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
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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
        commit(() -> menuRepository.save(createMenu(user, 1)));

        AssertUtil.assertValidateException(
                () -> menuRepository.tryGetByName(otherUser.getId(), "Menu#1"),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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
        Menu expected = createMenu(user, 100);
        commit(() -> menuRepository.save(expected));

        Menu actual = menuRepository.tryGetByName(user.getId(), "Menu#100");

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             criteria is null
             => exception
            """)
    public void getNumberMenus1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getMenusNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user haven't any menus
             => return 0
            """)
    public void getNumberMenus2() {
        User user = createAndSaveUser(1);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );
        
        Assertions.assertEquals(0, actual);
    }
    
    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             only user filter
             => return all menus number
            """)
    public void getNumberMenus3() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);
        
        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );
        
        Assertions.assertEquals(4, actual);
    }
    
    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is MinTags - matching not exists
             => return 0
            """)
    public void getNumberMenus4() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new Tag("common tag"), new Tag("unknown tag"))
                        )
                )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is MinTags - matching exists
             => return correct result
            """)
    public void getNumberMenus5() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new Tag("common tag"), new Tag("tagA"))
                        )
                )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is Dishes - matching not exists
             => return 0
            """)
    public void getNumberMenus6() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.anyDish("unknown dish")
                        )
                )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is Dishes - matching exists
             => return correct result
            """)
    public void getNumberMenus7() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.anyDish("dish#50")
                        )
                )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Dishes - matching not exists
             => return 0
            """)
    public void getNumberMenus8() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new Tag("common tag")),
                                Filter.anyDish("unknown dish")
                        )
                )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Dishes - matching exists
             => return 0
            """)
    public void getNumberMenus9() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new Tag("tagA")),
                                Filter.anyDish("dish#2")
                        )
                )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberMenus(criteria):
             user have menus,
             filter is AndFilter - matching exists
                MinTags - matching exists,
                Dishes - matching exists
             => return correct result
            """)
    public void getNumberMenus10() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getMenusNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.minTags(new Tag("tagA")),
                                Filter.anyDish("dish#100")
                        )
                )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             criteria is null
             => exception
            """)
    public void getMenus1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getMenus(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             user haven't any menus
             => return empty page
            """)
    public void getMenus2() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);
        User otherUser = createAndSaveUser(2);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(Filter.user(otherUser.getId())).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        Assertions.assertTrue(actual.getMetadata().isEmpty());
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             only user filter
             => return all Menus
            """)
    public void getMenus3() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        Page<Menu> expected = PageableByNumber.of(4, 0).
                createPageMetadata(4, 30).
                createPage(menus);
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is MinTags - matching not exists
             => return empty page
            """)
    public void getMenus4() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()), 
                                        Filter.minTags(new Tag("unknown tag"))
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );
        
        AssertUtil.assertEquals(Page.empty(), actual);
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is MinTags - matching exists
             => return correct result
            """)
    public void getMenus5() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()), 
                                        Filter.minTags(new Tag("common tag"), new Tag("tagA"))
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        Page<Menu> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 30).
                createPage(menus.subList(0, 2));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is Dishes - matching not exists
             => return empty page
            """)
    public void getMenus6() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()), 
                                        Filter.anyDish("unknown dish")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        AssertUtil.assertEquals(Page.empty(), actual);
    }
    
    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is Dishes - matching exists
             => return correct result
            """)
    public void getMenus7() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()), 
                                        Filter.anyDish("dish#50")
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        Page<Menu> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 30).
                createPage(menus.subList(0, 2));
        AssertUtil.assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Dishes - matching not exists
             => return empty page
            """)
    public void getMenus8() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyDish("unknown dish")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );
        
        AssertUtil.assertEquals(Page.empty(), actual);
    }
    
    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Dishes - matching exists
             => return empty page
            """)
    public void getMenus9() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tagA")),
                                        Filter.anyDish("dish#2")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        AssertUtil.assertEquals(Page.empty(), actual);
    }
    
    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is AndFilter - matching exists
                MinTags - matching exists,
                Dishes - matching exists
             => return correct result
            """)
    public void getMenus10() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tagA")),
                                        Filter.anyDish("dish#100")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.menuDefaultSort())
        );

        Page<Menu> expected = PageableByNumber.of(4, 0).
                createPageMetadata(2, 30).
                createPage(menus.subList(0, 2));
        AssertUtil.assertEquals(expected, actual);
    }
    
    @Test
    @DisplayName("""
            getMenus(criteria):
             user have menus,
             filter is AndFilter - matching exists
                MinTags - matching exists,
                Dishes - matching exists,
             not first page
             => return correct result
            """)
    public void getMenus11() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Menu> actual = menuRepository.getMenus(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyDish("dish#100")
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 1)).
                        setSort(Sort.menuDefaultSort())
        );

        Page<Menu> expected = PageableByNumber.of(2, 1).
                createPageMetadata(4, 30).
                createPage(menus.subList(2, 4));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             criteria is null
             => exception
            """)
    public void getTagsNumber1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getTagsNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             user haven't any menus
             => return 0
            """)
    public void getTagsNumber2() {
        User user = createAndSaveUser(1);

        int actual = menuRepository.getTagsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             user have menus
             => return correct result
            """)
    public void getTagsNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getTagsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertEquals(7, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             criteria is null
             => exception
            """)
    public void getTags1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getTags(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user haven't any menus
             => return empty page
            """)
    public void getTags2() {
        User user = createAndSaveUser(1);

        Page<Tag> actual = menuRepository.getTags(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Assertions.assertEquals(Page.empty(), actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have menus
             => return correct result
            """)
    public void getTags3() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<Tag> actual = menuRepository.getTags(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<Tag> expected = PageableByNumber.of(2, 1).
                createPageMetadata(7, 1000).
                createPage(getAllTags(menus).subList(2, 4));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNamesNumber(criteria):
             criteria is null
             => exception
            """)
    public void getNamesNumber1() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getNamesNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNamesNumber(criteria):
             user haven't any menus
             => return 0
            """)
    public void getNamesNumber2() {
        User user = createAndSaveUser(1);

        int actual = menuRepository.getNamesNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNamesNumber(criteria):
             user have menus
             => return correct result
            """)
    public void getNamesNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveMenus(user);

        int actual = menuRepository.getNamesNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertEquals(4, actual);
    }

    @Test
    @DisplayName("""
            getNames(criteria):
             criteria is null
             => exception
            """)
    public void getNames() {
        AssertUtil.assertValidateException(
                () -> menuRepository.getNames(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNames(criteria):
             user haven't any menus
             => return empty page
            """)
    public void getNames2() {
        User user = createAndSaveUser(1);

        Page<String> actual = menuRepository.getNames(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Assertions.assertEquals(Page.empty(), actual);
    }

    @Test
    @DisplayName("""
            getNames(criteria):
             user have menus
             => return correct result
            """)
    public void getNames3() {
        User user = createAndSaveUser(1);
        List<Menu> menus = createAndSaveMenus(user);

        Page<String> actual = menuRepository.getNames(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<String> expected = PageableByNumber.of(2, 1).
                createPageMetadata(4, 1000).
                createPage(getAllNames(menus).subList(2, 4));
        Assertions.assertEquals(expected, actual);
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

    private Menu createMenu(User user, int menuId) {
        List<Dish> dishes = List.of(
                createDish(user, 0, 100, 101, 102),
                createDish(user, 2, 103, 104, 105),
                createDish(user, 1, 106, 107, 108),
                createDish(user, 4, 109, 110, 111),
                createDish(user, 3, 112, 113, 114)
        );

        Menu menu = new Menu.Builder().
                setId(toUUID(menuId)).
                setUser(user).
                setName("Menu#" + menuId).
                setDescription("Description for menu#" + menuId).
                setImageUrl("https://nutritionmanager.xyz/menus/menuId=" + menuId).
                setConfig(appConfiguration).
                addTag("common tag").
                addTag("tag#" + menuId).
                addItem(createMenuItem(dishes.get(0), new BigDecimal("3.5"), 1000)).
                addItem(createMenuItem(dishes.get(1), new BigDecimal("10.1"), 1001)).
                addItem(createMenuItem(dishes.get(2), new BigDecimal("5"), 1002)).
                addItem(createMenuItem(dishes.get(3), new BigDecimal("2"), 1003)).
                addItem(createMenuItem(dishes.get(4), new BigDecimal("0.4"), 1004)).
                tryBuild();

        commit(() -> dishes.forEach(dish -> dishRepository.save(dish)));

        return menu;
    }

    private List<Menu> createAndSaveMenus(User user) {
        Dish dish0, dish1, dish2, dish3, dish50, dish60, dish100;
        List<Dish> dishes = List.of(
                dish0 = createDish(user, 0, 1000, 1001, 1002),
                dish1 = createDish(user, 1, 10001, 10002, 10003),
                dish2 = createDish(user, 2, 100001, 100002, 100003),
                dish3 = createDish(user, 3, 1000001, 1000002, 1000003),
                dish50 = createDish(user, 50, 10000001, 10000002, 10000003),
                dish60 = createDish(user, 60, 100000001, 100000002, 100000003),
                dish100 = createDish(user, 100, 1000000001, 1000000002, 1000000003)
        );

        List<Menu> menus = new ArrayList<>();
        menus.add(
                new Menu.Builder().
                        setId(toUUID(0)).
                        setUser(user).
                        setName("Menu#0").
                        setDescription("Description for menu#0").
                        setImageUrl("https://nutritionmanager.xyz/menus/menuId=0").
                        setConfig(appConfiguration).
                        addTag("common tag").
                        addTag("tag#0").
                        addTag("tagA").
                        addItem(createMenuItem(dish0, BigDecimal.TEN, 2001)).
                        addItem(createMenuItem(dish50, new BigDecimal("3.5"), 2002)).
                        addItem(createMenuItem(dish100, new BigDecimal(2), 2003)).
                        tryBuild()
        );

        menus.add(
                new Menu.Builder().
                        setId(toUUID(1)).
                        setUser(user).
                        setName("Menu#1").
                        setDescription("Description for menu#1").
                        setImageUrl("https://nutritionmanager.xyz/menus/menuId=1").
                        setConfig(appConfiguration).
                        addTag("common tag").
                        addTag("tag#1").
                        addTag("tagA").
                        addItem(createMenuItem(dish1, BigDecimal.TEN, 2011)).
                        addItem(createMenuItem(dish50, new BigDecimal("3.5"), 2012)).
                        addItem(createMenuItem(dish100, new BigDecimal(2), 2013)).
                        tryBuild()
        );

        menus.add(
                new Menu.Builder().
                        setId(toUUID(2)).
                        setUser(user).
                        setName("Menu#2").
                        setDescription("Description for menu#2").
                        setImageUrl("https://nutritionmanager.xyz/menus/menuId=2").
                        setConfig(appConfiguration).
                        addTag("common tag").
                        addTag("tag#2").
                        addTag("tagB").
                        addItem(createMenuItem(dish2, BigDecimal.TEN, 2101)).
                        addItem(createMenuItem(dish60, new BigDecimal("3.5"), 2102)).
                        addItem(createMenuItem(dish100, new BigDecimal(2), 2103)).
                        tryBuild()
        );

        menus.add(
                new Menu.Builder().
                        setId(toUUID(3)).
                        setUser(user).
                        setName("Menu#3").
                        setDescription("Description for menu#3").
                        setImageUrl("https://nutritionmanager.xyz/menus/menuId=3").
                        setConfig(appConfiguration).
                        addTag("common tag").
                        addTag("tag#3").
                        addTag("tagB").
                        addItem(createMenuItem(dish3, BigDecimal.TEN, 2051)).
                        addItem(createMenuItem(dish60, new BigDecimal("3.5"), 2052)).
                        addItem(createMenuItem(dish100, new BigDecimal(2), 2053)).
                        tryBuild()
        );

        commit(() -> {
            dishes.forEach(dish -> dishRepository.save(dish));
            menus.forEach(menu -> menuRepository.save(menu));
        });

        return menus;
    }

    private List<Tag> getAllTags(List<Menu> menus) {
        return menus.stream().
                flatMap(m -> m.getTags().stream()).
                distinct().
                sorted().
                toList();
    }

    private List<String> getAllNames(List<Menu> menus) {
        return menus.stream().
                map(Menu::getName).
                distinct().
                sorted().
                toList();
    }


    private Dish createDish(User user, int dishId, int ingredient1, int ingredient2, int ingredient3) {
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
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(ingredient1)).
                                setFilter(
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
                                        )
                                ).
                                setName("ingredient 1").
                                setQuantity(BigDecimal.TEN).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(ingredient2)).
                                setFilter(
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
                                        )
                                ).
                                setName("ingredient 2").
                                setQuantity(new BigDecimal("2.5")).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(ingredient3)).
                                setFilter(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new com.bakuard.nutritionManager.model.Tag("value 1"), new Tag("value 2")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyGrade("variety B")
                                        )
                                ).
                                setName("ingredient 3").
                                setQuantity(new BigDecimal("0.1")).
                                setConfig(appConfiguration)
                ).
                tryBuild();
    }

    private MenuItem.LoadBuilder createMenuItem(Dish dish, BigDecimal quantity, int itemId) {
        return new MenuItem.LoadBuilder().
                setId(toUUID(itemId)).
                setConfig(appConfiguration).
                setDish(dish).
                setQuantity(quantity);
    }

}
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishFieldCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishFieldNumberCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishesNumberCriteria;
import com.bakuard.nutritionManager.dal.impl.DishRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

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
import java.util.UUID;
import java.util.function.Supplier;

class DishRepositoryPostgresTest {

    private static HikariDataSource dataSource;
    private static ProductRepository productRepository;
    private static DishRepository dishRepository;
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
            save(dish):
             dish is null
             => exception
            """)
    public void save1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.save(null),
                DishRepositoryPostgres.class,
                "save",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            save(dish):
             no dishes in DB
             => return true
            """)
    public void save2() {
        User user = createAndSaveUser(1);
        Dish dish = createDish(1, user);

        boolean actual = commit(() -> dishRepository.save(dish));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             no dishes in DB
             => add dish
            """)
    public void save3() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(1, user);

        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.getById(toUUID(1));

        Assertions.assertTrue(expected.equalsFullState(actual),
                "expected: " + expected + "\nactual: " + actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id not exists
             => return true
            """)
    public void save4() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        boolean actual = commit(() -> dishRepository.save(dish));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id not exists
             => add dish
            """)
    public void save5() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish expected = createDish(7, user);

        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.getById(toUUID(7));

        Assertions.assertTrue(expected.equalsFullState(actual),
                "expected: " + expected + "\nactual: " + actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id not exists,
             user has a dish with the same name and other id
             => exception
            """)
    public void save6() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);
        dish.setName("dish 1");

        AssertUtil.assertServiceException(
                () -> commit(() -> dishRepository.save(dish)),
                DishRepositoryPostgres.class,
                "save",
                ConstraintType.ALREADY_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id exists,
             dish state was changed
             => return true
            """)
    public void save7() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish updatedDish = new Dish(dish);
        updatedDish.setName("New name");
        updatedDish.removeIngredient("ingredient 1");
        updatedDish.putIngredient("ingredient 4", CategoriesFilter.of("category Z"), BigDecimal.TEN);
        updatedDish.removeTag(new Tag("tag A"));
        updatedDish.addTag(new Tag("tag Z"));
        boolean actual = commit(() -> dishRepository.save(updatedDish));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id exists,
             dish state was changed
             => update dish
            """)
    public void save8() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish expected = new Dish(dish);
        expected.setName("New name");
        expected.removeIngredient("ingredient 1");
        expected.putIngredient("ingredient 4", CategoriesFilter.of("category Z"), BigDecimal.TEN);
        expected.removeTag(new Tag("tag A"));
        expected.addTag(new Tag("tag Z"));
        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.getById(toUUID(7));

        Assertions.assertTrue(expected.equalsFullState(actual),
                "expected: " + expected + "\nactual: " + actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id exists,
             dish state was changed,
             user has dish with the same name and other id
             => exception
            """)
    public void save9() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish updatedDish = new Dish(dish);
        updatedDish.setName("dish 1");

        AssertUtil.assertServiceException(
                () -> dishRepository.save(updatedDish),
                DishRepositoryPostgres.class,
                "save",
                ConstraintType.ALREADY_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id exists,
             dish state wasn't changed,
             => return false
            """)
    public void save10() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        boolean actual = commit(() -> dishRepository.save(dish));

        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("""
            save(dish):
             there are dishes in DB,
             dish id exists,
             dish state wasn't changed,
             => don't update dish
            """)
    public void save11() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish expected = createDish(7, user);

        commit(() -> dishRepository.save(expected));
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.getById(toUUID(7));
        Assertions.assertTrue(expected.equalsFullState(actual),
                "expected: " + expected + "\nactual: " + actual);
    }

    @Test
    @DisplayName("""
            remove(dishId):
             dishId is null
             => exception
            """)
    public void remove1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.remove(null),
                DishRepositoryPostgres.class,
                "remove",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            remove(dishId):
             dish with such id not exists
             => exception
            """)
    public void remove2() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        AssertUtil.assertServiceException(
                () -> dishRepository.remove(toUUID(100)),
                DishRepositoryPostgres.class,
                "remove",
                ConstraintType.UNKNOWN_ENTITY
        );
    }

    @Test
    @DisplayName("""
            remove(dishId):
             dish with such id exists
             => remove dish
            """)
    public void remove3() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        commit(() -> dishRepository.remove(toUUID(100)));

        AssertUtil.assertServiceException(
                () -> dishRepository.getById(toUUID(100)),
                DishRepositoryPostgres.class,
                "getById",
                ConstraintType.UNKNOWN_ENTITY
        );
    }

    @Test
    @DisplayName("""
            remove(dishId):
             dish with such id exists
             => return removed dish
            """)
    public void remove4() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.remove(toUUID(100));

        Assertions.assertTrue(expected.equalsFullState(actual));
    }

    @Test
    @DisplayName("""
            getById(dishId):
             dishId is null
             => exception
            """)
    public void getById1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getById(null),
                DishRepositoryPostgres.class,
                "getById",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getById(dishId):
             not exists dish with such id
             => exception
            """)
    public void getById2() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getById(toUUID(100)),
                DishRepositoryPostgres.class,
                "getById",
                ConstraintType.UNKNOWN_ENTITY
        );
    }

    @Test
    @DisplayName("""
            getById(dishId):
             exists dish with such id
             => return dish
            """)
    public void getById3() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.getById(toUUID(100));

        System.out.println(expected);
        System.out.println(actual);
        Assertions.assertTrue(expected.equalsFullState(actual));
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             criteria is null
             => exception
            """)
    public void getDishesNumber1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getDishesNumber(null),
                DishRepositoryPostgres.class,
                "getNumberDishes",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getDishesNumber2() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(2);

        int actual = dishRepository.getDishesNumber(DishesNumberCriteria.of(actualUser));

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter not set
             => return all user dishes
            """)
    public void getDishesNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(DishesNumberCriteria.of(user));

        Assertions.assertEquals(4, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is set - matching not exists
             => return 0
            """)
    public void getDishesNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                DishesNumberCriteria.of(user).
                        setFilter(MinTagsFilter.of(new Tag("common tag"), new Tag("unknown tag")))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is set - matching exists
             => return correct result
            """)
    public void getDishesNumber5() {
        User user = createAndSaveUser(1);
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                DishesNumberCriteria.of(user).
                        setFilter(MinTagsFilter.of(new Tag("common tag"), new Tag("tag A")))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             criteria is null
             => exception
            """)
    public void getDishes1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getDishes(null),
                DishRepositoryPostgres.class,
                "getDishes",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getDishes2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<Dish> actual = dishRepository.getDishes(
                DishCriteria.of(
                        Pageable.of(2, 0),
                        actualUser
                )
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter not set
             => return correct result
            """)
    public void getDishes3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                DishCriteria.of(
                        Pageable.of(3, 1),
                        user
                )
        );

        Page<Dish> expected = Pageable.of(3, 1).
                createPageMetadata(4).
                createPage(dishes.subList(3, 4));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is set - matching not exists
             => return empty page
            """)
    public void getDishes4() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                DishCriteria.of(
                        Pageable.of(3, 0),
                        user
                ).setFilter(
                        MinTagsFilter.of(new Tag("unknown tag"))
                )
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is set - matching exists
             => return empty page
            """)
    public void getDishes5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                DishCriteria.of(
                        Pageable.of(2, 0),
                        user
                ).setFilter(
                        MinTagsFilter.of(new Tag("common tag"), new Tag("tag B"))
                )
        );

        Page<Dish> expected = Pageable.of(2, 0).
                createPageMetadata(2).
                createPage(dishes.subList(0, 2));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNumberTags(criteria):
             criteria is null
             => exception
            """)
    public void getTagsNumber1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getTagsNumber(null),
                DishRepositoryPostgres.class,
                "getTagsNumber",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNumberTags(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getTagsNumber2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getTagsNumber(
                DishFieldNumberCriteria.of(actualUser)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberTags(criteria):
             user have dishes
             => return correct result
            """)
    public void getTagsNumber3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getTagsNumber(
                DishFieldNumberCriteria.of(user)
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
        AssertUtil.assertServiceException(
                () -> dishRepository.getTags(null),
                DishRepositoryPostgres.class,
                "getTags",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getTags2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<Tag> actual = dishRepository.getTags(
                DishFieldCriteria.of(actualUser, Pageable.of(2, 1))
        );

        Page<Tag> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have dishes
             => return correct result
            """)
    public void getTags3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Tag> actual = dishRepository.getTags(
                DishFieldCriteria.of(user, Pageable.of(2, 1))
        );

        Page<Tag> expected = Pageable.of(2, 1).
                createPageMetadata(7).
                createPage(getAllTags(dishes).subList(2, 4));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNumberUnits(criteria):
             criteria is null
             => exception
            """)
    public void getUnitsNumber1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getUnitsNumber(null),
                DishRepositoryPostgres.class,
                "getUnitsNumber",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getNumberUnits(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getUnitsNumber2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getUnitsNumber(
                DishFieldNumberCriteria.of(actualUser)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberUnits(criteria):
             user have dishes
             => return correct result
            """)
    public void getUnitsNumber3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getUnitsNumber(
                DishFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(3, actual);
    }

    @Test
    @DisplayName("""
            getUnits(criteria):
             criteria is null
             => exception
            """)
    public void getUnits1() {
        AssertUtil.assertServiceException(
                () -> dishRepository.getUnits(null),
                DishRepositoryPostgres.class,
                "getUnits",
                ConstraintType.MISSING_VALUE
        );
    }

    @Test
    @DisplayName("""
            getUnits(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getUnits2() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<String> actual = dishRepository.getUnits(
                DishFieldCriteria.of(actualUser, Pageable.of(2, 1))
        );

        Page<String> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getUnits(criteria):
             user have dishes
             => return correct result
            """)
    public void getUnits3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<String> actual = dishRepository.getUnits(
                DishFieldCriteria.of(user, Pageable.of(2, 1))
        );

        Page<String> expected = Pageable.of(2, 1).
                createPageMetadata(3).
                createPage(getAllUnits(dishes).subList(2, 3));
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
        User user = new User(
                toUUID(userId),
                "User#" + userId,
                "password" + userId,
                "user" + userId + "@mail.com"
        );
        commit(() -> userRepository.save(user));
        return user;
    }

    private Dish createDish(int dishId, User user) {
        return new Dish.Builder().
                setId(toUUID(dishId)).
                setUser(user).
                setName("dish A").
                setUnit("unit A").
                setDescription("description A").
                setImagePath("image path A").
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag A").
                addTag("common tag").
                addIngredient("ingredient 1",
                        OrElseFilter.of(
                                AndFilter.of(
                                        MinTagsFilter.of(new Tag("common tag")),
                                        CategoriesFilter.of("name A"),
                                        ShopsFilter.of("shop A"),
                                        VarietiesFilter.of("variety A"),
                                        ManufacturerFilter.of("manufacturer A")
                                ),
                                MinTagsFilter.of(new Tag("tag B"))
                        ),
                        BigDecimal.TEN).
                addIngredient("ingredient 2",
                        OrElseFilter.of(
                                AndFilter.of(
                                        MinTagsFilter.of(new Tag("value 1")),
                                        CategoriesFilter.of("name A"),
                                        ShopsFilter.of("shop A"),
                                        VarietiesFilter.of("variety A")
                                ),
                                ManufacturerFilter.of("manufacturer B")
                        ),
                        new BigDecimal("2.5")).
                addIngredient("ingredient 3",
                        AndFilter.of(
                                MinTagsFilter.of(new Tag("value 1"), new Tag("value 2")),
                                CategoriesFilter.of("name A"),
                                ShopsFilter.of("shop B"),
                                VarietiesFilter.of("variety B")
                        ),
                        new BigDecimal("0.1")).
                tryBuild();
    }

    private List<Dish> createDishes(User user) {
        ArrayList<Dish> dishes = new ArrayList<>();

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(1)).
                        setUser(user).
                        setName("dish 1").
                        setUnit("unit A").
                        setDescription("description 1").
                        setImagePath("image path 1").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 1").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                OrElseFilter.of(
                                        AndFilter.of(
                                                MinTagsFilter.of(new Tag("common tag")),
                                                CategoriesFilter.of("name A"),
                                                ShopsFilter.of("shop A"),
                                                VarietiesFilter.of("variety A"),
                                                ManufacturerFilter.of("manufacturer A")
                                        ),
                                        MinTagsFilter.of(new Tag("tag B"))
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                OrElseFilter.of(
                                        AndFilter.of(
                                                MinTagsFilter.of(new Tag("value 1")),
                                                CategoriesFilter.of("name A"),
                                                ShopsFilter.of("shop A"),
                                                VarietiesFilter.of("variety A")
                                        ),
                                        ManufacturerFilter.of("manufacturer B")
                                ),
                                new BigDecimal("2.5")).
                        addIngredient("ingredient 3",
                                AndFilter.of(
                                        MinTagsFilter.of(new Tag("value 1"), new Tag("value 2")),
                                        CategoriesFilter.of("name A"),
                                        ShopsFilter.of("shop B"),
                                        VarietiesFilter.of("variety B")
                                ),
                                new BigDecimal("0.1")).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(2)).
                        setUser(user).
                        setName("dish 2").
                        setUnit("unit A").
                        setDescription("description 2").
                        setImagePath("image path 2").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 2").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                OrElseFilter.of(
                                        AndFilter.of(
                                                MinTagsFilter.of(new Tag("common tag")),
                                                CategoriesFilter.of("name A"),
                                                ShopsFilter.of("shop A"),
                                                VarietiesFilter.of("variety A"),
                                                ManufacturerFilter.of("manufacturer A")
                                        ),
                                        MinTagsFilter.of(new Tag("tag B"))
                                ),
                                BigDecimal.TEN).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(3)).
                        setUser(user).
                        setName("dish 3").
                        setUnit("unit B").
                        setDescription("description 3").
                        setImagePath("image path 3").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 3").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                OrElseFilter.of(
                                        MinTagsFilter.of(new Tag("tag B")),
                                        AndFilter.of(
                                                MinTagsFilter.of(new Tag("common tag")),
                                                CategoriesFilter.of("name A"),
                                                ShopsFilter.of("shop A"),
                                                VarietiesFilter.of("variety A"),
                                                ManufacturerFilter.of("manufacturer A")
                                        )
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                OrElseFilter.of(
                                        ShopsFilter.of("shop B"),
                                        ManufacturerFilter.of("manufacturer B")
                                ),
                                new BigDecimal("2.5")).
                        addIngredient("ingredient 3",
                                AndFilter.of(
                                        MinTagsFilter.of(new Tag("value 1"), new Tag("value 2")),
                                        CategoriesFilter.of("name A"),
                                        ShopsFilter.of("shop B"),
                                        VarietiesFilter.of("variety B")
                                ),
                                new BigDecimal("0.1")).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(4)).
                        setUser(user).
                        setName("dish 4").
                        setUnit("unit C").
                        setDescription("description 4").
                        setImagePath("image path 4").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 4").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                OrElseFilter.of(
                                        MinTagsFilter.of(new Tag("tag B")),
                                        AndFilter.of(
                                                ShopsFilter.of("shop C"),
                                                VarietiesFilter.of("variety D")
                                        )
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                OrElseFilter.of(
                                        ShopsFilter.of("shop B"),
                                        ManufacturerFilter.of("manufacturer B")
                                ),
                                new BigDecimal("2.5")).
                        tryBuild()
        );

        return dishes;
    }

    private List<Tag> getAllTags(List<Dish> allDishes) {
        return allDishes.stream().
                flatMap(d -> d.getTags().stream()).
                distinct().
                sorted().
                toList();
    }

    private List<String> getAllUnits(List<Dish> allDishes) {
        return allDishes.stream().
                map(Dish::getUnit).
                distinct().
                sorted().
                toList();
    }

}
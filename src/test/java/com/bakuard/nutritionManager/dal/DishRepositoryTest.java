package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.DishRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.Tag;
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

class DishRepositoryTest {

    private static HikariDataSource dataSource;
    private static ProductRepositoryPostgres productRepository;
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
        AssertUtil.assertValidateException(
                () -> dishRepository.save(null),
                Constraint.NOT_NULL
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
        Dish actual = dishRepository.tryGetById(user.getId(), toUUID(1));

        AssertUtil.assertEquals(expected, actual);
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
        createAndSaveDishes(user);
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
        createAndSaveDishes(user);
        Dish expected = createDish(7, user);

        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.tryGetById(user.getId(), toUUID(7));

        AssertUtil.assertEquals(expected, actual);
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
        createAndSaveDishes(user);
        Dish dish = new Dish.Builder().
                setId(toUUID(7)).
                setUser(user).
                setName("dish 1").
                setServingSize(BigDecimal.TEN).
                setUnit("unit A").
                setDescription("description A").
                setImageUrl("https://nutritionmanager.xyz/dishes/images?id=7").
                setConfig(appConfiguration).
                setRepository(productRepository).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> commit(() -> dishRepository.save(dish)),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
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
        createAndSaveDishes(user);
        Dish dish = createDish(7, user);
        commit(() -> dishRepository.save(dish));

        Dish updatedDish = new Dish.Builder().
                setId(toUUID(7)).
                setUser(user).
                setName("updated dish").
                setServingSize(BigDecimal.TEN).
                setUnit("updated unit").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/dishes/images?updatedImageUrl").
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag 2").
                addTag("2 tag").
                addTag("new tag").
                addTag("tag new").
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(0)).
                                setFilter(
                                        Filter.orElse(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("common tag")),
                                                        Filter.anyCategory("name A"),
                                                        Filter.anyShop("shop A"),
                                                        Filter.anyGrade("variety A"),
                                                        Filter.anyManufacturer("manufacturer A")
                                                ),
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("tag B"))
                                                )
                                        )
                                ).
                                setName("ingredient 1").
                                setQuantity(BigDecimal.TEN).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(1)).
                                setFilter(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.anyCategory("name A", "name B", "name C"),
                                                Filter.anyShop("shop B", "shop A"),
                                                Filter.anyGrade("variety B")
                                        )
                                ).
                                setName("ingredient 3").
                                setQuantity(new BigDecimal("5.4")).
                                setConfig(appConfiguration)
                ).
                tryBuild();
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
        createAndSaveDishes(user);
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish expected = new Dish.Builder().
                setId(toUUID(7)).
                setUser(user).
                setName("updated dish").
                setServingSize(BigDecimal.TEN).
                setUnit("updated unit").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/dishes/images?updatedImageUrl").
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag 2").
                addTag("2 tag").
                addTag("new tag").
                addTag("tag new").
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(0)).
                                setFilter(
                                        Filter.orElse(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("common tag")),
                                                        Filter.anyCategory("name A"),
                                                        Filter.anyShop("shop A"),
                                                        Filter.anyGrade("variety A"),
                                                        Filter.anyManufacturer("manufacturer A")
                                                ),
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("tag B"))
                                                )
                                        )
                                ).
                                setName("ingredient 1").
                                setQuantity(BigDecimal.TEN).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(2)).
                                setFilter(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.anyCategory("name A", "name B", "name C"),
                                                Filter.anyShop("shop B", "shop A"),
                                                Filter.anyGrade("variety B")
                                        )
                                ).
                                setName("ingredient 3").
                                setQuantity(new BigDecimal("5.4")).
                                setConfig(appConfiguration)
                ).
                tryBuild();
        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.tryGetById(user.getId(), toUUID(7));

        AssertUtil.assertEquals(expected, actual);
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
        createAndSaveDishes(user);
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish updatedDish = new Dish.Builder().
                setId(toUUID(7)).
                setUser(user).
                setName("dish 1").
                setServingSize(BigDecimal.TEN).
                setUnit("updated unit").
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/dishes/images?updatedImageUrl").
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag 2").
                addTag("2 tag").
                addTag("new tag").
                addTag("tag new").
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(0)).
                                setFilter(
                                        Filter.orElse(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("common tag")),
                                                        Filter.anyCategory("name A"),
                                                        Filter.anyShop("shop A"),
                                                        Filter.anyGrade("variety A"),
                                                        Filter.anyManufacturer("manufacturer A")
                                                ),
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("tag B"))
                                                )
                                        )
                                ).
                                setName("ingredient 1").
                                setQuantity(BigDecimal.TEN).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(0)).
                                setFilter(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.anyCategory("name A", "name B", "name C"),
                                                Filter.anyShop("shop B", "shop A"),
                                                Filter.anyGrade("variety B")
                                        )
                                ).
                                setName("ingredient 3").
                                setQuantity(new BigDecimal("5.4")).
                                setConfig(appConfiguration)
                ).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> dishRepository.save(updatedDish),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
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
        createAndSaveDishes(user);
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
        createAndSaveDishes(user);
        Dish expected = createDish(7, user);

        commit(() -> dishRepository.save(expected));
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.tryGetById(user.getId(), toUUID(7));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             dishId is null
             => exception
            """)
    public void tryRemove1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryRemove(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             userId is null
             => exception
            """)
    public void tryRemove2() {
        AssertUtil.assertValidateException(
                () -> dishRepository.tryRemove(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             dish with such id not exists in DB
             => exception
            """)
    public void tryRemove3() {
        User user = createAndSaveUser(1);
        createAndSaveDishes(user);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryRemove(user.getId(), toUUID(100)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             dish with such id exists in DB,
             user is not owner of this dish
             => exception
            """)
    public void tryRemove4() {
        User user = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(1, createAndSaveUser(2))));

        AssertUtil.assertValidateException(
                () -> dishRepository.tryRemove(user.getId(), toUUID(1)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             dish with such id exists in DB,
             user is owner of this dish
             => remove dish
            """)
    public void tryRemove5() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(1, user);
        commit(() -> dishRepository.save(expected));

        commit(() -> dishRepository.tryRemove(user.getId(), toUUID(1)));
        Optional<Dish> actual = dishRepository.getById(user.getId(), toUUID(1));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            tryRemove(userId, dishId):
             dish with such id exists in DB,
             user is owner of this dish
             => return removed dish
            """)
    public void tryRemove6() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(1, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.tryRemove(user.getId(), toUUID(1));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getById(userId, dishId):
             dishId is null
             => exception
            """)
    public void getById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.getById(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(userId, dishId):
             userId is null
             => exception
            """)
    public void getById2() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.getById(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(userId, dishId):
             not exists dish with such id
             => return empty Optional
            """)
    public void getById3() {
        User user = createAndSaveUser(1);

        Optional<Dish> actual = dishRepository.getById(user.getId(), toUUID(100));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getById(userId, dishId):
             dish with such id exists in DB,
             user is not owner of this dish
             => return empty Optional
            """)
    public void getById4() {
        User user = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(1, createAndSaveUser(2))));

        Optional<Dish> actual = dishRepository.getById(user.getId(), toUUID(1));

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getById(userId, dishId):
             dish with such id exists in DB,
             user is owner of this dish
             => return correct result
            """)
    public void getById5() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.getById(user.getId(), toUUID(100)).orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryGetById(userId, dishId):
             dishId is null
             => exception
            """)
    public void tryGetById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetById(user.getId(), null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, dishId):
             userId is null
             => exception
            """)
    public void tryGetById2() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetById(null, toUUID(1)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, dishId):
             not exists dish with such id
             => exception
            """)
    public void tryGetById3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetById(user.getId(), toUUID(100)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, dishId):
             exists dish with such id,
             user is not owner of this dish
             => exception
            """)
    public void tryGetById4() {
        User user = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(1, createAndSaveUser(2))));

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetById(user.getId(), toUUID(1)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, dishId):
             exists dish with such id,
             user is owner of this dish
             => return correct result
            """)
    public void tryGetById5() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.tryGetById(user.getId(), toUUID(100));

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
                () -> dishRepository.getByName(user.getId(), null),
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
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.getByName(null, "dish A"),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getByName(userId, name):
             not exists dish with such name
             => return empty Optional
            """)
    public void getByName3() {
        User user = createAndSaveUser(1);

        Optional<Dish> actual =  dishRepository.getByName(user.getId(), "unknown dish");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getByName(name):
             exists dish with such name,
             user is not owner of this dish
             => return empty Optional
            """)
    public void getByName4() {
        User user = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(1, createAndSaveUser(2))));

        Optional<Dish> actual = dishRepository.getByName(user.getId(), "dish A");

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getByName(name):
             exists dish with such name,
             user is owner of this dish
             => return correct result
            """)
    public void getByName5() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.getByName(user.getId(), "dish A").orElseThrow();

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
                () -> dishRepository.tryGetByName(user.getId(), null),
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
                () -> dishRepository.tryGetByName(null, "dish A"),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             not exists dish with such name
             => exception
            """)
    public void tryGetByName3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetByName(user.getId(), "unknown dish"),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             exists dish with such name,
             user is not owner of this dish
             => return correct result
            """)
    public void tryGetByName4() {
        User user = createAndSaveUser(1);
        commit(() -> dishRepository.save(createDish(1, createAndSaveUser(2))));

        AssertUtil.assertValidateException(
                () -> dishRepository.tryGetByName(user.getId(), "dish A"),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }
    
    @Test
    @DisplayName("""
            tryGetByName(userId, name):
             exists dish with such name,
             user is owner of this dish
             => return correct result
            """)
    public void tryGetByName5() {
        User user = createAndSaveUser(1);
        Dish expected = createDish(100, user);
        commit(() -> dishRepository.save(expected));

        Dish actual = dishRepository.tryGetByName(user.getId(), "dish A");

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             criteria is null
             => exception
            """)
    public void getDishesNumber1() {
        AssertUtil.assertValidateException(
                () -> dishRepository.getDishesNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user haven't any dishes,
             filter not set
             => return 0
            """)
    public void getDishesNumber2() {
        User user = createAndSaveUser(1);
        createAndSaveDishes(user);
        User actualUser = createAndSaveUser(2);

        int actual = dishRepository.getDishesNumber(
                new Criteria().setFilter(Filter.user(actualUser.getId()))
        );

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
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(new Criteria().setFilter(Filter.user(user.getId())));

        Assertions.assertEquals(4, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is MinTags - matching not exists
             => return 0
            """)
    public void getDishesNumber4() {
        User user = createAndSaveUser(1);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
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
            getNumberDishes(criteria):
             user have dishes,
             filter is MinTags - matching exists
             => return correct result
            """)
    public void getDishesNumber5() {
        User user = createAndSaveUser(1);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag A"))
                                )
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is Ingredients - matching exists
             => return correct result
            """)
    public void getDishesNumber6() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyIngredient("name A", "name Z")
                                )
                        )
        );

        Assertions.assertEquals(3, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is Ingredients - matching not exists
             => return 0
            """)
    public void getDishesNumber7() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyIngredient("name C", "name D")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is AndFilter - matching exists
                MinTags - matching exists,
                Ingredients - matching exists
             return correct result
            """)
    public void getDishesNumber8() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B")),
                                        Filter.anyIngredient("name B", "name Z")
                                )
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getNumberDishes(criteria):
             user have dishes,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Ingredients - matching not exists
             return 0
            """)
    public void getDishesNumber9() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        createAndSaveDishes(user);

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B")),
                                        Filter.anyIngredient("name C", "name D")
                                )
                        )
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
        AssertUtil.assertValidateException(
                () -> dishRepository.getDishes(null),
                Constraint.NOT_NULL
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
        List<Dish> dishes = createAndSaveDishes(user);
        User actualUser = createAndSaveUser(100);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(actualUser.getId())).
                        setPageable(PageableByNumber.of(2, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Page.empty();
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter not set
             => return dishes in correct state
            """)
    public void getDishes3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = PageableByNumber.of(4, 0).
                createPageMetadata(4, 30).
                createPage(dishes);
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter not set
             => return correct result
            """)
    public void getDishes4() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(3, 1)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = PageableByNumber.of(3, 1).
                createPageMetadata(4, 200).
                createPage(dishes.subList(3, 4));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is MinTags - matching not exists
             => return empty page
            """)
    public void getDishes5() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag"))
                                )
                        ).
                        setPageable(PageableByNumber.of(3, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Page.empty();
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is MinTags - matching exists
             => return full page
            """)
    public void getDishes6() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B"))
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
                createPage(dishes.subList(2, 4));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is Ingredients - matching exists
             => return full page
            """)
    public void getDishes7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyIngredient("name A", "name C", "name D")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = PageableByNumber.of(4, 0).
                createPageMetadata(3, 200).
                createPage(dishes.subList(0, 3));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is Ingredients - matching not exists
             => return empty page
            """)
    public void getDishes8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyIngredient("name C", "name D", "name E")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Page.empty();
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is AndFilter - matching exists
                MinTags - matching exists,
                Ingredients - matching exists
             return full page
            """)
    public void getDishes9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                                        Filter.anyIngredient("name A", "name Z")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = PageableByNumber.of(4, 0).
                createPageMetadata(2, 200).
                createPage(dishes.subList(0, 2));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getDishes(criteria):
             user have dishes,
             filter is AndFilter - matching not exists
                MinTags - matching exists,
                Ingredients - matching not exists
             return empty page
            """)
    public void getDishes10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B")),
                                        Filter.anyIngredient("name C", "name D")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Page.empty();
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNumberTags(criteria):
             criteria is null
             => exception
            """)
    public void getTagsNumber1() {
        AssertUtil.assertValidateException(
                () -> dishRepository.getTagsNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNumberTags(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getTagsNumber2() {
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getTagsNumber(
                new Criteria().setFilter(Filter.user(actualUser.getId()))
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
        createAndSaveDishes(user);

        int actual = dishRepository.getTagsNumber(
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
                () -> dishRepository.getTags(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getTags2() {
        User actualUser = createAndSaveUser(100);

        Page<Tag> actual = dishRepository.getTags(
                new Criteria().
                        setFilter(Filter.user(actualUser.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<Tag> expected = Page.empty();
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
        List<Dish> dishes = createAndSaveDishes(user);

        Page<Tag> actual = dishRepository.getTags(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<Tag> expected = PageableByNumber.of(2, 1).
                createPageMetadata(7, 200).
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
        AssertUtil.assertValidateException(
                () -> dishRepository.getUnitsNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNumberUnits(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getUnitsNumber2() {
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getUnitsNumber(
                new Criteria().setFilter(Filter.user(actualUser.getId()))
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
        createAndSaveDishes(user);

        int actual = dishRepository.getUnitsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
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
        AssertUtil.assertValidateException(
                () -> dishRepository.getUnits(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getUnits(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getUnits2() {
        User actualUser = createAndSaveUser(100);

        Page<String> actual = dishRepository.getUnits(
                new Criteria().
                        setFilter(Filter.user(actualUser.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<String> expected = Page.empty();
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
        List<Dish> dishes = createAndSaveDishes(user);

        Page<String> actual = dishRepository.getUnits(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<String> expected = PageableByNumber.of(2, 1).
                createPageMetadata(3, 200).
                createPage(getAllUnits(dishes).subList(2, 3));
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
                () -> dishRepository.getNamesNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNamesNumber(criteria):
             user haven't any dishes
             => return 0
            """)
    public void getNamesNumber2() {
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getNamesNumber(
                new Criteria().setFilter(Filter.user(actualUser.getId()))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getNamesNumber(criteria):
             user have dishes
             => return correct result
            """)
    public void getNamesNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveDishes(user);

        int actual = dishRepository.getNamesNumber(
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
    public void getNames1() {
        AssertUtil.assertValidateException(
                () -> dishRepository.getNames(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getNames(criteria):
             user haven't any dishes
             => return empty page
            """)
    public void getNames2() {
        User actualUser = createAndSaveUser(100);

        Page<String> actual = dishRepository.getNames(
                new Criteria().
                        setFilter(Filter.user(actualUser.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<String> expected = Page.empty();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getNames(criteria):
             user have dishes
             => return correct result
            """)
    public void getNames3() {
        User user = createAndSaveUser(1);
        List<Dish> dishes = createAndSaveDishes(user);

        Page<String> actual = dishRepository.getNames(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Page<String> expected = PageableByNumber.of(2, 1).
                createPageMetadata(4, 200).
                createPage(getAllNames(dishes).subList(2, 4));
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

    private List<Product> createAndSaveProducts(User user) {
        ArrayList<Product> products = new ArrayList<>();

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(1)).
                        setUser(user).
                        setCategory("name A").
                        setShop("shop A").
                        setGrade("variety A").
                        setManufacturer("manufacturer A").
                        setUnit("unitA").
                        setPrice(new BigDecimal(25)).
                        setPackingSize(new BigDecimal("0.5")).
                        setQuantity(BigDecimal.ZERO).
                        setDescription("some description A").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=1").
                        addTag("common tag").
                        addTag("tag A").
                        addTag("value 1").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(2)).
                        setUser(user).
                        setCategory("name A").
                        setShop("shop A").
                        setGrade("variety A").
                        setManufacturer("manufacturer A").
                        setUnit("unitA").
                        setPrice(new BigDecimal(37)).
                        setPackingSize(BigDecimal.ONE).
                        setQuantity(BigDecimal.ZERO).
                        setDescription("some description B").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=2").
                        addTag("common tag").
                        addTag("tag A").
                        addTag("value 2").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(3)).
                        setUser(user).
                        setCategory("name A").
                        setShop("shop B").
                        setGrade("variety B").
                        setManufacturer("manufacturer A").
                        setUnit("unitA").
                        setPrice(new BigDecimal(45)).
                        setPackingSize(new BigDecimal("1.5")).
                        setQuantity(BigDecimal.ZERO).
                        setDescription("some description C").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=3").
                        addTag("common tag").
                        addTag("tag A").
                        addTag("value 3").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(4)).
                        setUser(user).
                        setCategory("name B").
                        setShop("shop B").
                        setGrade("variety C").
                        setManufacturer("manufacturer A").
                        setUnit("unitA").
                        setPrice(new BigDecimal(60)).
                        setPackingSize(new BigDecimal(2)).
                        setQuantity(new BigDecimal("12.5")).
                        setDescription("some description D").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=4").
                        addTag("common tag").
                        addTag("tag B").
                        addTag("value 4").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(5)).
                        setUser(user).
                        setCategory("name B").
                        setShop("shop C").
                        setGrade("variety C").
                        setManufacturer("manufacturer B").
                        setUnit("unitA").
                        setPrice(new BigDecimal(95)).
                        setPackingSize(new BigDecimal(5)).
                        setQuantity(new BigDecimal("6")).
                        setDescription("some description E").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=5").
                        addTag("common tag").
                        addTag("tag B").
                        addTag("value 5").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(6)).
                        setUser(user).
                        setCategory("name B").
                        setShop("shop C").
                        setGrade("variety D").
                        setManufacturer("manufacturer B").
                        setUnit("unitA").
                        setPrice(new BigDecimal(140)).
                        setPackingSize(BigDecimal.TEN).
                        setQuantity(new BigDecimal("9.2")).
                        setDescription("some description F").
                        setImageUrl("https://nutritionmanager.xyz/products/images?id=6").
                        addTag("common tag").
                        addTag("tag B").
                        addTag("value 6").
                        tryBuild()
        );

        commit(() -> products.forEach(p -> productRepository.save(p)));
        
        return products;
    }

    private Dish createDish(int dishId, User user) {
        return new Dish.Builder().
                setId(toUUID(dishId)).
                setUser(user).
                setName("dish A").
                setServingSize(BigDecimal.ONE).
                setUnit("unit A").
                setDescription("description A").
                setImageUrl("https://nutritionmanager.xyz/dishes/images?id=1").
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
                                setId(toUUID(0)).
                                setFilter(
                                    Filter.orElse(
                                            Filter.and(
                                                    Filter.user(user.getId()),
                                                    Filter.minTags(new Tag("common tag")),
                                                    Filter.anyCategory("name A"),
                                                    Filter.anyShop("shop A"),
                                                    Filter.anyGrade("variety A"),
                                                    Filter.anyManufacturer("manufacturer A")
                                            ),
                                            Filter.and(
                                                    Filter.user(user.getId()),
                                                    Filter.minTags(new Tag("tag B"))
                                            )
                                    )
                                ).
                                setName("ingredient 1").
                                setQuantity(BigDecimal.TEN).
                                setConfig(appConfiguration)
                ).
                addIngredient(
                        new DishIngredient.Builder().
                                setId(toUUID(1)).
                                setFilter(
                                        Filter.orElse(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("value 1")),
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
                                setId(toUUID(2)).
                                setFilter(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("value 1"), new Tag("value 2")),
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

    private List<Dish> createAndSaveDishes(User user) {
        ArrayList<Dish> dishes = new ArrayList<>();

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(1)).
                        setUser(user).
                        setName("dish 1").
                        setServingSize(BigDecimal.ONE).
                        setUnit("unit A").
                        setDescription("description 1").
                        setImageUrl("https://nutritionmanager.xyz/dishes/images?id=1").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 1").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1001)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("common tag")),
                                                                Filter.anyCategory("name A"),
                                                                Filter.anyShop("shop A"),
                                                                Filter.anyGrade("variety A"),
                                                                Filter.anyManufacturer("manufacturer A")
                                                        ),
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("tag B"))
                                                        )
                                                )
                                        ).
                                        setName("ingredient 1").
                                        setQuantity(BigDecimal.TEN).
                                        setConfig(appConfiguration)
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1002)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("value 1")),
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
                                        setId(toUUID(1003)).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("value 1"), new Tag("value 2")),
                                                        Filter.anyCategory("name A"),
                                                        Filter.anyShop("shop B"),
                                                        Filter.anyGrade("variety B")
                                                )
                                        ).
                                        setName("ingredient 3").
                                        setQuantity(new BigDecimal("0.1")).
                                        setConfig(appConfiguration)
                        ).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(2)).
                        setUser(user).
                        setName("dish 2").
                        setServingSize(BigDecimal.ONE).
                        setUnit("unit A").
                        setDescription("description 2").
                        setImageUrl("https://nutritionmanager.xyz/dishes/images?id=2").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 2").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1004)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("common tag")),
                                                                Filter.anyCategory("name A"),
                                                                Filter.anyShop("shop A"),
                                                                Filter.anyGrade("variety A"),
                                                                Filter.anyManufacturer("manufacturer A")
                                                        ),
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("tag B"))
                                                        )
                                                )
                                        ).
                                        setName("ingredient 1").
                                        setQuantity(BigDecimal.TEN).
                                        setConfig(appConfiguration)
                        ).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(3)).
                        setUser(user).
                        setName("dish 3").
                        setServingSize(BigDecimal.ONE).
                        setUnit("unit B").
                        setDescription("description 3").
                        setImageUrl("https://nutritionmanager.xyz/dishes/images?id=3").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 3").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1005)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("tag B"))
                                                        ),
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("common tag")),
                                                                Filter.anyCategory("name A"),
                                                                Filter.anyShop("shop A"),
                                                                Filter.anyGrade("variety A"),
                                                                Filter.anyManufacturer("manufacturer A")
                                                        )
                                                )
                                        ).
                                        setName("ingredient 1").
                                        setQuantity(BigDecimal.TEN).
                                        setConfig(appConfiguration)
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1006)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.anyShop("shop B")
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
                                        setId(toUUID(1007)).
                                        setFilter(
                                                Filter.and(
                                                        Filter.user(user.getId()),
                                                        Filter.minTags(new Tag("value 1"), new Tag("value 2")),
                                                        Filter.anyCategory("name A"),
                                                        Filter.anyShop("shop B"),
                                                        Filter.anyGrade("variety B")
                                                )
                                        ).
                                        setName("ingredient 3").
                                        setQuantity(new BigDecimal("0.1")).
                                        setConfig(appConfiguration)
                        ).
                        tryBuild()
        );

        dishes.add(
                new Dish.Builder().
                        setId(toUUID(4)).
                        setUser(user).
                        setName("dish 4").
                        setServingSize(BigDecimal.ONE).
                        setUnit("unit C").
                        setDescription("description 4").
                        setImageUrl("https://nutritionmanager.xyz/dishes/images?id=4").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 4").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1008)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.minTags(new Tag("tag B"))
                                                        ),
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.anyShop("shop C"),
                                                                Filter.anyGrade("variety D")
                                                        )
                                                )
                                        ).
                                        setName("ingredient 1").
                                        setQuantity(BigDecimal.TEN).
                                        setConfig(appConfiguration)
                        ).
                        addIngredient(
                                new DishIngredient.Builder().
                                        setId(toUUID(1009)).
                                        setFilter(
                                                Filter.orElse(
                                                        Filter.and(
                                                                Filter.user(user.getId()),
                                                                Filter.anyShop("variety C")
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
                        tryBuild()
        );

        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        
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

    private List<String> getAllNames(List<Dish> allDishes) {
        return allDishes.stream().
                map(Dish::getName).
                distinct().
                sorted().
                toList();
    }

}
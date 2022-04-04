package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.DishRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Constraint;
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
                DishRepositoryPostgres.class,
                "save",
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
        Dish actual = dishRepository.getById(toUUID(1));

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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);
        dish.setName("dish 1");

        AssertUtil.assertValidateException(
                () -> commit(() -> dishRepository.save(dish)),
                DishRepositoryPostgres.class,
                "save",
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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish updatedDish = new Dish(dish);
        updatedDish.setName("New name");
        updatedDish.setServingSize(new BigDecimal("0.75"));
        updatedDish.setUnit("new unit");
        updatedDish.setDescription("new description");
        updatedDish.setImageUrl("https://newDishImage");
        updatedDish.removeIngredient("ingredient 1");
        updatedDish.putIngredient("ingredient 4", Filter.anyCategory("category Z"), BigDecimal.TEN);
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
        expected.setServingSize(new BigDecimal("0.75"));
        expected.setUnit("new unit");
        expected.setDescription("new description");
        expected.setImageUrl("https://newDishImage");
        expected.removeIngredient("ingredient 1");
        expected.putIngredient(
                "ingredient 4",
                Filter.and(Filter.user(user), Filter.anyCategory("category Z")),
                BigDecimal.TEN
        );
        expected.removeTag(new Tag("tag A"));
        expected.addTag(new Tag("tag Z"));
        commit(() -> dishRepository.save(expected));
        Dish actual = dishRepository.getById(toUUID(7));

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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        Dish dish = createDish(7, user);

        commit(() -> dishRepository.save(dish));
        Dish updatedDish = new Dish(dish);
        updatedDish.setName("dish 1");

        AssertUtil.assertValidateException(
                () -> dishRepository.save(updatedDish),
                DishRepositoryPostgres.class,
                "save",
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
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            remove(dishId):
             dishId is null
             => exception
            """)
    public void remove1() {
        AssertUtil.assertValidateException(
                () -> dishRepository.remove(null),
                DishRepositoryPostgres.class,
                "remove",
                Constraint.NOT_NULL
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

        AssertUtil.assertValidateException(
                () -> dishRepository.remove(toUUID(100)),
                DishRepositoryPostgres.class,
                "remove",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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

        AssertUtil.assertValidateException(
                () -> dishRepository.getById(toUUID(100)),
                DishRepositoryPostgres.class,
                "getById",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getById(dishId):
             dishId is null
             => exception
            """)
    public void getById1() {
        AssertUtil.assertValidateException(
                () -> dishRepository.getById(null),
                DishRepositoryPostgres.class,
                "getById",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(dishId):
             not exists dish with such id
             => exception
            """)
    public void getById2() {
        AssertUtil.assertValidateException(
                () -> dishRepository.getById(toUUID(100)),
                DishRepositoryPostgres.class,
                "getById",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
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
                DishRepositoryPostgres.class,
                "getDishesNumber",
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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(2);

        int actual = dishRepository.getDishesNumber(
                new Criteria().setFilter(Filter.user(actualUser))
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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(new Criteria().setFilter(Filter.user(user)));

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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        commit(() -> createProducts(user).forEach(p -> productRepository.save(p)));
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        commit(() -> createProducts(user).forEach(p -> productRepository.save(p)));
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        commit(() -> createProducts(user).forEach(p -> productRepository.save(p)));
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        commit(() -> createProducts(user).forEach(p -> productRepository.save(p)));
        commit(() -> createDishes(user).forEach(d -> dishRepository.save(d)));

        int actual = dishRepository.getDishesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
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
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(actualUser)).
                        setPageable(Pageable.of(2, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
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
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(user)).
                        setPageable(Pageable.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Assertions.assertAll(
                () -> AssertUtil.assertEquals(dishes.get(0), actual.get(0)),
                () -> AssertUtil.assertEquals(dishes.get(1), actual.get(1)),
                () -> AssertUtil.assertEquals(dishes.get(2), actual.get(2))
        );
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
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(Filter.user(user)).
                        setPageable(Pageable.of(3, 1)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.of(3, 1).
                createPageMetadata(4, 200).
                createPage(dishes.subList(3, 4));
        Assertions.assertEquals(expected, actual);
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
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("unknown tag"))
                                )
                        ).
                        setPageable(Pageable.of(3, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
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
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B"))
                                )
                        ).
                        setPageable(Pageable.of(2, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(dishes.subList(2, 4));
        Assertions.assertEquals(expected, actual);
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
        List<Product> products = createProducts(user);
        List<Dish> dishes = createDishes(user);
        commit(() -> products.forEach(p -> productRepository.save(p)));
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.anyIngredient("name A", "name C", "name D")
                                )
                        ).
                        setPageable(Pageable.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.of(4, 0).
                createPageMetadata(3, 200).
                createPage(dishes.subList(0, 3));
        Assertions.assertEquals(expected, actual);
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
        List<Product> products = createProducts(user);
        List<Dish> dishes = createDishes(user);
        commit(() -> products.forEach(p -> productRepository.save(p)));
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.anyIngredient("name C", "name D", "name E")
                                )
                        ).
                        setPageable(Pageable.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
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
        List<Product> products = createProducts(user);
        List<Dish> dishes = createDishes(user);
        commit(() -> products.forEach(p -> productRepository.save(p)));
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                                        Filter.anyIngredient("name A", "name Z")
                                )
                        ).
                        setPageable(Pageable.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.of(4, 0).
                createPageMetadata(2, 200).
                createPage(dishes.subList(0, 2));
        Assertions.assertEquals(expected, actual);
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
        List<Product> products = createProducts(user);
        List<Dish> dishes = createDishes(user);
        commit(() -> products.forEach(p -> productRepository.save(p)));
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));

        Page<Dish> actual = dishRepository.getDishes(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("common tag"), new Tag("tag B")),
                                        Filter.anyIngredient("name C", "name D")
                                )
                        ).
                        setPageable(Pageable.of(4, 0)).
                        setSort(Sort.dishDefaultSort())
        );

        Page<Dish> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
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
                DishRepositoryPostgres.class,
                "getTagsNumber",
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
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getTagsNumber(
                new Criteria().setFilter(Filter.user(actualUser))
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
                new Criteria().setFilter(Filter.user(user))
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
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<Tag> actual = dishRepository.getTags(
                new Criteria().
                        setFilter(Filter.user(actualUser)).
                        setPageable(Pageable.of(2, 1))
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
                new Criteria().
                        setFilter(Filter.user(user)).
                        setPageable(Pageable.of(2, 1))
        );

        Page<Tag> expected = Pageable.of(2, 1).
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
                DishRepositoryPostgres.class,
                "getUnitsNumber",
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
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        int actual = dishRepository.getUnitsNumber(
                new Criteria().setFilter(Filter.user(actualUser))
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
                new Criteria().setFilter(Filter.user(user))
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
        User user = createAndSaveUser(1);
        List<Dish> dishes = createDishes(user);
        commit(() -> dishes.forEach(d -> dishRepository.save(d)));
        User actualUser = createAndSaveUser(100);

        Page<String> actual = dishRepository.getUnits(
                new Criteria().
                        setFilter(Filter.user(actualUser)).
                        setPageable(Pageable.of(2, 1))
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
                new Criteria().
                        setFilter(Filter.user(user)).
                        setPageable(Pageable.of(2, 1))
        );

        Page<String> expected = Pageable.of(2, 1).
                createPageMetadata(3, 200).
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

    private List<Product> createProducts(User user) {
        ArrayList<Product> products = new ArrayList<>();

        products.add(
                new Product.Builder().
                        setAppConfiguration(appConfiguration).
                        setId(toUUID(1)).
                        setUser(user).
                        setCategory("name A").
                        setShop("shop A").
                        setVariety("variety A").
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
                        setVariety("variety A").
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
                        setVariety("variety B").
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
                        setVariety("variety C").
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
                        setVariety("variety C").
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
                        setVariety("variety D").
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
                setImagePath("https://nutritionmanager.xyz/products/images?id=1").
                setConfig(appConfiguration).
                setRepository(productRepository).
                addTag("tag A").
                addTag("common tag").
                addIngredient("ingredient 1",
                        Filter.orElse(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                ),
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("tag B"))
                                )
                        ),
                        BigDecimal.TEN).
                addIngredient("ingredient 2",
                        Filter.orElse(
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("value 1")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A")
                                ),
                                Filter.and(
                                        Filter.user(user),
                                        Filter.anyManufacturer("manufacturer B")
                                )
                        ),
                        new BigDecimal("2.5")).
                addIngredient("ingredient 3",
                        Filter.and(
                                Filter.user(user),
                                Filter.minTags(new Tag("value 1"), new Tag("value 2")),
                                Filter.anyCategory("name A"),
                                Filter.anyShop("shop B"),
                                Filter.anyGrade("variety B")
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
                        setServingSize(BigDecimal.ONE).
                        setUnit("unit A").
                        setDescription("description 1").
                        setImagePath("https://nutritionmanager.xyz/products/images?id=1").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 1").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("common tag")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("tag B"))
                                        )
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("value 1")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A")
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyManufacturer("manufacturer B")
                                        )
                                ),
                                new BigDecimal("2.5")).
                        addIngredient("ingredient 3",
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("value 1"), new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop B"),
                                        Filter.anyGrade("variety B")
                                ),
                                new BigDecimal("0.1")).
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
                        setImagePath("https://nutritionmanager.xyz/products/images?id=2").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 2").
                        addTag("tag A").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("common tag")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("tag B"))
                                        )
                                ),
                                BigDecimal.TEN).
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
                        setImagePath("https://nutritionmanager.xyz/products/images?id=3").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 3").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("tag B"))
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("common tag")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyShop("shop B")
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyManufacturer("manufacturer B")
                                        )
                                ),
                                new BigDecimal("2.5")).
                        addIngredient("ingredient 3",
                                Filter.and(
                                        Filter.user(user),
                                        Filter.minTags(new Tag("value 1"), new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop B"),
                                        Filter.anyGrade("variety B")
                                ),
                                new BigDecimal("0.1")).
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
                        setImagePath("https://nutritionmanager.xyz/products/images?id=4").
                        setConfig(appConfiguration).
                        setRepository(productRepository).
                        addTag("tag 4").
                        addTag("tag B").
                        addTag("common tag").
                        addIngredient("ingredient 1",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.minTags(new Tag("tag B"))
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyShop("shop C"),
                                                Filter.anyGrade("variety D")
                                        )
                                ),
                                BigDecimal.TEN).
                        addIngredient("ingredient 2",
                                Filter.orElse(
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyShop("variety C")
                                        ),
                                        Filter.and(
                                                Filter.user(user),
                                                Filter.anyManufacturer("manufacturer B")
                                        )
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
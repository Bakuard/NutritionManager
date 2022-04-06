package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.DishRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ImageRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Filter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.*;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

class ImageRepositoryPostgresTest {

    private static HikariDataSource dataSource;
    private static ImageRepository imageRepository;
    private static UserRepository userRepository;
    private static ProductRepository productRepository;
    private static DishRepository dishRepository;
    //private static MenuRepository menuRepository;
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

        imageRepository = new ImageRepositoryPostgres(dataSource);
        userRepository = new UserRepositoryPostgres(dataSource);
        productRepository = new ProductRepositoryPostgres(dataSource, appConfiguration);
        dishRepository = new DishRepositoryPostgres(dataSource, appConfiguration, (ProductRepositoryPostgres) productRepository);
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
            addImageUrl(userId, imageHash, imageUrl):
             userId and imageHash - unique
             => save data
            """)
    void addImageUrl1() {
        User user = createAndSaveUser(1);
        URL expected = createUrl("https://somepath");
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                expected)
        );

        URL actual = imageRepository.getImageUrl(user.getId(), "d1921aa0ca3c1146a01520c04e6caa9e");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            addImageUrl(userId, imageHash, imageUrl):
             userId and imageHash - not unique
             => don't throw any exception
            """)
    void addImageUrl2() {
        User user = createAndSaveUser(1);
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                createUrl("https://somepath"))
        );

        Assertions.assertDoesNotThrow(() ->
            commit(() -> imageRepository.addImageUrl(
                    user.getId(),
                    "d1921aa0ca3c1146a01520c04e6caa9e",
                    createUrl("https://somepath"))
            )
        );
    }

    @Test
    @DisplayName("""
            addImageUrl(userId, imageHash, imageUrl):
             userId and imageHash - not unique
             => do nothing
            """)
    void addImageUrl3() {
        User user = createAndSaveUser(1);
        URL expected = createUrl("https://somepath");
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                expected)
        );

        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                createUrl("https://otherURl"))
        );
        URL actual = imageRepository.getImageUrl(user.getId(), "d1921aa0ca3c1146a01520c04e6caa9e");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getImageUrl(userId, imageHash):
             userId and imageHash - not exists
             => return null
            """)
    void getImageUrl1() {
        User user = createAndSaveUser(1);

        URL actual = imageRepository.getImageUrl(
                toUUID(2),
                "889b88c75a5b0236e6fa67848fdce656"
        );

        Assertions.assertNull(actual);
    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are not images in DB
             => return empty list
            """)
    void getAndRemoveUnusedImages1() {
        List<String> actual = imageRepository.getAndRemoveUnusedImages();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images in DB,
             there are not unused images
             => return empty list, don't remove anything
            """)
    void getAndRemoveUnusedImages2() {
        User user = createAndSaveUser(1);
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                createUrl("https://somepath1"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "889b88c75a5b0236e6fa67848fdce656",
                createUrl("https://somepath2"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "622ed5d9d4c45a0ce5b4dd9a7b80fd74",
                createUrl("https://somepath3"))
        );
        commit(() ->
                createProducts(user,
                        "https://somepath1",
                        "https://somepath2",
                        "https://somepath3").
                        forEach(p -> productRepository.save(p))
        );

        List<String> actual = imageRepository.getAndRemoveUnusedImages();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images used for products,
             there are unused images
             => return all unused images, remove all unused images
            """)
    void getAndRemoveUnusedImages3() {
        User user = createAndSaveUser(1);
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                createUrl("https://somepath1"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "889b88c75a5b0236e6fa67848fdce656",
                createUrl("https://somepath2"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "622ed5d9d4c45a0ce5b4dd9a7b80fd74",
                createUrl("https://somepath3"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "6e209648b5cce6ebd95561788d1cbfb8",
                createUrl("https://somepath4"))
        );
        commit(() ->
                createProducts(user,
                        "https://somepath1",
                        "https://somepath2").
                        forEach(p -> productRepository.save(p))
        );

        List<String> actual = commit(() -> imageRepository.getAndRemoveUnusedImages());

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        List.of("622ed5d9d4c45a0ce5b4dd9a7b80fd74",
                                "6e209648b5cce6ebd95561788d1cbfb8"),
                        actual
                ),
                () -> Assertions.assertNull(
                        imageRepository.getImageUrl(user.getId(), "622ed5d9d4c45a0ce5b4dd9a7b80fd74")
                ),
                () -> Assertions.assertNull(
                        imageRepository.getImageUrl(user.getId(), "6e209648b5cce6ebd95561788d1cbfb8")
                ),
                () -> Assertions.assertEquals(
                        createUrl("https://somepath1"),
                        imageRepository.getImageUrl(user.getId(), "d1921aa0ca3c1146a01520c04e6caa9e")
                ),
                () -> Assertions.assertEquals(
                        createUrl("https://somepath2"),
                        imageRepository.getImageUrl(user.getId(), "889b88c75a5b0236e6fa67848fdce656")
                )
        );
    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images used for dishes,
             there are unused images
             => return all unused images, remove all unused images
            """)
    void getAndRemoveUnusedImages4() {
        User user = createAndSaveUser(1);
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "d1921aa0ca3c1146a01520c04e6caa9e",
                createUrl("https://somepath1"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "889b88c75a5b0236e6fa67848fdce656",
                createUrl("https://somepath2"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "622ed5d9d4c45a0ce5b4dd9a7b80fd74",
                createUrl("https://somepath3"))
        );
        commit(() -> imageRepository.addImageUrl(
                user.getId(),
                "6e209648b5cce6ebd95561788d1cbfb8",
                createUrl("https://somepath4"))
        );
        commit(() ->
                createDishes(user,
                        "https://somepath1",
                        "https://somepath2").
                        forEach(d -> dishRepository.save(d))
        );

        List<String> actual = commit(() -> imageRepository.getAndRemoveUnusedImages());

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        List.of("622ed5d9d4c45a0ce5b4dd9a7b80fd74",
                                "6e209648b5cce6ebd95561788d1cbfb8"),
                        actual
                ),
                () -> Assertions.assertNull(
                        imageRepository.getImageUrl(user.getId(), "622ed5d9d4c45a0ce5b4dd9a7b80fd74")
                ),
                () -> Assertions.assertNull(
                        imageRepository.getImageUrl(user.getId(), "6e209648b5cce6ebd95561788d1cbfb8")
                ),
                () -> Assertions.assertEquals(
                        createUrl("https://somepath1"),
                        imageRepository.getImageUrl(user.getId(), "d1921aa0ca3c1146a01520c04e6caa9e")
                ),
                () -> Assertions.assertEquals(
                        createUrl("https://somepath2"),
                        imageRepository.getImageUrl(user.getId(), "889b88c75a5b0236e6fa67848fdce656")
                )
        );
    }

    /*@Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images used for menus,
             there are unused images
             => return all unused images, remove all unused images
            """)
    void getAndRemoveUnusedImages5() {

    }*/


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

    private List<Product> createProducts(User user, String... imageUrls) {
        List<Product> products = new ArrayList<>();

        for(int i = 0; i < imageUrls.length; i++) {
            products.add(
                    new Product.Builder().
                            setAppConfiguration(appConfiguration).
                            setId(toUUID(i)).
                            setUser(user).
                            setCategory("name#" + i).
                            setShop("shop#" + i).
                            setGrade("variety#" + i).
                            setManufacturer("manufacturer#" + i).
                            setUnit("unitA").
                            setPrice(BigDecimal.ZERO).
                            setPackingSize(BigDecimal.ONE).
                            setQuantity(BigDecimal.ZERO).
                            setDescription("some description A").
                            setImageUrl(imageUrls[i]).
                            addTag("tag 1").
                            addTag("tag 2").
                            addTag("tag 3").
                            tryBuild()
            );
        }

        return products;
    }

    private List<Dish> createDishes(User user, String... imageUrls) {
        List<Dish> dishes = new ArrayList<>();

        for(int i = 0; i < imageUrls.length; i++) {
            dishes.add(
                    new Dish.Builder().
                            setId(toUUID(i)).
                            setUser(user).
                            setName("dish#" + i).
                            setServingSize(BigDecimal.ONE).
                            setUnit("unit A").
                            setDescription("description A").
                            setImagePath(imageUrls[i]).
                            setConfig(appConfiguration).
                            setRepository(productRepository).
                            addTag("tag A").
                            addTag("common tag").
                            addIngredient("ingredient 1",
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
                                    ),
                                    BigDecimal.TEN).
                            tryBuild()
            );
        }

        return dishes;
    }

    private User createAndSaveUser(int userId) {
        User user = new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
        commit(() -> userRepository.save(user));
        return user;
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private URL createUrl(String url) {
        try {
            return new URL(url);
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
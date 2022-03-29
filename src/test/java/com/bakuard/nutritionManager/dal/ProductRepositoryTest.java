package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.criteria.products.*;
import com.bakuard.nutritionManager.dal.impl.ProductRepositoryPostgres;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
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
import java.util.*;
import java.util.function.Supplier;

class ProductRepositoryTest {

    private static HikariDataSource dataSource;
    private static ProductRepository repository;
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
        repository = new ProductRepositoryPostgres(dataSource, appConfiguration);
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
    @DisplayName("save(product): product is null => exception")
    void save1() {
        AssertUtil.assertValidateException(
                () -> repository.save(null),
                ProductRepositoryPostgres.class,
                "save",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("save(product): no products in DB => return true")
    void save2() {
        User user = createAndSaveUser(1);
        Product product = createProduct(1, user).tryBuild();

        Assertions.assertTrue(commit(() -> repository.save(product)));
    }

    @Test
    @DisplayName("save(product): no products in DB => add product")
    void save3() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user).tryBuild();

        commit(() -> repository.save(expected));
        Product actual = repository.getById(expected.getId());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("save(product): there are products in DB, product id not exists => return true")
    void save4() {
        User user1 = createAndSaveUser(1);
        User user2 = createAndSaveUser(2);
        Product product1 = createProduct(1, user1).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user1).tryBuild();
        Product addedProduct = createProduct(3, user2).tryBuild();

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        Assertions.assertTrue(commit(() -> repository.save(addedProduct)));
    }

    @Test
    @DisplayName("save(product): there are products in DB, product id not exists => add product")
    void save5() {
        User user1 = createAndSaveUser(1);
        User user2 = createAndSaveUser(2);
        Product product1 = createProduct(1, user1).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user1).tryBuild();
        Product expected = createProduct(3, user2).tryBuild();

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));
        commit(() -> repository.save(expected));

        Product actual = repository.getById(toUUID(3));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id not exists,
             user has a product with the same productContext and other id in the database
             => exception
            """)
    void save6() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user).tryBuild();
        Product addedProduct = createProduct(3, user).tryBuild();
        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        AssertUtil.assertValidateException(
                () -> commit(() ->repository.save(addedProduct)),
                ProductRepositoryPostgres.class,
                "save",
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state was changed
             => return true
            """)
    void save7() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user).setPrice(BigDecimal.TEN).tryBuild();
        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        Product updatedProduct = new Product(product1);
        updatedProduct.setImageUrl("https://nutritionmanager.xyz/products/images?id=112");
        boolean isSaved = commit(() -> repository.save(updatedProduct));

        Assertions.assertTrue(isSaved);
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state was changed
             => update product
            """)
    void save8() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user).setPrice(BigDecimal.TEN).tryBuild();
        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        Product expected = new Product(product1);
        expected.setImageUrl("https://nutritionmanager.xyz/products/images?id=112");
        commit(() -> repository.save(expected));

        Assertions.assertTrue(expected.equalsFullState(repository.getById(toUUID(1))));
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state was changed,
             user has a product with the same productContext and other in the database
             => exception
            """)
    void save9() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).
                setPrice(new BigDecimal("115.12")).
                tryBuild();
        Product product2 = createProduct(2, user).
                setImageUrl("https://nutritionmanager.xyz/products/images?id=unique").
                setDescription("unique description").
                setQuantity(BigDecimal.TEN).
                tryBuild();
        Product updatedProduct = new Product(product2);
        updatedProduct.setContext(product1.getContext());

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.save(updatedProduct)),
                ProductRepositoryPostgres.class,
                "save",
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state wasn't changed
             => return false
            """)
    void save10() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user).setUnit("unitB").tryBuild();

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        Assertions.assertFalse(
                repository.save(product1)
        );
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state wasn't changed
             => don't update product
            """)
    void save11() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user).setPrice(new BigDecimal("115.12")).tryBuild();
        Product product2 = createProduct(2, user).setUnit("unitB").tryBuild();
        Product expectedProduct = new Product(product1);

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));
        commit(() -> repository.save(product1));

        Assertions.assertTrue(
                expectedProduct.equalsFullState(repository.getById(toUUID(1)))
        );
    }

    @Test
    @DisplayName("remove(productId): productId is null => exception")
    void remove1() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.remove(null)),
                ProductRepositoryPostgres.class,
                "remove",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("remove(productId): product with such id not exists in DB => exception")
    void remove2() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.remove(toUUID(10))),
                ProductRepositoryPostgres.class,
                "remove",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("remove(productId): product with such id exists in DB => remove product")
    void remove3() {
        User user = createAndSaveUser(1);
        Product product = createProduct(1, user).tryBuild();

        commit(() -> repository.save(product));
        commit(() -> repository.remove(toUUID(1)));

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.getById(toUUID(1))),
                ProductRepositoryPostgres.class,
                "getById",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("remove(productId): product with such id exists in DB => return removed product")
    void remove4() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user).tryBuild();

        commit(() -> repository.save(expected));
        Product actual = commit(() -> repository.remove(toUUID(1)));

        Assertions.assertTrue(expected.equalsFullState(actual));
    }

    @Test
    @DisplayName("getById(productId): productId is null => exception")
    void getById1() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.getById(null)),
                ProductRepositoryPostgres.class,
                "getById",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("getById(productId): not exists product with such id => exception")
    void getById2() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.getById(toUUID(256))),
                ProductRepositoryPostgres.class,
                "getById",
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("getById(productId): exists product with such id => return product")
    void getById3() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user).tryBuild();
        commit(() -> repository.save(expected));

        Product actual = commit(() -> repository.getById(toUUID(1)));

        Assertions.assertTrue(expected.equalsFullState(actual));
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             criteria is null
             => exception
            """)
    void getProductsNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getProductsNumber(null),
                ProductRepositoryPostgres.class,
                "getProductsNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getProductsNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = true,
             filter not specified
            """)
    void getProductsNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).setOnlyFridge(true)
        );

        Assertions.assertEquals(3, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter not specified
            """)
    void getProductsNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).setOnlyFridge(false)
        );

        Assertions.assertEquals(6, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user haven't any products,
             filter specified
            """)
    void getProductsNumber5() {
        User user1 = createAndSaveUser(1);
        User user2 = createAndSaveUser(2);
        commit(() -> createProducts(user1).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user2).
                        setFilter(Filter.minTags(new Tag("common tag")))
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlFridge = true,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
             matches exist
            """)
    void getProductsNumber6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety C")
                                )
                        )
        );

        Assertions.assertEquals(1, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is MinTags,
             matches exist
            """)
    void getProductsNumber7() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.minTags(new Tag("common tag"))
                        )
        );

        Assertions.assertEquals(6, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = true,
             filter is ShopsConstraint,
             matches exist
            """)
    void getProductsNumber8() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.anyShop("shop C")
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                CategoryConstraint - matches exist,
                VarietiesConstraints - matches exist,
             matches exist
            """)
    void getProductsNumber9() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.anyCategory("name B"),
                                        Filter.anyVariety("variety C")
                                )
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches not exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber10() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("this tag not exists")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety C")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches not exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber11() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("this name not exists"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety C")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches not exist,
                VarietiesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber12() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("this shop not exists"),
                                        Filter.anyVariety("variety C")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches not exist,
             matches not exist
            """)
    void getProductsNumber13() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("this variety not exists")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber14() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety D")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
                ManufacturerConstraint - matches exists
             matches not exist
            """)
    void getProductsNumber15() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety D"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
                ManufacturerConstraint - matches exists
             matches exist
            """)
    void getProductsNumber16() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is AndConstraint. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                VarietiesConstraints - matches exist,
                ManufacturerConstraint - matches not exists
            """)
    void getProductsNumber17() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer Z")
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = false,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return correct result
            """)
    void getProductsNumber18() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             onlyFridge = true,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return correct result
            """)
    void getProductsNumber19() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyVariety("variety C"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Assertions.assertEquals(1, actual);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match not exists,
                    ManufacturerConstraint - match not exists
             => return 0
            """)
    void getProductsNumber20() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getProductsNumber(
                ProductsNumberCriteria.of(user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyVariety("unknown variety"),
                                                Filter.anyManufacturer("unknown manufacturer")
                                        )
                                )
                        )
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             criteria is null
             => exception
            """)
    void getProducts1() {
        AssertUtil.assertValidateException(
                () -> repository.getProducts(null),
                ProductRepositoryPostgres.class,
                "getProducts",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user haven't any products
             => return empty page
            """)
    void getProducts2() {
        User user1 = createAndSaveUser(1);
        User user2 = createAndSaveUser(2);
        List<Product> products = createProducts(user1);
        commit(() -> products.forEach(p -> repository.save(p)));
        Page<Product> expected = Pageable.firstEmptyPage();

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(6, 0),
                        user2
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             get full page,
             filter not specified
            """)
    void getProducts3() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));
        Page<Product> expected = Pageable.of(5, 0).
                createPageMetadata(6, 200).
                createPage(products.subList(0, 5));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(5, 0),
                        user
                ).setOnlyFridge(false)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             get partial page
             filter not specified
            """)
    void getProducts4() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));
        Page<Product> expected = Pageable.of(5, 1).
                createPageMetadata(6, 200).
                createPage(products.subList(5, 6));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(5, 1),
                        user
                ).setOnlyFridge(false)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             get full page
             filter not specified
            """)
    void getProducts5() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));
        Page<Product> expected = Pageable.of(2, 0).
                createPageMetadata(3, 200).
                createPage(products.subList(3, 5));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 0),
                        user
                ).setOnlyFridge(true)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             get partial page
             filter not specified
            """)
    void getProducts6() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));
        Page<Product> expected = Pageable.of(2, 1).
                createPageMetadata(3, 200).
                createPage(products.subList(5, 6));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 1),
                        user
                ).setOnlyFridge(true)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user haven't any products,
             filter is MinTags
             => return empty page
            """)
    void getProducts7() {
        User user = createAndSaveUser(1);

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(6, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(Filter.minTags(new Tag("common tag")))
        );

        Assertions.assertEquals(Pageable.firstEmptyPage(), actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = full,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists
            """)
    void getProducts8() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.of(1, 0).
                createPageMetadata(1, 200).
                createPage(products.subList(5, 6));
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(1, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("tag B"), new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety D")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = empty,
             filter is MinTags - match exists
            """)
    void getProducts9() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(6, 0),
                        user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.minTags(new Tag("tag A"), new Tag("common tag"))
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = full,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists
            """)
    void getProducts10() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(products.subList(0, 2));
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("tag A"), new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                VarietiesConstraint - match exists
            """)
    void getProducts11() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("tag Z"), new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop Z"),
                                        Filter.anyVariety("variety A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = partial,
             filter is AndConstraint. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match exists
            """)
    void getProducts12() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.of(3, 0).
                createPageMetadata(2, 200).
                createPage(products.subList(4, 6));
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(3, 0),
                        user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = empty,
             filter is AndConstraint. Operands:
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                VarietiesConstraint - match exists
            """)
    void getProducts13() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 0),
                        user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.anyCategory("name Z"),
                                        Filter.anyShop("shop Z"),
                                        Filter.anyVariety("variety A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = empty,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match not exists
            """)
    void getProducts14() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(2, 0),
                        user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety Z")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = partial,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists
            """)
    void getProducts15() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(products.subList(0, 2));
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(3, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match not exists
            """)
    void getProducts16() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                        Pageable.of(3, 0),
                        user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common Z")),
                                        Filter.anyCategory("name Z"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety Z")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match exists
             match not exists
            """)
    void getProducts17() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(6, 0),
                                user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyVariety("variety D"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = full,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match exists
             match exists
            """)
    void getProducts18() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(products.subList(0, 2));
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(2, 0),
                                user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match not exists
            """)
    void getProducts19() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        Page<Product> expected = Pageable.firstEmptyPage();
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(2, 0),
                                user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.and(
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer Z")
                                )
                        )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = full,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts20() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(5, 0),
                                user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown name"),
                                                Filter.anyShop("unknown shop"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Page<Product> expected = Pageable.of(5, 0).
                createPageMetadata(2, 200).
                createPage(products.subList(0, 2));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = true,
             pageable = full,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts21() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(5, 0),
                                user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyVariety("variety C"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Page<Product> expected = Pageable.of(5, 0).
                createPageMetadata(1, 200).
                createPage(products.subList(3, 4));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             onlyFridge = false,
             pageable = full,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    CategoryConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts22() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(5, 0),
                                user).
                        setOnlyFridge(false).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.anyCategory("name B"),
                                                Filter.anyManufacturer("manufacturer B")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        ).setProductSort(
                                Sort.products().desc("price")
                        )
        );

        Page<Product> expected = Pageable.of(5, 0).
                createPageMetadata(4, 200).
                createPage(
                        List.of(
                                products.get(5),
                                products.get(4),
                                products.get(1),
                                products.get(0)
                        )
                );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             pageable = full,
             filter is OrElse. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match not exists,
                    ManufacturerConstraint - match not exists
             => return empty page
            """)
    void getProducts23() {
        User user = createAndSaveUser(1);
        List<Product> products = createProducts(user);
        commit(() -> products.forEach(p -> repository.save(p)));

        Page<Product> actual = repository.getProducts(
                ProductCriteria.of(
                                Pageable.of(5, 0),
                                user).
                        setOnlyFridge(true).
                        setFilter(
                                Filter.orElse(
                                        Filter.and(
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyVariety("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyVariety("unknown variety"),
                                                Filter.anyManufacturer("unknown manufacturer")
                                        )
                                )
                        )
        );

        Page<Product> expected = Pageable.firstEmptyPage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             criteria is null
             => exception
            """)
    void getTagsNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getTagsNumber(null),
                ProductRepositoryPostgres.class,
                "getTagsNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getTagsNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getTagsNumber(ProductFieldNumberCriteria.of(user));

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             user have some products,
             productName not specified
             => return correct result
            """)
    void getTagsNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getTagsNumber(ProductFieldNumberCriteria.of(user));

        Assertions.assertEquals(9, actual);
    }

    @Test
    @DisplayName("""
            getTagsNumber(criteria):
             user have some products,
             productName specified
             => return correct result
            """)
    void getTagsNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getTagsNumber(
                ProductFieldNumberCriteria.of(user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(5, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             criteria is null
             => exception
            """)
    void getTags1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> repository.getTags(null),
                ProductRepositoryPostgres.class,
                "getTags",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user haven't any products
             => return empty page
            """)
    void getTags2() {
        User user = createAndSaveUser(1);

        Page<Tag> actual = repository.getTags(
                ProductFieldCriteria.of(
                        Pageable.of(5, 0),
                        user
                )
        );

        Assertions.assertEquals(Pageable.firstEmptyPage(), actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some product,
             pageable is full,
             productName not specified
             => return full page
            """)
    void getTags3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<Tag> expected = Pageable.of(5, 0).
                createPageMetadata(9, 200).
                createPage(createTags().subList(0, 5));

        Page<Tag> actual = repository.getTags(
                ProductFieldCriteria.of(
                    Pageable.of(5, 0),
                    user
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some product,
             pageable is partial,
             productName not specified
             => return partial page
            """)
    void getTags4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<Tag> expected = Pageable.of(4, 2).
                createPageMetadata(9, 200).
                createPage(createTags().subList(8, 9));

        Page<Tag> actual = repository.getTags(
                ProductFieldCriteria.of(
                    Pageable.of(4, 2),
                    user
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some product,
             pageable is full,
             productName specified
             => return full page
            """)
    void getTags5() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<Tag> expected = Pageable.of(5, 0).
                createPageMetadata(5, 200).
                createPage(List.of(
                        new Tag("common tag"),
                        new Tag("tag A"),
                        new Tag("value 1"),
                        new Tag("value 2"),
                        new Tag("value 3")
                ));

        Page<Tag> actual = repository.getTags(
                ProductFieldCriteria.of(
                        Pageable.of(5, 0),
                        user
                ).setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some product,
             pageable is partial,
             productName specified
             => return partial page
            """)
    void getTags6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<Tag> expected = Pageable.of(4, 1).
                createPageMetadata(5, 200).
                createPage(List.of(
                        new Tag("value 3")
                ));

        Page<Tag> actual = repository.getTags(
                ProductFieldCriteria.of(
                        Pageable.of(4, 1),
                        user
                ).setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getShopsNumber(criteria):
             criteria is null
             => exception
            """)
    void getShopsNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getShopsNumber(null),
                ProductRepositoryPostgres.class,
                "getShopsNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getShopsNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getShopsNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getShopsNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getShopsNumber(criteria):
             user have some products,
             productName not specified
             => return correct result
            """)
    void getShopsNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getShopsNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(3, actual);
    }

    @Test
    @DisplayName("""
            getShopsNumber(criteria):
             user have some products,
             productName specified
             => return correct result
            """)
    void getShopsNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getShopsNumber(
                ProductFieldNumberCriteria.of(user).setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             criteria is null
             => exception
            """)
    void getShops1() {
        AssertUtil.assertValidateException(
                () -> repository.getShops(null),
                ProductRepositoryPostgres.class,
                "getShops",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user haven't any products
             => return empty page
            """)
    void getShops2() {
        User user = createAndSaveUser(1);
        Page<String> expected = Pageable.firstEmptyPage();

        Page<String> actual = repository.getShops(
                ProductFieldCriteria.of(Pageable.of(5, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             pageable is full,
             productName not specified
            """)
    void getShops3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(3, 0).
                createPageMetadata(3, 200).
                createPage(List.of("shop A", "shop B", "shop C"));

        Page<String> actual = repository.getShops(
                ProductFieldCriteria.of(Pageable.of(3, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             pageable is partial,
             productName not specified
            """)
    void getShops4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(2, 1).
                createPageMetadata(3, 200).
                createPage(List.of("shop C"));

        Page<String> actual = repository.getShops(
                ProductFieldCriteria.of(Pageable.of(2, 1), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             pageable is full,
             productName specified
            """)
    void getShops5() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("shop A", "shop B"));

        Page<String> actual = repository.getShops(
                ProductFieldCriteria.of(Pageable.of(2, 0), user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             pageable is partial,
             productName specified
            """)
    void getShops6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("shop A", "shop B"));

        Page<String> actual = repository.getShops(
                ProductFieldCriteria.of(Pageable.of(5, 0), user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarietiesNumber(criteria):
             criteria is null
             => exception
            """)
    void getVarietiesNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getVarietiesNumber(null),
                ProductRepositoryPostgres.class,
                "getVarietiesNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getVarietiesNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getVarietiesNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getVarietiesNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getVarietiesNumber(criteria):
             user have some products,
             productName not specified
             => return correct result
            """)
    void getVarietiesNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getVarietiesNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(4, actual);
    }

    @Test
    @DisplayName("""
            getVarietiesNumber(criteria):
             user have some products,
             productName specified
             => return correct result
            """)
    void getVarietiesNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getVarietiesNumber(
                ProductFieldNumberCriteria.of(user).setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             criteria is null
             => exception
            """)
    void getVarieties1() {
        AssertUtil.assertValidateException(
                () -> repository.getVarieties(null),
                ProductRepositoryPostgres.class,
                "getVarieties",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user haven't any products
             => return empty page
            """)
    void getVarieties2() {
        User user = createAndSaveUser(1);
        Page<String> expected = Pageable.firstEmptyPage();

        Page<String> actual = repository.getVarieties(
                ProductFieldCriteria.of(Pageable.of(5, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user have some products,
             pageable is full,
             productName not specified
            """)
    void getVarieties3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(4, 0).
                createPageMetadata(4, 200).
                createPage(List.of("variety A", "variety B", "variety C", "variety D"));

        Page<String> actual = repository.getVarieties(
                ProductFieldCriteria.of(Pageable.of(4, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user have some products,
             pageable is partial,
             productName not specified
            """)
    void getVarieties4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(3, 1).
                createPageMetadata(4, 200).
                createPage(List.of("variety D"));

        Page<String> actual = repository.getVarieties(
                ProductFieldCriteria.of(Pageable.of(3, 1), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user have some products,
             pageable is full,
             productName specified
            """)
    void getVarieties5() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("variety A", "variety B"));

        Page<String> actual = repository.getVarieties(
                ProductFieldCriteria.of(Pageable.of(2, 0), user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user have some products,
             pageable is partial,
             productName specified
            """)
    void getVarieties6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(4, 0).
                createPageMetadata(2, 200).
                createPage(List.of("variety A", "variety B"));

        Page<String> actual = repository.getVarieties(
                ProductFieldCriteria.of(Pageable.of(4, 0), user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getCategoriesNumber(criteria):
             criteria is null
             => exception
            """)
    void getCategoriesNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getCategoriesNumber(null),
                ProductRepositoryPostgres.class,
                "getCategoriesNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getCategoriesNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getCategoriesNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getCategoriesNumber(
                ProductCategoryNumberCriteria.of(user)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getCategoriesNumber(criteria):
             user have some products
             => return correct result
            """)
    void getCategoriesNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getCategoriesNumber(
                ProductCategoryNumberCriteria.of(user)
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             criteria is null
             => exception
            """)
    void getCategories1() {
        AssertUtil.assertValidateException(
                () -> repository.getCategories(null),
                ProductRepositoryPostgres.class,
                "getCategories",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user haven't any products
             => return empty page
            """)
    void getCategories2() {
        User user = createAndSaveUser(1);
        Page<String> expected = Pageable.firstEmptyPage();

        Page<String> actual = repository.getCategories(
                ProductCategoryCriteria.of(
                        Pageable.of(2, 0), user
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             pageable is partial
             => return correct result
            """)
    void getCategories3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("name A", "name B"));

        Page<String> actual = repository.getCategories(
                ProductCategoryCriteria.of(
                        Pageable.of(5, 0), user
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             pageable is full
             => return correct result
            """)
    void getCategories4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(1, 1).
                createPageMetadata(2, 200).
                createPage(List.of("name B"));

        Page<String> actual = repository.getCategories(
                ProductCategoryCriteria.of(
                        Pageable.of(1, 1), user
                )
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getManufacturersNumber(criteria):
             criteria is null
             => exception
            """)
    void getManufacturersNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getManufacturersNumber(null),
                ProductRepositoryPostgres.class,
                "getManufacturersNumber",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getManufacturersNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getManufacturersNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getManufacturersNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            getManufacturersNumber(criteria):
             user have some products,
             productName not specified
             => return correct result
            """)
    void getManufacturersNumber3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getManufacturersNumber(
                ProductFieldNumberCriteria.of(user)
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getManufacturersNumber(criteria):
             user have some products,
             productName specified
             => return correct result
            """)
    void getManufacturersNumber4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        int actual = repository.getManufacturersNumber(
                ProductFieldNumberCriteria.of(user).setProductCategory(Filter.anyCategory("name B"))
        );

        Assertions.assertEquals(2, actual);
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             criteria is null
             => exception
            """)
    void getManufacturers1() {
        AssertUtil.assertValidateException(
                () -> repository.getManufacturers(null),
                ProductRepositoryPostgres.class,
                "getManufacturers",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user haven't any products
             => return empty page
            """)
    void getManufacturers2() {
        User user = createAndSaveUser(1);
        Page<String> expected = Pageable.firstEmptyPage();

        Page<String> actual = repository.getManufacturers(
                ProductFieldCriteria.of(Pageable.of(5, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             pageable is full,
             productName not specified
            """)
    void getManufacturers3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

        Page<String> actual = repository.getManufacturers(
                ProductFieldCriteria.of(Pageable.of(2, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             pageable is partial,
             productName not specified
            """)
    void getManufacturers4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

        Page<String> actual = repository.getManufacturers(
                ProductFieldCriteria.of(Pageable.of(5, 0), user)
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getVarieties(criteria):
             user have some products,
             pageable is full,
             productName specified
            """)
    void getManufacturers5() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

        Page<String> actual = repository.getManufacturers(
                ProductFieldCriteria.of(Pageable.of(2, 0), user).
                        setProductCategory(Filter.anyCategory("name B"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             pageable is partial,
             productName specified
            """)
    void getManufacturers6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));
        Page<String> expected = Pageable.of(5, 0).
                createPageMetadata(1, 200).
                createPage(List.of("manufacturer A"));

        Page<String> actual = repository.getManufacturers(
                ProductFieldCriteria.of(Pageable.of(5, 0), user).
                        setProductCategory(Filter.anyCategory("name A"))
        );

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             criteria is null
             => exception
            """)
    void getProductsSum1() {
        AssertUtil.assertValidateException(
                () -> repository.getProductsSum(null),
                ProductRepositoryPostgres.class,
                "getProductsSum",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum2() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("unknown tag")),
                                Filter.anyShop("unknown shop"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum3() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.anyCategory("unknown name"),
                                Filter.anyShop("shop A"),
                                Filter.anyVariety("variety A"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                CategoryConstraint - match exists,
                VarietiesConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum4() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("unknown tag")),
                                Filter.anyCategory("name A"),
                                Filter.anyVariety("variety A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                VarietiesConstraint - match not exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum5() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("tag A")),
                                Filter.anyCategory("name A"),
                                Filter.anyShop("unknown shop"),
                                Filter.anyVariety("unknown variety"),
                                Filter.anyManufacturer("unknown manufacturer")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum6() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("tag A")),
                                Filter.anyShop("shop A"),
                                Filter.anyVariety("unknown variety")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum7() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("tag A")),
                                Filter.anyCategory("unknown name")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum8() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("unknown tag")),
                                Filter.anyVariety("variety A"),
                                Filter.anyManufacturer("unknown manufacturer")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum9() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.anyCategory("name A"),
                                Filter.anyShop("shop A"),
                                Filter.anyVariety("variety A")
                        )
                )
        );

        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(new BigDecimal(62), actual.get());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                VarietiesConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum10() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("tag A")),
                                Filter.anyCategory("unknown name"),
                                Filter.anyShop("unknown shop"),
                                Filter.anyVariety("variety A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                VarietiesConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum11() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("unknown tag")),
                                Filter.anyCategory("unknown name"),
                                Filter.anyShop("shop A"),
                                Filter.anyVariety("unknown variety"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum12() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("tag A")),
                                Filter.anyCategory("name A"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(new BigDecimal(107), actual.get());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                VarietiesConstraint - match exists,
                ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum13() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.anyVariety("variety A"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(new BigDecimal(62), actual.get());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                VarietiesConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum14() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.anyCategory("name A"),
                                Filter.anyShop("unknown shop"),
                                Filter.anyVariety("unknown variety"),
                                Filter.anyManufacturer("manufacturer A")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is ShopsConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum15() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.anyShop("shop A")
                )
        );

        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(new BigDecimal(62), actual.get());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndConstraint. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum16() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.and(
                                Filter.minTags(new Tag("unknown tag")),
                                Filter.anyCategory("unknown name"),
                                Filter.anyShop("unknown shop"),
                                Filter.anyManufacturer("unknown manufacturer")
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is OrElse. Operands:
                first operands is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operands is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum17() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.orElse(
                                Filter.and(
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyCategory("unknown category"),
                                        Filter.anyShop("unknown shops"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                ),
                                Filter.and(
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
                )
        );

        Assertions.assertTrue(actual.isPresent());
        AssertUtil.assertEquals(new BigDecimal(62), actual.get());
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is OrElse. Operands:
                first operands is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    VarietiesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operands is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    VarietiesConstraint - match not exists,
                    ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum18() {
        User user = createAndSaveUser(1);
        commit(() -> createProducts(user).forEach(p -> repository.save(p)));

        Optional<BigDecimal> actual = repository.getProductsSum(
                ProductSumCriteria.of(
                        user,
                        Filter.orElse(
                                Filter.and(
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyCategory("unknown category"),
                                        Filter.anyShop("unknown shops"),
                                        Filter.anyVariety("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                ),
                                Filter.and(
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyVariety("unknown variety"),
                                        Filter.anyManufacturer("unknown manufacturer")
                                )
                        )
                )
        );

        Assertions.assertTrue(actual.isEmpty());
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

    private Product.Builder createProduct(int productId, User user) {
        return new Product.Builder().
                setAppConfiguration(appConfiguration).
                setId(toUUID(productId)).
                setUser(user).
                setCategory("name#" + user.getName()).
                setShop("shop#" + user.getName()).
                setVariety("variety#" + user.getName()).
                setManufacturer("manufacturer#" + user.getName()).
                setUnit("unitA").
                setPrice(BigDecimal.ZERO).
                setPackingSize(BigDecimal.ONE).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description A").
                setImageUrl("https://nutritionmanager.xyz/products/images?id=1").
                addTag("tag 1").
                addTag("tag 2").
                addTag("tag 3");
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
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

    private List<Tag> createTags() {
        ArrayList<Tag> tags = new ArrayList<>();

        tags.add(new Tag("common tag"));
        tags.add(new Tag("tag A"));
        tags.add(new Tag("tag B"));
        tags.add(new Tag("value 1"));
        tags.add(new Tag("value 2"));
        tags.add(new Tag("value 3"));
        tags.add(new Tag("value 4"));
        tags.add(new Tag("value 5"));
        tags.add(new Tag("value 6"));

        return tags;
    }

}
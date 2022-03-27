package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.*;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class ImageRepositoryImplTest {

    private static HikariDataSource dataSource;
    private static JwsBlackListRepository repository;
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

        repository = new JwsBlackListPostgres(dataSource);
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

    }

    @Test
    @DisplayName("""
            addImageUrl(userId, imageHash, imageUrl):
             userId and imageHash - not unique
             => don't throw any exception, do nothing
            """)
    void addImageUrl2() {

    }

    @Test
    @DisplayName("""
            getImageUrl(userId, imageHash):
             userId and imageHash - not exists
             => return null
            """)
    void getImageUrl1() {

    }

    @Test
    @DisplayName("""
            getImageUrl(userId, imageHash):
             userId and imageHash - exists
             => return URL
            """)
    void getImageUrl2() {

    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are not images in DB
             => return empty list
            """)
    void getAndRemoveUnusedImages1() {

    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images in DB,
             there are not unused images
             => return empty list, don't remove anything
            """)
    void getAndRemoveUnusedImages2() {

    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images used for products,
             there are unused images
             => return all unused images, remove all unused images
            """)
    void getAndRemoveUnusedImages3() {

    }

    @Test
    @DisplayName("""
            getAndRemoveUnusedImages():
             there are images used for dishes,
             there are unused images
             => return all unused images, remove all unused images
            """)
    void getAndRemoveUnusedImages4() {

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

}
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.Action;
import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.JwsBlackListPostgres;

import com.bakuard.nutritionManager.validation.Constraint;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

class JwsBlackListPostgresTest {

    private static HikariDataSource dataSource;
    private static JwsBlackListRepository repository;
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
    @DisplayName("addToBlackList(tokenId, expired): tokenId is null => exception")
    public void addToBlackList1() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.addToBlackList(null, LocalDateTime.now().plusDays(2))),
                JwsBlackListPostgres.class,
                "addToBlackList",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("addToBlackList(tokenId, expired): expired is null => exception")
    public void addToBlackList2() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.addToBlackList(toUUID(1), null)),
                JwsBlackListPostgres.class,
                "addToBlackList",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired < current time
             => return false
            """)
    public void addToBlackList3() {
        boolean actual = commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().minusDays(1)));

        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired = current time
             => return false
            """)
    public void addToBlackList4() {
        boolean actual = commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now()));

        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             tokenId already added to black list
             => return false
            """)
    public void addToBlackList5() {
        commit(() ->repository.addToBlackList(toUUID(1), LocalDateTime.now().plusDays(5)));

        boolean actual = commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().plusDays(3)));

        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired < current time
             => doesn't add token to black list
            """)
    public void addToBlackList6() {
        commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().minusDays(1)));

        Assertions.assertFalse(repository.inBlackList(toUUID(1)));
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired = current time
             => doesn't add token to black list
            """)
    public void addToBlackList7() {
        commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now()));

        Assertions.assertFalse(repository.inBlackList(toUUID(1)));
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             tokenId not contains in black list,
             expired > current time
             => return true
            """)
    public void addToBlackList8() {
        boolean actual = commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().plusDays(3)));

        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             tokenId not contains in black list,
             expired > current time
             => add tokenId to black list
            """)
    public void addToBlackList9() {
        commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().plusDays(3)));

        Assertions.assertTrue(commit(() -> repository.inBlackList(toUUID(1))));
    }

    @Test
    @DisplayName("removeAllExpired(deadline): black list is empty => return 0")
    public void removeAllExpired1() {
        int actual =commit(() -> repository.removeAllExpired(LocalDateTime.now()));

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            removeAllExpired(deadline):
             deadline is null
             => exception
            """)
    public void removeAllExpired2() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.removeAllExpired(null)),
                JwsBlackListPostgres.class,
                "removeAllExpired",
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            removeAllExpired(deadline):
             all tokens expiration date > deadline
             => return 0
            """)
    public void removeAllExpired3() {
        LocalDateTime now = LocalDateTime.now();
        commit(() -> {
            repository.addToBlackList(toUUID(1), now.plusDays(1));
            repository.addToBlackList(toUUID(2), now.plusDays(2));
            repository.addToBlackList(toUUID(3), now.plusDays(3));
        });

        int actual = commit(() -> repository.removeAllExpired(now));

        Assertions.assertEquals(0, actual);
    }

    @Test
    @DisplayName("""
            removeAllExpired(deadline):
             all tokens expiration date > deadline
             => doesn't remove any token
            """)
    public void removeAllExpired4() {
        LocalDateTime now = LocalDateTime.now();
        commit(() -> {
            repository.addToBlackList(toUUID(1), now.plusDays(1));
            repository.addToBlackList(toUUID(2), now.plusDays(2));
            repository.addToBlackList(toUUID(3), now.plusDays(3));
        });

        commit(() -> repository.removeAllExpired(now));

        Assertions.assertAll(
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(1))),
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(2))),
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(3)))
        );
    }

    @Test
    @DisplayName("""
            removeAllExpired(deadline):
             there are tokens with expiration date < deadline
             => return correct number of removed tokens
            """)
    public void removeAllExpired5() {
        LocalDateTime now = LocalDateTime.now();
        commit(() -> {
            repository.addToBlackList(toUUID(1), now.plusDays(1));
            repository.addToBlackList(toUUID(2), now.plusDays(2));
            repository.addToBlackList(toUUID(3), now.plusDays(3));
            repository.addToBlackList(toUUID(4), now.plusDays(5));
            repository.addToBlackList(toUUID(5), now.plusDays(6));
            repository.addToBlackList(toUUID(6), now.plusDays(7));
        });

        int actual = commit(() -> repository.removeAllExpired(now.plusDays(4)));

        Assertions.assertEquals(3, actual);
    }

    @Test
    @DisplayName("""
            removeAllExpired(deadline):
             there are tokens with expiration date < deadline
             => remove these tokens
            """)
    public void removeAllExpired6() {
        LocalDateTime now = LocalDateTime.now();
        commit(() -> {
            repository.addToBlackList(toUUID(1), now.plusDays(1));
            repository.addToBlackList(toUUID(2), now.plusDays(2));
            repository.addToBlackList(toUUID(3), now.plusDays(3));
            repository.addToBlackList(toUUID(4), now.plusDays(5));
            repository.addToBlackList(toUUID(5), now.plusDays(6));
            repository.addToBlackList(toUUID(6), now.plusDays(7));
        });

        commit(() -> repository.removeAllExpired(now.plusDays(4)));

        Assertions.assertAll(
                () -> Assertions.assertFalse(repository.inBlackList(toUUID(1))),
                () -> Assertions.assertFalse(repository.inBlackList(toUUID(2))),
                () -> Assertions.assertFalse(repository.inBlackList(toUUID(3))),
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(4))),
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(5))),
                () -> Assertions.assertTrue(repository.inBlackList(toUUID(6)))
        );
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

}
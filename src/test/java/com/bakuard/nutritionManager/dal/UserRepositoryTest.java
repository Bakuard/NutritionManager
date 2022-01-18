package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.UnknownUserException;
import com.bakuard.nutritionManager.model.exceptions.UserAlreadyExistsException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class UserRepositoryTest {

    private static HikariDataSource dataSource;
    private static UserRepository repository;
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

        repository = new UserRepositoryPostgres(dataSource);
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
    @DisplayName("save(user): user is null => exception")
    public void save1() {
        Assertions.assertThrows(MissingValueException.class, () -> repository.save(null));
    }

    @Test
    @DisplayName("save(user): no user in DB => return true")
    public void save2() {
        User user = createDefaultUser(1);

        Assertions.assertTrue(repository.save(user));
    }

    @Test
    @DisplayName("save(user): no user in DB => add user")
    public void save3() {
        User expected = createDefaultUser(1);

        commit(() -> repository.save(expected));

        User actual = repository.getById(toUUID(1));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("save(user): there are users in DB, user id not exists => return true")
    public void save4() {
        User user1 = createDefaultUser(1);
        User user2 = createDefaultUser(2);
        User addedUser = createDefaultUser(3);
        repository.save(user1);
        repository.save(user2);

        Assertions.assertTrue(repository.save(addedUser));
    }

    @Test
    @DisplayName("save(user): there are users in DB, user id not exists => add user")
    public void save5() {
        User user1 = createDefaultUser(1);
        User user2 = createDefaultUser(2);
        User addedUser = createDefaultUser(3);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        commit(() -> repository.save(addedUser));

        User actualUser = repository.getById(toUUID(3));
        Assertions.assertEquals(addedUser, actualUser);
    }

    @Test
    @DisplayName("save(user): there is user with same name => exception")
    public void save6() {
        User user1 = createDefaultUser(1);
        commit(() -> repository.save(user1));

        User addedUser = createDefaultUser(2);
        addedUser.setName("User1");

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(addedUser));
    }

    @Test
    @DisplayName("save(user): there is user with same password => exception")
    public void save7() {
        User user1 = createDefaultUser(1);
        commit(() -> repository.save(user1));

        User addedUser = new User(
                toUUID(2),
                "user2",
                user1.getPasswordHash(),
                "email",
                user1.getSalt()
        );

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(addedUser));
    }

    @Test
    @DisplayName("save(user): there is user with same email => exception")
    public void save8() {
        User user1 = createDefaultUser(1);
        commit(() -> repository.save(user1));

        User addedUser = new User(
                toUUID(2),
                "user2",
                "passwordHash",
                user1.getEmail(),
                user1.getSalt()
        );

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(addedUser));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed
             => return true
            """)
    public void save9() {
        User user = createDefaultUser(1);
        repository.save(user);

        User updatedUser = new User(user);
        updatedUser.setName("new name");
        boolean isSaved = repository.save(updatedUser);

        Assertions.assertTrue(isSaved);
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed
             => update user
            """)
    public void save10() {
        User user = createDefaultUser(1);
        commit(() -> repository.save(user));

        User expected = new User(user);
        expected.setName("new name");
        commit(() -> repository.save(expected));

        Assertions.assertEquals(expected, repository.getById(toUUID(1)));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed,
             there is user in DB with same name
             => exception
            """)
    public void save11() {
        User user1 = createDefaultUser(1);
        User user2 = createDefaultUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User(user1);
        expected.setName("User2");

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(expected));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed,
             there is user in DB with same password
             => exception
            """)
    public void save12() {
        User user1 = createDefaultUser(1);
        User user2 = createDefaultUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User(
                toUUID(3),
                "user3",
                user1.getPasswordHash(),
                "email",
                user1.getSalt()
        );

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(expected));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed,
             there is user in DB with same email
             => exception
            """)
    public void save13() {
        User user1 = createDefaultUser(1);
        User user2 = createDefaultUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User(
                toUUID(3),
                "user3",
                "passwordHash",
                user1.getEmail(),
                user1.getSalt()
        );

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(expected));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state wasn't changed
             => return false
            """)
    public void save14() {
        User user = createDefaultUser(1);
        commit(() -> repository.save(user));

        Assertions.assertFalse(repository.save(user));
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state wasn't changed
             => don't update user
            """)
    public void save15() {
        User user = createDefaultUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        commit(() -> repository.save(user));

        User actual = repository.getById(toUUID(1));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById(userId): userId is null => exception")
    public void getById1() {
        Assertions.assertThrows(UnknownUserException.class, () -> repository.getById(null));
    }

    @Test
    @DisplayName("getById(userId): not exists user with such id => exception")
    public void getById2() {
        User user = createDefaultUser(1);
        repository.save(user);

        Assertions.assertThrows(UnknownUserException.class, () -> repository.getById(toUUID(2)));
    }

    @Test
    @DisplayName("getById(userId): exists user with such id => return user")
    public void getById3() {
        User user = createDefaultUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        User actual = repository.getById(toUUID(1));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getByName(name):
             name is null
             => exception
            """)
    public void getByName1() {
        Assertions.assertThrows(
                UnknownUserException.class,
                () -> repository.getByName(null)
        );
    }

    @Test
    @DisplayName("""
            getByName(name):
             not exists user with such name
             => exception
            """)
    public void getByName2() {
        User user = createDefaultUser(1);
        commit(() -> repository.save(user));

        Assertions.assertThrows(
                UnknownUserException.class,
                () -> repository.getByName("some user")
        );
    }

    @Test
    @DisplayName("""
            getByName(name):
             exists user with such name
             => return user
            """)
    public void getByName3() {
        User expected = createDefaultUser(1);
        commit(() -> repository.save(expected));

        User actual = repository.getByName("User1");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            getByEmail(email):
             email is null
             => exception
            """)
    public void getByEmail1() {
        Assertions.assertThrows(
                UnknownUserException.class,
                () -> repository.getByEmail(null)
        );
    }

    @Test
    @DisplayName("""
            getByEmail(email):
             not exists uer with such email
             => exception
            """)
    public void getByEmail2() {
        User user = createDefaultUser(1);
        commit(() -> repository.save(user));

        Assertions.assertThrows(
                UnknownUserException.class,
                () -> repository.getByEmail("newEmail@mail.com")
        );
    }

    @Test
    @DisplayName("""
            getByEmail(email):
             exists user with such email
             => return user
            """)
    public void getByEmail3() {
        User expected = createDefaultUser(1);
        commit(() -> repository.save(expected));

        User actual = repository.getByEmail("user1@mail.com");

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

    private User createDefaultUser(int id) {
        return new User(toUUID(id),
                "User" + id,
                "password" + id,
                "user" + id + "@mail.com");
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.DBTestConfig;
import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.impl.UserRepositoryPostgres;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Constraint;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@SpringBootTest(classes = DBTestConfig.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@TestPropertySource(locations = "classpath:application.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private AppConfigData appConfiguration;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            JdbcTestUtils.deleteFromTables(jdbcTemplate,
                    "UsedImages", "JwsBlackList",
                    "MenuItems", "DishIngredients", "MenuTags", "DishTags", "ProductTags",
                    "Menus", "Dishes", "Products", "Users");
            transactionManager.commit(status);
        } catch(RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    @Test
    @DisplayName("save(user): user is null => exception")
    public void save1() {
        AssertUtil.assertValidateException(
                () -> repository.save(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("save(user): no user in DB => return true")
    public void save2() {
        User user = createUser(1);

        Assertions.assertTrue(repository.save(user));
    }

    @Test
    @DisplayName("save(user): no user in DB => add user")
    public void save3() {
        User expected = createUser(1);

        commit(() -> repository.save(expected));

        User actual = repository.tryGetById(toUUID(1));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("save(user): there are users in DB, user id not exists => return true")
    public void save4() {
        User user1 = createUser(1);
        User user2 = createUser(2);
        User addedUser = createUser(3);
        repository.save(user1);
        repository.save(user2);

        Assertions.assertTrue(repository.save(addedUser));
    }

    @Test
    @DisplayName("save(user): there are users in DB, user id not exists => add user")
    public void save5() {
        User user1 = createUser(1);
        User user2 = createUser(2);
        User expected = createUser(3);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        commit(() -> repository.save(expected));

        User actual = repository.tryGetById(toUUID(3));
        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("save(user): there is user with same name => exception")
    public void save6() {
        User user1 = createUser(1);
        commit(() -> repository.save(user1));

        User addedUser = createUser(2);
        addedUser.setName("User1");

        AssertUtil.assertValidateException(
                () -> repository.save(addedUser),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("save(user): there is user with same password => exception")
    public void save7() {
        User user1 = createUser(1);
        commit(() -> repository.save(user1));

        User addedUser = new User.LoadBuilder().
                setId(toUUID(2)).
                setName("user2").
                setEmail("email").
                setPasswordHash(user1.getPasswordHash()).
                setSalt(user1.getSalt()).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> repository.save(addedUser),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("save(user): there is user with same email => exception")
    public void save8() {
        User user1 = createUser(1);
        commit(() -> repository.save(user1));

        User addedUser = new User.LoadBuilder().
                setId(toUUID(2)).
                setName("user2").
                setEmail(user1.getEmail()).
                setSalt(user1.getSalt()).
                setPasswordHash("passwordHash").
                tryBuild();

        AssertUtil.assertValidateException(
                () -> repository.save(addedUser),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state was changed
             => return true
            """)
    public void save9() {
        User user = createUser(1);
        repository.save(user);

        User expected = new User(user);
        expected.setName("new name");
        expected.setPassword("new password");
        expected.setEmail("new email");
        boolean isSaved = repository.save(expected);

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
        User user = createUser(1);
        commit(() -> repository.save(user));

        User expected = new User(user);
        expected.setName("new name");
        expected.setPassword("new password");
        expected.setEmail("new email");
        commit(() -> repository.save(expected));

        User actual =  repository.tryGetById(toUUID(1));
        AssertUtil.assertEquals(expected, actual);
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
        User user1 = createUser(1);
        User user2 = createUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User(user1);
        expected.setName("User2");

        AssertUtil.assertValidateException(
                () -> repository.save(expected),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
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
        User user1 = createUser(1);
        User user2 = createUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User.LoadBuilder().
                setId(toUUID(3)).
                setName("user3").
                setEmail("email").
                setSalt(user1.getSalt()).
                setPasswordHash(user1.getPasswordHash()).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> repository.save(expected),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
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
        User user1 = createUser(1);
        User user2 = createUser(2);
        commit(() -> repository.save(user1));
        commit(() -> repository.save(user2));

        User expected = new User.LoadBuilder().
                setId(toUUID(3)).
                setName("user3").
                setEmail(user1.getEmail()).
                setPasswordHash("passwordHash").
                setSalt(user1.getSalt()).
                tryBuild();

        AssertUtil.assertValidateException(
                () -> repository.save(expected),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(user):
             user already saved in DB,
             user state wasn't changed
             => return false
            """)
    public void save14() {
        User user = createUser(1);
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
        User user = createUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        commit(() -> repository.save(user));

        User actual = repository.tryGetById(toUUID(1));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById(userId): userId is null => exception")
    public void getById1() {
        AssertUtil.assertValidateException(
                () -> repository.getById(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("getById(userId): not exists user with such id => empty optional")
    public void getById2() {
        User user = createUser(1);
        repository.save(user);

        Optional<User> actual = repository.getById(toUUID(2));
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("getById(userId): exists user with such id => return user")
    public void getById3() {
        User user = createUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        User actual = repository.getById(toUUID(1)).orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("tryGetById(userId): userId is null => exception")
    public void tryGetById1() {
        AssertUtil.assertValidateException(
                () -> repository.tryGetById(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("tryGetById(userId): not exists user with such id => exception")
    public void tryGetById2() {
        User user = createUser(1);
        repository.save(user);

        AssertUtil.assertValidateException(
                () -> repository.tryGetById(toUUID(2)),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("tryGetById(userId): exists user with such id => return user")
    public void tryGetById3() {
        User user = createUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        User actual = repository.tryGetById(toUUID(1));

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getByName(name): name is null => exception")
    public void getByName1() {
        AssertUtil.assertValidateException(
                () -> repository.getByName(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("getByName(name): not exists user with such name => empty optional")
    public void getByName2() {
        User user = createUser(1);
        repository.save(user);

        Optional<User> actual = repository.getByName("unknown name");
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("getByName(name): exists user with such name => return user")
    public void getByName3() {
        User user = createUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        User actual = repository.getByName("User1").orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryGetByName(name):
             name is null
             => exception
            """)
    public void tryGetByName1() {
        AssertUtil.assertValidateException(
                () -> repository.tryGetByName(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(name):
             not exists user with such name
             => exception
            """)
    public void tryGetByName2() {
        User user = createUser(1);
        commit(() -> repository.save(user));

        AssertUtil.assertValidateException(
                () -> repository.tryGetByName("some user"),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetByName(name):
             exists user with such name
             => return user
            """)
    public void tryGetByName3() {
        User expected = createUser(1);
        commit(() -> repository.save(expected));

        User actual = repository.tryGetByName("User1");

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getByEmail(email): email is null => exception")
    public void getByEmail1() {
        AssertUtil.assertValidateException(
                () -> repository.getByEmail(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("getByEmail(email): not exists user with such email => empty optional")
    public void getByEmail2() {
        User user = createUser(1);
        repository.save(user);

        Optional<User> actual = repository.getByEmail("unknownUser@gmail.com");
        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("getByEmail(email): exists user with such email => return user")
    public void getByEmail3() {
        User user = createUser(1);
        User expected = new User(user);
        commit(() -> repository.save(user));

        User actual = repository.getByEmail("user1@mail.com").orElseThrow();

        AssertUtil.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            tryGetByEmail(email):
             email is null
             => exception
            """)
    public void tryGetByEmail1() {
        AssertUtil.assertValidateException(
                () -> repository.tryGetByEmail(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetByEmail(email):
             not exists uer with such email
             => exception
            """)
    public void tryGetByEmail2() {
        User user = createUser(1);
        commit(() -> repository.save(user));

        AssertUtil.assertValidateException(
                () -> repository.tryGetByEmail("newEmail@mail.com"),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetByEmail(email):
             exists user with such email
             => return user
            """)
    public void tryGetByEmail3() {
        User expected = createUser(1);
        commit(() -> repository.save(expected));

        User actual = repository.tryGetByEmail("user1@mail.com");

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

    private User createUser(int userId) {
        return new User.Builder().
                setId(toUUID(userId)).
                setName("User" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@mail.com").
                tryBuild();
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}
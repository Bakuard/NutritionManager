package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.validation.Constraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class JwsBlackListTest {

    @Autowired
    private JwsBlackListRepository repository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ConfigData appConfiguration;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        commit(() -> JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "UsedImages", "JwsBlackList",
                "MenuItems", "DishIngredients", "MenuTags", "DishTags", "ProductTags",
                "Menus", "Dishes", "Products", "Users"));
    }

    @Test
    @DisplayName("addToBlackList(tokenId, expired): tokenId is null => exception")
    public void addToBlackList1() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.addToBlackList(null, LocalDateTime.now().plusDays(2))),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("addToBlackList(tokenId, expired): expired is null => exception")
    public void addToBlackList2() {
        AssertUtil.assertValidateException(
                () -> commit(() -> repository.addToBlackList(toUUID(1), null)),
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
        
        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired = current time
             => return false
            """)
    public void addToBlackList4() {
        boolean actual = commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now()));

        Assertions.assertThat(actual).isFalse();
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

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired < current time
             => doesn't add token to black list
            """)
    public void addToBlackList6() {
        commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now().minusDays(1)));

        boolean actual = repository.inBlackList(toUUID(1));
        
        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            addToBlackList(tokenId, expired):
             expired = current time
             => doesn't add token to black list
            """)
    public void addToBlackList7() {
        commit(() -> repository.addToBlackList(toUUID(1), LocalDateTime.now()));

        boolean actual = repository.inBlackList(toUUID(1));
        
        Assertions.assertThat(actual).isFalse();
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

        Assertions.assertThat(actual).isTrue();
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

        boolean actual = repository.inBlackList(toUUID(1));
        
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("removeAllExpired(deadline): black list is empty => return 0")
    public void removeAllExpired1() {
        int actual =commit(() -> repository.removeAllExpired(LocalDateTime.now()));

        Assertions.assertThat(actual).isZero();
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

        Assertions.assertThat(actual).isZero();
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

        Assertions.assertThat(List.of(
                repository.inBlackList(toUUID(1)),
                repository.inBlackList(toUUID(2)),
                repository.inBlackList(toUUID(3))
        )).containsOnly(true);
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

        Assertions.assertThat(actual).isEqualTo(3);
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

        Assertions.assertThat(List.of(
                repository.inBlackList(toUUID(1)),
                repository.inBlackList(toUUID(2)),
                repository.inBlackList(toUUID(3)),
                repository.inBlackList(toUUID(4)),
                repository.inBlackList(toUUID(5)),
                repository.inBlackList(toUUID(6))
        )).containsExactly(false, false, false, true, true, true);
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

    private void commit(Runnable action) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            action.run();
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
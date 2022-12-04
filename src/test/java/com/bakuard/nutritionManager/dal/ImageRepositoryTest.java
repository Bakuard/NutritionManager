package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.User;
import org.junit.jupiter.api.Assertions;
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

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DBTestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private AppConfigData appConfiguration;
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
            getUnusedImages():
             there are not images in DB
             => return empty list
            """)
    void getUnusedImages1() {
        List<String> actual = imageRepository.getUnusedImages();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getUnusedImages():
             there are images in DB,
             there are not unused images
             => return empty list
            """)
    void getUnusedImages2() {
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

        List<String> actual = imageRepository.getUnusedImages();

        Assertions.assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("""
            getUnusedImages():
             there are images used for products,
             there are unused images
             => return all unused images
            """)
    void getUnusedImages3() {
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

        List<String> actual = commit(() -> imageRepository.getUnusedImages());

        Assertions.assertEquals(
                List.of("622ed5d9d4c45a0ce5b4dd9a7b80fd74", "6e209648b5cce6ebd95561788d1cbfb8"),
                actual
        );
    }

    @Test
    @DisplayName("""
            removeUnusedImages():
             there are images in DB,
             there are not unused images
             => don't remove anything
            """)
    void removeUnusedImages1() {
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

        imageRepository.removeUnusedImages();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(
                        imageRepository.getImageUrl(user.getId(), "d1921aa0ca3c1146a01520c04e6caa9e")
                ),
                () -> Assertions.assertNotNull(
                        imageRepository.getImageUrl(user.getId(), "889b88c75a5b0236e6fa67848fdce656")
                ),
                () -> Assertions.assertNotNull(
                        imageRepository.getImageUrl(user.getId(), "622ed5d9d4c45a0ce5b4dd9a7b80fd74")
                )
        );
    }

    @Test
    @DisplayName("""
            removeUnusedImages():
             there are images used for products,
             there are unused images
             => remove all unused images
            """)
    void removeUnusedImages2() {
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

        commit(() -> imageRepository.removeUnusedImages());

        List<String> actual = commit(() -> imageRepository.getUnusedImages());
        Assertions.assertTrue(actual.isEmpty());
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
package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ConfigData conf;
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
    @DisplayName("save(product): product is null => exception")
    void save1() {
        AssertUtil.assertValidateException(
                () -> repository.save(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("save(product): no products in DB => add product")
    void save2() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user);

        commit(() -> repository.save(expected));
        Product actual = repository.tryGetById(user.getId(), expected.getId());

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("save(product): there are products in DB, product id not exists => add product")
    void save3() {
        User user1 = createAndSaveUser(1);
        User user2 = createAndSaveUser(2);
        Product product1 = createProduct(1, user1);
        Product product2 = createProduct(2, user1);
        Product expected = createProduct(3, user2);

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));
        commit(() -> repository.save(expected));

        Product actual = repository.tryGetById(user2.getId(), toUUID(3));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id not exists,
             user has a product with the same productContext and other id in the database
             => exception
            """)
    void save4() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user);
        Product product2 = createProduct(2, user);
        Product addedProduct = new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(3)).
                setUser(user).
                setCategory("name#2").
                setShop("shop#2").
                setGrade("variety#2").
                setManufacturer("manufacturer#2").
                setUnit("unitA").
                setPrice(BigDecimal.TEN).
                setPackingSize(BigDecimal.ONE).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description #3").
                setImageUrl("https://nutritionmanager.xyz/products/images?id=3").
                addTag("tag 1").
                addTag("1 tag").
                addTag("tag 2").
                addTag("2 tag").
                addTag("tag 3").
                addTag("3 tag").
                addTag("a tag").
                tryBuild();
        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        AssertUtil.assertValidateException(
                () -> commit(() ->repository.save(addedProduct)),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
        );
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state was changed
             => update product
            """)
    void save5() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user);
        Product product2 = createProduct(2, user);
        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        Product expected = new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(1)).
                setUser(user).
                setCategory("updated name").
                setShop("updated shop").
                setGrade("updated grade").
                setManufacturer("updated manufacturer").
                setUnit("updated unit").
                setPrice(BigDecimal.TEN).
                setPackingSize(BigDecimal.TEN).
                setQuantity(BigDecimal.TEN).
                setDescription("updated description").
                setImageUrl("https://nutritionmanager.xyz/products/images?updatedImageUrl").
                addTag("tag 1").
                addTag("1 tag").
                addTag("updated tag 2").
                addTag("2 tag updated").
                tryBuild();
        commit(() -> repository.save(expected));

        Product actual = repository.tryGetById(user.getId(), toUUID(1));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            save(product):
             there are products in DB,
             product id exists,
             product state was changed,
             user has a product with the same productContext in the database
             => exception
            """)
    void save6() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user);
        Product product2 = createProduct(2, user);
        Product updatedProduct = new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(2)).
                setUser(user).
                setCategory("name#1").
                setShop("shop#1").
                setGrade("variety#1").
                setManufacturer("manufacturer#1").
                setUnit("unitA").
                setPrice(BigDecimal.TEN).
                setPackingSize(BigDecimal.ONE).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description #2").
                setImageUrl("https://nutritionmanager.xyz/products/images?id=2").
                addTag("tag 1").
                addTag("1 tag").
                addTag("tag 2").
                addTag("2 tag").
                addTag("tag 3").
                addTag("3 tag").
                addTag("a tag").
                tryBuild();

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.save(updatedProduct)),
                Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB
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
    void save7() {
        User user = createAndSaveUser(1);
        Product product1 = createProduct(1, user);
        Product product2 = createProduct(2, user);
        Product expected = new Product(product1);

        commit(() -> repository.save(product1));
        commit(() -> repository.save(product2));
        commit(() -> repository.save(product1));

        Product actual = repository.tryGetById(user.getId(), toUUID(1));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("tryRemove(userId, productId): productId is null => exception")
    void tryRemove1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryRemove(user.getId(), null)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("tryRemove(userId, productId): userId is null => exception")
    void tryRemove2() {
        createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryRemove(null, toUUID(1))),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, productId):
             product with such id not exists in DB
             => exception
            """)
    void tryRemove3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryRemove(user.getId(), toUUID(10))),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, productId):
             product with such id exists in DB,
             user is not owner of this product
             => exception
            """)
    void tryRemove4() {
        User user = createAndSaveUser(1);
        commit(() -> repository.save(createProduct(1, createAndSaveUser(2))));

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryRemove(user.getId(), toUUID(1))),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryRemove(userId, productId):
             product with such id exists in DB,
             user is owner of this product
             => remove product
            """)
    void tryRemove5() {
        User user = createAndSaveUser(1);
        Product product = createProduct(1, user);
        commit(() -> repository.save(product));

        commit(() -> repository.tryRemove(user.getId(), toUUID(1)));
        Optional<Product> actual = repository.getById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            tryRemove(userId, productId):
             product with such id exists in DB,
             user is owner of this product
             => return removed product
            """)
    void tryRemove6() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user);
        commit(() -> repository.save(expected));

        Product actual = commit(() -> repository.tryRemove(user.getId(), toUUID(1)));

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("getById(userId, productId): productId is null => exception")
    void getById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.getById(user.getId(), null)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("getById(userId, productId): userId is null => exception")
    void getById2() {
        createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.getById(null, toUUID(1))),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getById(userId, productId):
             product with such id not exists in DB
             => return empty Optional
            """)
    void getById3() {
        User user = createAndSaveUser(1);

        Optional<Product> actual = repository.getById(user.getId(), toUUID(256));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getById(userId, productId):
             product with such id exists in DB,
             user is not owner of this product
             => return empty Optional
            """)
    void getById4() {
        User user = createAndSaveUser(1);
        commit(() -> repository.save(createProduct(1, createAndSaveUser(2))));

        Optional<Product> actual = repository.getById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getById(userId, productId):
             product with such id exists in DB,
             user is owner of this product
             => return correct result
            """)
    void getById5() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user);
        commit(() -> repository.save(expected));

        Optional<Product> actual = repository.getById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.type(Product.class)).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("tryGetById(userId, productId): productId is null => exception")
    void tryGetById1() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryGetById(user.getId(), null)),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("tryGetById(userId, productId): userId is null => exception")
    void tryGetById2() {
        createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryGetById(null, toUUID(1))),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, productId):
             product with such id not exists in DB
             => exception
            """)
    void tryGetById3() {
        User user = createAndSaveUser(1);

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryGetById(user.getId(), toUUID(256))),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, productId):
             product with such id exists in DB,
             user is not owner of this product
             => exception
            """)
    void tryGetById4() {
        User user = createAndSaveUser(1);
        commit(() -> repository.save(createProduct(1, createAndSaveUser(2))));

        AssertUtil.assertValidateException(
                () -> commit(() -> repository.tryGetById(user.getId(), toUUID(1))),
                Constraint.ENTITY_MUST_EXISTS_IN_DB
        );
    }

    @Test
    @DisplayName("""
            tryGetById(userId, productId):
             product with such id exists in DB,
             user is owner of this product
             => return correct result
            """)
    void tryGetById5() {
        User user = createAndSaveUser(1);
        Product expected = createProduct(1, user);
        commit(() -> repository.save(expected));

        Product actual = repository.tryGetById(user.getId(), toUUID(1));

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
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
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider only those products that the user has,
             filter not specified
            """)
    void getProductsNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO)
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(3);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter not specified
            """)
    void getProductsNumber4() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(6);
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
        createAndSaveProducts(user1);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user2.getId()),
                                        Filter.minTags(new Tag("common tag"))
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider only those products that the user has,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
             matches exist
            """)
    void getProductsNumber6() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(1);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is MinTags,
             matches exist
            """)
    void getProductsNumber7() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.minTags(new Tag("common tag")),
                                Filter.user(user.getId())
                        )
                )
        );

        Assertions.assertThat(actual).isEqualTo(6);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider only those products that the user has,
             filter is ShopsConstraint,
             matches exist
            """)
    void getProductsNumber8() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.anyShop("shop C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                CategoryConstraint - matches exist,
                GradesConstraints - matches exist,
             matches exist
            """)
    void getProductsNumber9() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name B"),
                                        Filter.anyGrade("variety C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches not exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber10() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("this tag not exists")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches not exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber11() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("this name not exists"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches not exist,
                GradesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber12() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("this shop not exists"),
                                        Filter.anyGrade("variety C")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches not exist,
             matches not exist
            """)
    void getProductsNumber13() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("this variety not exists")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
             matches not exist
            """)
    void getProductsNumber14() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety D")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
                ManufacturerConstraint - matches exists
             matches not exist
            """)
    void getProductsNumber15() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("value 2")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety D"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
                ManufacturerConstraint - matches exists
             matches exist
            """)
    void getProductsNumber16() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is AndFilter. Operands:
                MinTags - matches exist,
                CategoryConstraint - matches exist,
                ShopsConstraint - matches exist,
                GradesConstraints - matches exist,
                ManufacturerConstraint - matches not exists
            """)
    void getProductsNumber17() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer Z")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
             => return correct result
            """)
    void getProductsNumber18() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             consider only those products that the user has,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
             => return correct result
            """)
    void getProductsNumber19() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.greater(BigDecimal.ZERO),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.greater(BigDecimal.ZERO),
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyGrade("variety C"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(1);
    }

    @Test
    @DisplayName("""
            getProductsNumber(criteria):
             user have some products,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match not exists,
                    ManufacturerConstraint - match not exists
             => return 0
            """)
    void getProductsNumber20() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getProductsNumber(
                new Criteria().
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("unknown variety"),
                                                Filter.anyManufacturer("unknown manufacturer")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(0);
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
        createAndSaveProducts(user1);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(6, 0)).
                        setFilter(Filter.user(user2.getId()))
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             get full page,
             filter not specified
            """)
    void getProducts3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(6, conf.pagination().productMaxPageSize()).
                createPage(products.subList(0, 5));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 0)).
                        setFilter(Filter.user(user.getId())).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             get partial page
             filter not specified
            """)
    void getProducts4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(5, 1).
                createPageMetadata(6, conf.pagination().productMaxPageSize()).
                createPage(products.subList(5, 6));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 1)).
                        setFilter(Filter.user(user.getId())).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             get full page
             filter not specified
            """)
    void getProducts5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 0).
                createPageMetadata(3, conf.pagination().productMaxPageSize()).
                createPage(products.subList(3, 5));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO)
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             get partial page
             filter not specified
            """)
    void getProducts6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 1).
                createPageMetadata(3, conf.pagination().productMaxPageSize()).
                createPage(products.subList(5, 6));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 1)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO)
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
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
                new Criteria().
                        setPageable(PageableByNumber.of(6, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag"))
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = full,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(1, 0).
                createPageMetadata(1, conf.pagination().productMaxPageSize()).
                createPage(products.subList(5, 6));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(1, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag B"), new Tag("common tag")),
                                        Filter.anyCategory("name B", "unknown name", "unknown name 2"),
                                        Filter.anyShop("shop C", "unknown shop", "unknown shop 2"),
                                        Filter.anyGrade("variety D", "unknown variety", "unknown variety 2")
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = empty,
             filter is MinTags - match exists
            """)
    void getProducts9() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(6, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.minTags(new Tag("tag A"), new Tag("common tag")),
                                        Filter.anyCategory("name B"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety D")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = full,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, conf.pagination().productMaxPageSize()).
                createPage(products.subList(0, 2));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A"), new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A")
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = empty,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                GradesConstraints - match exists
            """)
    void getProducts11() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag Z"), new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop Z"),
                                        Filter.anyGrade("variety A")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = partial,
             filter is AndFilter. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match exists
            """)
    void getProducts12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(3, 0).
                createPageMetadata(3, conf.pagination().productMaxPageSize()).
                createPage(products.subList(3, 6));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(3, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.anyCategory("name B", "name A"),
                                        Filter.anyShop("shop B", "shop C")
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = empty,
             filter is AndFilter. Operands:
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                GradesConstraints - match exists
            """)
    void getProducts13() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.anyCategory("name Z"),
                                        Filter.anyShop("shop Z"),
                                        Filter.anyGrade("variety A")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = empty,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match not exists
            """)
    void getProducts14() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety Z")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = partial,
             filter is AndFilter. Operands:
                MinTags - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(3, 0).
                createPageMetadata(3, conf.pagination().productMaxPageSize()).
                createPage(products.subList(0, 3));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(3, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyShop("shop A", "shop B"),
                                        Filter.anyGrade("variety A", "variety B")
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = empty,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                GradesConstraints - match not exists
            """)
    void getProducts16() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(3, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common Z")),
                                        Filter.anyCategory("name Z"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety Z")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = empty,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists,
                ManufacturerConstraint - match exists
             match not exists
            """)
    void getProducts17() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(6, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop C"),
                                        Filter.anyGrade("variety D"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = full,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists,
                ManufacturerConstraint - match exists
             match exists
            """)
    void getProducts18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, conf.pagination().productMaxPageSize()).
                createPage(products.subList(0, 2));

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = empty,
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists,
                ManufacturerConstraint - match not exists
            """)
    void getProducts19() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(2, 0)).
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("common tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer Z")
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = full,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 0)).
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown name"),
                                                Filter.anyShop("unknown shop"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(2, conf.pagination().productMaxPageSize()).
                createPage(products.subList(0, 2));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider only those products that the user has,
             pageable = full,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 0)).
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.greater(BigDecimal.ZERO),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.greater(BigDecimal.ZERO),
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyGrade("variety C"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        ).
                        setSort(Sort.productDefaultSort())
        );

        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(1, conf.pagination().productMaxPageSize()).
                createPage(products.subList(3, 4));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             consider all the user's products, whether he has them or not,
             pageable = full,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    CategoryConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
             => return full page
            """)
    void getProducts22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 0)).
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.anyCategory("name B"),
                                                Filter.anyManufacturer("manufacturer B")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        ).
                        setSort(
                                Sort.products("price_desc")
                        )
        );

        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(4, conf.pagination().productMaxPageSize()).
                createPage(
                        List.of(
                                products.get(5),
                                products.get(4),
                                products.get(1),
                                products.get(0)
                        )
                );
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getProducts(criteria):
             user have some products,
             pageable = full,
             filter is Or. Operands:
                first operand is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraints - match exists,
                    ManufacturerConstraint - match exists
                second operand is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraints - match not exists,
                    ManufacturerConstraint - match not exists
             => return empty page
            """)
    void getProducts23() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Page<Product> actual = repository.getProducts(
                new Criteria().
                        setPageable(PageableByNumber.of(5, 0)).
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag B")),
                                                Filter.anyCategory("name B"),
                                                Filter.anyShop("shop B"),
                                                Filter.anyGrade("unknown variety"),
                                                Filter.anyManufacturer("unknown manufacturer")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(Page.empty());
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
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists,
             result not exists
             => return empty Page
            """)
    void getTags3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyShop("unknown shop"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturers - match not exists
             result not exists
             => return empty Page
            """)
    void getTags4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getTags5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyCategory("unknown name"),
                Filter.anyGrade("unknown grade")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match not exists
             result not exists
             => return empty Page
            """)
    void getTags6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B", "name C"),
                Filter.anyShop("unknown shop")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getTags7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match not exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getTags8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("unknown category 1"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getTags9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getTags10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getTags11() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("shop A", "shop B")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match not exists,
                Grades - match exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getTags12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A"), new Tag("unknown tag")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("variety 1", "variety 2"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Shops - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getTags13() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("unknown grade")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Shops - match exists,
                Grades - match exists
             result not exists
             => return empty Page
            """)
    void getTags14() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("grade A", "grade B")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getTags15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getTags16() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A","shop B", "shop C"),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(3, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of(
                                        new Tag("common tag"),
                                        new Tag("tag B"),
                                        new Tag("value 6")
                                ))
                );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getTags17() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getTags18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop C")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists
             result exists
             => return correct Page
            """)
    void getTags19() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A")
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(5, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of(
                                        new Tag("common tag"),
                                        new Tag("tag A"),
                                        new Tag("value 1"),
                                        new Tag("value 2"),
                                        new Tag("value 3")
                                ))
                );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getTags20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A", "manufacturer B")
                )
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(7, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of(
                                        new Tag("common tag"),
                                        new Tag("tag A"),
                                        new Tag("tag B"),
                                        new Tag("value 3"),
                                        new Tag("value 4"),
                                        new Tag("value 5"),
                                        new Tag("value 6")
                                ))
                );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             filter is UserFilter
             => return all shops
            """)
    void getTags21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.user(user.getId());

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(9, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of(
                                        new Tag("common tag"),
                                        new Tag("tag A"),
                                        new Tag("tag B"),
                                        new Tag("value 1"),
                                        new Tag("value 2"),
                                        new Tag("value 3"),
                                        new Tag("value 4"),
                                        new Tag("value 5"),
                                        new Tag("value 6")
                                ))
                );
    }

    @Test
    @DisplayName("""
            getTags(criteria):
             user have some products,
             consider only those products that the user has,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
            """)
    void getTags22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A"),
                        Filter.greater(BigDecimal.ZERO)
                )
        );

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(3, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of(
                                        new Tag("common tag"),
                                        new Tag("tag B"),
                                        new Tag("value 4")
                                ))
                );
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
        Page<String> expected = Page.empty();

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists,
             result not exists
             => return empty Page
            """)
    void getShops3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyShop("unknown shop"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturers - match not exists
             result not exists
             => return empty Page
            """)
    void getShops4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getShops5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyCategory("unknown name"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match not exists
             result not exists
             => return empty Page
            """)
    void getShops6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B", "name C"),
                Filter.anyShop("unknown shop")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getShops7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match not exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getShops8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("unknown category 1"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getShops9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getShops10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getShops11() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("shop A", "shop B")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match not exists,
                Grades - match exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getShops12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A"), new Tag("unknown tag")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("variety 1", "variety 2"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Shops - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getShops13() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Shops - match exists,
                Grades - match exists
             result not exists
             => return empty Page
            """)
    void getShops14() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("grade A", "grade B")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getShops15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getShops16() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A","shop B", "shop C"),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("shop C"))
                );
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getShops17() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getShops18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop C")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists
             result exists
             => return correct Page
            """)
    void getShops19() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A")
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("shop A", "shop B"))
                );
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getShops20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A", "manufacturer B")
                )
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("shop B", "shop C"))
                );
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             filter is UserFilter
             => return correct Page
            """)
    void getShops21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.user(user.getId());

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(3, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("shop A", "shop B", "shop C"))
                );
    }

    @Test
    @DisplayName("""
            getShops(criteria):
             user have some products,
             consider only those products that the user has,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getShops22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A"),
                        Filter.greater(BigDecimal.ZERO)
                )
        );

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("shop B"))
                );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             criteria is null
             => exception
            """)
    void getGrades1() {
        AssertUtil.assertValidateException(
                () -> repository.getGrades(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user haven't any products
             => return empty page
            """)
    void getGrades2() {
        User user = createAndSaveUser(1);
        Page<String> expected = Page.empty();

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists,
             result not exists
             => return empty Page
            """)
    void getGrades3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyShop("unknown shop"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturers - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyCategory("unknown name"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B", "name C"),
                Filter.anyShop("unknown shop")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getGrades7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match not exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getGrades8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("unknown category 1"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getGrades11() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("shop A", "shop B")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match not exists,
                Grades - match exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A"), new Tag("unknown tag")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("variety 1", "variety 2"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Shops - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades13() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Shops - match exists,
                Grades - match exists
             result not exists
             => return empty Page
            """)
    void getGrades14() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("grade A", "grade B")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getGrades15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getGrades16() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A","shop B", "shop C"),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("variety D"))
                );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getGrades17() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getGrades18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop C")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists
             result exists
             => return correct Page
            """)
    void getGrades19() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A")
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("variety A", "variety B"))
                );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getGrades20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A", "manufacturer B")
                )
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(3, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("variety B", "variety C", "variety D"))
                );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             filter is UserFilter
             => return correct Page
            """)
    void getGrades21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.user(user.getId());

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(4, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("variety A", "variety B", "variety C", "variety D"))
                );
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             consider only those products that the user has,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getGrades22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A"),
                        Filter.greater(BigDecimal.ZERO)
                )
        );

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("variety C"))
                );
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
        Page<String> expected = Page.empty();

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists,
             result not exists
             => return empty Page
            """)
    void getCategories3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyShop("unknown shop"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturers - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyCategory("unknown name"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B", "name C"),
                Filter.anyShop("unknown shop")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getCategories7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match not exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getCategories8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("unknown category 1"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getCategories11() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("shop A", "shop B")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match not exists,
                Grades - match exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A"), new Tag("unknown tag")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("variety 1", "variety 2"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Shops - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories13() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Shops - match exists,
                Grades - match exists
             result not exists
             => return empty Page
            """)
    void getCategories14() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("grade A", "grade B")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getCategories15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getCategories16() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A","shop B", "shop C"),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("name B"))
                );
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getCategories17() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getCategories18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop C")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists
             result exists
             => return correct Page
            """)
    void getCategories19() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A")
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("name A"))
                );
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getCategories20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A", "manufacturer B")
                )
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("name A", "name B"))
                );
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             filter is UserFilter
             => return correct Page
            """)
    void getCategories21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.user(user.getId());

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("name A", "name B"))
                );
    }

    @Test
    @DisplayName("""
            getCategories(criteria):
             user have some products,
             consider only those products that the user has,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getCategories22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A"),
                        Filter.greater(BigDecimal.ZERO)
                )
        );

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("name B"))
                );
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
        Page<String> expected = Page.empty();

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists,
             result not exists
             => return empty Page
            """)
    void getManufacturers3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyShop("unknown shop"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturers - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag")),
                Filter.anyCategory("unknown name"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A", "name B", "name C"),
                Filter.anyShop("unknown shop")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers7() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyShop("shop A", "shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match not exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("unknown category 1"),
                Filter.anyGrade("variety A", "variety B"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers9() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers11() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("shop A", "shop B")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match not exists,
                Shops - match not exists,
                Grades - match exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag"), new Tag("tag A"), new Tag("unknown tag")),
                Filter.anyCategory("unknown name 1", "unknown name 2"),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("variety 1", "variety 2"),
                Filter.anyManufacturer("unknown manufacturer 1", "unknown manufacturer 2")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Shops - match not exists,
                Grades - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers13() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyShop("unknown shop 1", "unknown shop 2"),
                Filter.anyGrade("unknown grade")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Shops - match exists,
                Grades - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers14() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("grade A", "grade B")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match not exists,
                Category - match exists,
                Shops - match exists,
                Grades - match not exists,
                Manufacturer - match not exists
             result not exists
             => return empty Page
            """)
    void getManufacturers15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("unknown tag 1"), new Tag("unknown tag 2")),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop A","shop B"),
                Filter.anyGrade("unknown grade"),
                Filter.anyManufacturer("unknown manufacturer")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists,
                Shops - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getManufacturers16() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A", "name B"),
                Filter.anyShop("shop A","shop B", "shop C"),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A", "manufacturer B")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("manufacturer B"))
                );
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Grades - match exists,
                Manufacturer - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers17() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyGrade("variety D"),
                Filter.anyManufacturer("manufacturer A")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                Category - match exists,
                Shops - match exists
             result not exists
             => return empty Page
            """)
    void getManufacturers18() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.anyCategory("name A"),
                Filter.anyShop("shop C")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).isEqualTo(Page.empty());
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is AndFilter. Operands:
                MinTags - match exists,
                Category - match exists
             result exists
             => return correct Page
            """)
    void getManufacturers19() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.and(
                Filter.user(user.getId()),
                Filter.minTags(new Tag("common tag")),
                Filter.anyCategory("name A")
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("manufacturer A"))
                );
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getManufacturers20() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A", "manufacturer B")
                )
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("manufacturer A", "manufacturer B"))
                );
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             filter is UserFilter
             => return correct Page
            """)
    void getManufacturers21() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.user(user.getId());

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(2, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("manufacturer A", "manufacturer B"))
                );
    }

    @Test
    @DisplayName("""
            getManufacturers(criteria):
             user have some products,
             consider only those products that the user has,
             filter is OrFilter. Operands:
                AndFilter:
                    MinTags - match exists,
                    Category - match not exists,
                    Shops - match not exists
                AndFilter:
                    Grades - match exists,
                    Manufacturer - match exists
             result exists
             => return correct Page
            """)
    void getManufacturers22() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.minTags(new Tag("tag A")),
                        Filter.anyCategory("unknown name"),
                        Filter.anyShop("unknown shop")
                ),
                Filter.and(
                        Filter.user(user.getId()),
                        Filter.anyGrade("variety B", "variety C", "variety D"),
                        Filter.anyManufacturer("manufacturer A"),
                        Filter.greater(BigDecimal.ZERO)
                )
        );

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(filter).
                        setPageable(PageableByNumber.of(20, 0))
        );

        Assertions.assertThat(actual).
                isEqualTo(
                        PageableByNumber.of(20, 0).
                                createPageMetadata(1, conf.pagination().itemsMaxPageSize()).
                                createPage(List.of("manufacturer A"))
                );
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
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum2() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyShop("unknown shop"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                GradesConstraint - match exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("unknown name"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match not exists,
                CategoryConstraint - match exists,
                GradesConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum4() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyGrade("variety A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                GradesConstraint - match not exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum5() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("unknown shop"),
                                        Filter.anyGrade("unknown variety"),
                                        Filter.anyManufacturer("unknown manufacturer")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match exists,
                ShopsConstraint - match exists,
                GradesConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum6() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("unknown variety")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum7() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("unknown name")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match not exists,
                GradesConstraint - match exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum8() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("unknown manufacturer")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum9() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("variety A")
                                )
                        )
        );
        
        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(62));
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                GradesConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum10() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("unknown name"),
                                        Filter.anyShop("unknown shop"),
                                        Filter.anyGrade("variety A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match exists,
                GradesConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum11() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyCategory("unknown name"),
                                        Filter.anyShop("shop A"),
                                        Filter.anyGrade("unknown variety"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum12() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("tag A")),
                                        Filter.anyCategory("name A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(107));
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                GradesConstraint - match exists,
                ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum13() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyGrade("variety A"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );
        
        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(62));
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match not exists,
                GradesConstraint - match not exists,
                ManufacturerConstraint - match exists
             => return empty Optional
            """)
    void getProductsSum14() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A"),
                                        Filter.anyShop("unknown shop"),
                                        Filter.anyGrade("unknown variety"),
                                        Filter.anyManufacturer("manufacturer A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is ShopsConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum15() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyShop("shop A")
                                )
                        )
        );

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(62));
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is AndFilter. Operands:
                MinTags - match not exists,
                CategoryConstraint - match not exists,
                ShopsConstraint - match not exists,
                ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum16() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.minTags(new Tag("unknown tag")),
                                        Filter.anyCategory("unknown name"),
                                        Filter.anyShop("unknown shop"),
                                        Filter.anyManufacturer("unknown manufacturer")
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is OrElse. Operands:
                first operands is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operands is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraint - match exists,
                    ManufacturerConstraint - match exists
             => return Optional with correct result
            """)
    void getProductsSum17() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).
                isPresent().
                get(InstanceOfAssertFactories.BIG_DECIMAL).
                isEqualByComparingTo(new BigDecimal(62));
    }

    @Test
    @DisplayName("""
            getProductsSum(criteria):
             filter is OrElse. Operands:
                first operands is AndConstraint. Operands:
                    MinTags - match not exists,
                    CategoryConstraint - match not exists,
                    ShopsConstraint - match not exists,
                    GradesConstraint - match exists,
                    ManufacturerConstraint - match exists
                second operands is AndConstraint. Operands:
                    MinTags - match exists,
                    CategoryConstraint - match exists,
                    ShopsConstraint - match exists,
                    GradesConstraint - match not exists,
                    ManufacturerConstraint - match not exists
             => return empty Optional
            """)
    void getProductsSum18() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        Optional<BigDecimal> actual = repository.getProductsSum(
                new Criteria().
                        setFilter(
                                Filter.or(
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("unknown tag")),
                                                Filter.anyCategory("unknown category"),
                                                Filter.anyShop("unknown shops"),
                                                Filter.anyGrade("variety A"),
                                                Filter.anyManufacturer("manufacturer A")
                                        ),
                                        Filter.and(
                                                Filter.user(user.getId()),
                                                Filter.minTags(new Tag("tag A")),
                                                Filter.anyCategory("name A"),
                                                Filter.anyShop("shop A"),
                                                Filter.anyGrade("unknown variety"),
                                                Filter.anyManufacturer("unknown manufacturer")
                                        )
                                )
                        )
        );

        Assertions.assertThat(actual).isEmpty();
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

    private User createAndSaveUser(int userId) {
        User user = new User.Builder().
                setId(toUUID(userId)).
                setName("User#" + userId).
                setPassword("password" + userId).
                setEmail("user" + userId + "@confirmationMail.com").
                tryBuild();
        commit(() -> userRepository.save(user));
        return user;
    }

    private Product createProduct(int productId, User user) {
        return new Product.Builder().
                setAppConfiguration(conf).
                setId(toUUID(productId)).
                setUser(user).
                setCategory("name#" + productId).
                setShop("shop#" + productId).
                setGrade("variety#" + productId).
                setManufacturer("manufacturer#" + productId).
                setUnit("unitA").
                setPrice(BigDecimal.TEN).
                setPackingSize(BigDecimal.ONE).
                setQuantity(BigDecimal.ZERO).
                setDescription("some description #" + productId).
                setImageUrl("https://nutritionmanager.xyz/products/images?id=" + productId).
                addTag("tag 1").
                addTag("1 tag").
                addTag("tag 2").
                addTag("2 tag").
                addTag("tag 3").
                addTag("3 tag").
                addTag("a tag").
                tryBuild();
    }

    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private List<Product> createAndSaveProducts(User user) {
        ArrayList<Product> products = new ArrayList<>();

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("value 1").
                        addTag("common tag").
                        addTag("tag A").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("tag A").
                        addTag("common tag").
                        addTag("value 2").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("tag A").
                        addTag("common tag").
                        addTag("value 3").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("tag B").
                        addTag("common tag").
                        addTag("value 4").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("tag B").
                        addTag("common tag").
                        addTag("value 5").
                        tryBuild()
        );

        products.add(
                new Product.Builder().
                        setAppConfiguration(conf).
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
                        addTag("tag B").
                        addTag("common tag").
                        addTag("value 6").
                        tryBuild()
        );

        commit(() -> products.forEach(p -> repository.save(p)));
        
        return products;
    }

    private List<Tag> createTags(List<Product> allProducts) {
        return allProducts.stream().
                flatMap(p -> p.getContext().getTags().stream()).
                distinct().
                sorted().
                toList();
    }

}
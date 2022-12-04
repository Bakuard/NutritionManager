package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.AssertUtil;
import com.bakuard.nutritionManager.config.AppConfigData;
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
@ContextConfiguration(classes = DBTestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;
    @Autowired
    private UserRepository userRepository;
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
                setAppConfiguration(appConfiguration).
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
                setAppConfiguration(appConfiguration).
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
                setAppConfiguration(appConfiguration).
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
             onlyFridge = true,
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
             onlyFridge = false,
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
             onlFridge = true,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
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
             onlyFridge = true,
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             filter is OrElse. Operands:
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
                                Filter.orElse(
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
             onlyFridge = true,
             filter is OrElse. Operands:
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
                                Filter.orElse(
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
             filter is OrElse. Operands:
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
                                Filter.orElse(
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
             onlyFridge = false,
             get full page,
             filter not specified
            """)
    void getProducts3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(6, 200).
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
             onlyFridge = false,
             get partial page
             filter not specified
            """)
    void getProducts4() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(5, 1).
                createPageMetadata(6, 200).
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
             onlyFridge = true,
             get full page
             filter not specified
            """)
    void getProducts5() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 0).
                createPageMetadata(3, 200).
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
             onlyFridge = true,
             get partial page
             filter not specified
            """)
    void getProducts6() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 1).
                createPageMetadata(3, 200).
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
             onlyFridge = true,
             pageable = full,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts8() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(1, 0).
                createPageMetadata(1, 200).
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
             onlyFridge = true,
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
             onlyFridge = false,
             pageable = full,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                CategoryConstraint - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts10() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
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
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = true,
             pageable = partial,
             filter is AndConstraint. Operands:
                CategoryConstraint - match exists,
                ShopsConstraint - match exists
            """)
    void getProducts12() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(3, 0).
                createPageMetadata(3, 200).
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
             onlyFridge = true,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = true,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             pageable = partial,
             filter is AndConstraint. Operands:
                MinTags - match exists,
                ShopsConstraint - match exists,
                GradesConstraints - match exists
            """)
    void getProducts15() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Product> expected = PageableByNumber.of(3, 0).
                createPageMetadata(3, 200).
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
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             pageable = full,
             filter is AndConstraint. Operands:
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
                createPageMetadata(2, 200).
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
             onlyFridge = false,
             pageable = empty,
             filter is AndConstraint. Operands:
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
             onlyFridge = false,
             pageable = full,
             filter is OrElse. Operands:
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
                                Filter.orElse(
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
                createPageMetadata(2, 200).
                createPage(products.subList(0, 2));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
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
                                Filter.orElse(
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
                createPageMetadata(1, 200).
                createPage(products.subList(3, 4));
        Assertions.assertThat(actual).
                usingRecursiveComparison().
                isEqualTo(expected);
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
                                Filter.orElse(
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
                                Sort.products().desc("price")
                        )
        );

        Page<Product> expected = PageableByNumber.of(5, 0).
                createPageMetadata(4, 200).
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
             filter is OrElse. Operands:
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
                                Filter.orElse(
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
            getTagsNumber(criteria):
             criteria is null
             => exception
            """)
    void getTagsNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getTagsNumber(null),
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

        int actual = repository.getTagsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
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
        createAndSaveProducts(user);

        int actual = repository.getTagsNumber(new Criteria().setFilter(Filter.user(user.getId())));

        Assertions.assertThat(actual).isEqualTo(9);
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
        createAndSaveProducts(user);

        int actual = repository.getTagsNumber(
                new Criteria().setFilter(
                        Filter.and(
                                Filter.user(user.getId()),
                                Filter.anyCategory("name A")
                        )
                )
        );

        Assertions.assertThat(actual).isEqualTo(5);
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
             user have some product,
             pageable is full,
             productName not specified
             => return full page
            """)
    void getTags3() {
        User user = createAndSaveUser(1);
        List<Product> products = createAndSaveProducts(user);
        Page<Tag> expected = PageableByNumber.of(5, 0).
                createPageMetadata(9, 200).
                createPage(createTags(products).subList(0, 5));

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        List<Product> products = createAndSaveProducts(user);
        Page<Tag> expected = PageableByNumber.of(4, 2).
                createPageMetadata(9, 200).
                createPage(createTags(products).subList(8, 9));

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(4, 2))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<Tag> expected = PageableByNumber.of(5, 0).
                createPageMetadata(5, 200).
                createPage(List.of(
                        new Tag("common tag"),
                        new Tag("tag A"),
                        new Tag("value 1"),
                        new Tag("value 2"),
                        new Tag("value 3")
                ));

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<Tag> expected = PageableByNumber.of(4, 1).
                createPageMetadata(5, 200).
                createPage(List.of(
                        new Tag("value 3")
                ));

        Page<Tag> actual = repository.getTags(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 1))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
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
        createAndSaveProducts(user);

        int actual = repository.getShopsNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(3);
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
        createAndSaveProducts(user);

        int actual = repository.getShopsNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
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
             pageable is full,
             productName not specified
            """)
    void getShops3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(3, 0).
                createPageMetadata(3, 200).
                createPage(List.of("shop A", "shop B", "shop C"));

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(3, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(2, 1).
                createPageMetadata(3, 200).
                createPage(List.of("shop C"));

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 1))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("shop A", "shop B"));

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("shop A", "shop B"));

        Page<String> actual = repository.getShops(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getGradesNumber(criteria):
             criteria is null
             => exception
            """)
    void getGradesNumber1() {
        AssertUtil.assertValidateException(
                () -> repository.getGradesNumber(null),
                Constraint.NOT_NULL
        );
    }

    @Test
    @DisplayName("""
            getGradesNumber(criteria):
             user haven't any products
             => return 0
            """)
    void getGradesNumber2() {
        User user = createAndSaveUser(1);

        int actual = repository.getGradesNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getGradesNumber(criteria):
             user have some products,
             productName not specified
             => return correct result
            """)
    void getGradesNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getGradesNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(4);
    }

    @Test
    @DisplayName("""
            getGradesNumber(criteria):
             user have some products,
             productName specified
             => return correct result
            """)
    void getGradesNumber4() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getGradesNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
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
             pageable is full,
             productName not specified
            """)
    void getGrades3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(4, 0).
                createPageMetadata(4, 200).
                createPage(List.of("variety A", "variety B", "variety C", "variety D"));

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(4, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             pageable is partial,
             productName not specified
            """)
    void getGrades4() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(3, 1).
                createPageMetadata(4, 200).
                createPage(List.of("variety D"));

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(3, 1))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             pageable is full,
             productName specified
            """)
    void getGrades5() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("variety A", "variety B"));

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            getGrades(criteria):
             user have some products,
             pageable is partial,
             productName specified
            """)
    void getGrades6() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(4, 0).
                createPageMetadata(2, 200).
                createPage(List.of("variety A", "variety B"));

        Page<String> actual = repository.getGrades(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(4, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("""
            getCategoriesNumber(criteria):
             user have some products
             => return correct result
            """)
    void getCategoriesNumber3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);

        int actual = repository.getCategoriesNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(2);
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
             pageable is partial
             => return correct result
            """)
    void getCategories3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("name A", "name B"));

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(1, 1).
                createPageMetadata(2, 200).
                createPage(List.of("name B"));

        Page<String> actual = repository.getCategories(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(1, 1))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(0);
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
        createAndSaveProducts(user);

        int actual = repository.getManufacturersNumber(
                new Criteria().setFilter(Filter.user(user.getId()))
        );

        Assertions.assertThat(actual).isEqualTo(2);
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
        createAndSaveProducts(user);

        int actual = repository.getManufacturersNumber(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name B")
                                )
                        )
        );

        Assertions.assertThat(actual).isEqualTo(2);
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
             pageable is full,
             productName not specified
            """)
    void getManufacturers3() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(Filter.user(user.getId())).
                        setPageable(PageableByNumber.of(2, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(5, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

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
             pageable is full,
             productName specified
            """)
    void getManufacturers5() {
        User user = createAndSaveUser(1);
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(2, 0).
                createPageMetadata(2, 200).
                createPage(List.of("manufacturer A", "manufacturer B"));

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name B")
                                )
                        ).
                        setPageable(PageableByNumber.of(2, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
        createAndSaveProducts(user);
        Page<String> expected = PageableByNumber.of(5, 0).
                createPageMetadata(1, 200).
                createPage(List.of("manufacturer A"));

        Page<String> actual = repository.getManufacturers(
                new Criteria().
                        setFilter(
                                Filter.and(
                                        Filter.user(user.getId()),
                                        Filter.anyCategory("name A")
                                )
                        ).
                        setPageable(PageableByNumber.of(5, 0))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
             filter is AndConstraint. Operands:
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
                                Filter.orElse(
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
                                Filter.orElse(
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
                setEmail("user" + userId + "@mail.com").
                tryBuild();
        commit(() -> userRepository.save(user));
        return user;
    }

    private Product createProduct(int productId, User user) {
        return new Product.Builder().
                setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
                        setAppConfiguration(appConfiguration).
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
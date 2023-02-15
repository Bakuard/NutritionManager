package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.impl.mappers.ProductFilterMapper;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.filters.UserFilter;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bakuard.nutritionManager.model.filters.Filter.Type.USER;
import static com.bakuard.nutritionManager.validation.Rule.*;
import static org.jooq.impl.DSL.*;

public class ProductRepositoryPostgres implements ProductRepository {

    private JdbcTemplate statement;
    private ConfigData conf;
    private ProductFilterMapper filterMapper;

    public ProductRepositoryPostgres(DataSource dataSource, ConfigData conf) {
        statement = new JdbcTemplate(dataSource);
        this.conf = conf;
        filterMapper = new ProductFilterMapper();
    }

    @Override
    public void save(Product product) {
        Validator.check("ProductRepository.product", notNull(product));

        try {
            if(doesProductExist(product.getId())) updateProduct(product);
            else addNewProduct(product);
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save product").
                    addReason(Rule.of("ProductRepository.product", failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB)));
        }
    }

    @Override
    public Product tryRemove(UUID userId, UUID productId) {
        Product product = getById(userId, productId).
                orElseThrow(
                        () -> new ValidateException("Unknown product with id=" + productId + " for userId=" + userId).
                                addReason(Rule.of("ProductRepository.productId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );

        statement.update(
                "DELETE FROM Products WHERE productId = ? AND userId = ?;",
                (PreparedStatement ps) -> {
                    ps.setObject(1, productId);
                    ps.setObject(2, userId);
                }
        );

        return product;
    }

    @Override
    public Optional<Product> getById(UUID userId, UUID productId) {
        Validator.check(
                "ProductRepository.userId", notNull(userId),
                "ProductRepository.productId", notNull(productId)
        );

        return Stream.of(new ProductAggregateRootBuilders()).
                peek(aggregateRootBuilders -> loadAndFillProductAggregateRoot(con -> {
                            PreparedStatement ps = con.prepareStatement("""
                                    SELECT * FROM Products
                                        INNER JOIN Users ON Users.userId = Products.userId
                                        WHERE Products.productId = ? AND Products.userId = ?;
                                    """);
                            ps.setObject(1, productId);
                            ps.setObject(2, userId);
                            return ps;
                        },
                        aggregateRootBuilders)).
                filter(aggregateRootBuilders -> !aggregateRootBuilders.isEmpty()).
                peek(aggregateRootBuilders -> loadUser(userId).
                        map(user -> aggregateRootBuilders.products.get(0).setUser(user))).
                peek(aggregateRootBuilders -> loadAndFillProductTags(con -> {
                    PreparedStatement ps = con.prepareStatement("""
                            select * from ProductTags
                             inner join Products on ProductTags.productId = Products.productId
                             where Products.productId = ?
                             order by ProductTags.index;
                            """);
                    ps.setObject(1, productId);
                    return ps;
                }, aggregateRootBuilders)).
                map(aggregateRootBuilders -> aggregateRootBuilders.products().get(0).tryBuild()).
                findAny();
    }

    @Override
    public Product tryGetById(UUID userId, UUID productId) {
        return getById(userId, productId).
                orElseThrow(
                        () -> new ValidateException("Unknown product with id = " + productId + " for userId=" + userId).
                                addReason(Rule.of("ProductRepository.productId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public Page<Product> getProducts(Criteria criteria) {
        int productsNumber = getProductsNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(productsNumber, conf.pagination().productMaxPageSize());

        if(metadata.isEmpty()) return Page.empty();

        final String aggregateRootQuery = """
                select * from Products
                    where %s
                    order by %s
                    limit %s
                    offset %s
                """.formatted(
                        filterMapper.toCondition(criteria.getFilter()),
                        getOrderFields(criteria.tryGetSort()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<Product> products = Stream.of(new ProductAggregateRootBuilders()).
                peek(aggregateRootBuilders -> loadAndFillProductAggregateRoot(
                        con -> con.prepareStatement(aggregateRootQuery),
                        aggregateRootBuilders
                )).
                filter(aggregateRootBuilders -> !aggregateRootBuilders.isEmpty()).
                peek(aggregateRootBuilders -> loadUser(criteria.getFilter().
                        <UserFilter>findAny(USER).
                        orElseThrow().
                        getUserId()).
                        ifPresent(user -> aggregateRootBuilders.products().forEach(p -> p.setUser(user)))).
                peek(aggregateRootBuilders -> loadAndFillProductTags(
                        con -> con.prepareStatement("""
                            select * from ProductTags
                             inner join (%s) as TempTable
                              on ProductTags.productId = TempTable.productId
                             order by ProductTags.index;
                            """.formatted(aggregateRootQuery)),
                        aggregateRootBuilders)).
                flatMap(aggregateRootBuilders -> aggregateRootBuilders.products().stream().
                        map(Product.Builder::tryBuild)).
                collect(Collectors.toCollection(ArrayList::new));

        return metadata.createPage(products);
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        int tagsNumber = getTagsNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(tagsNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = """
                select distinct ProductTags.tagValue
                    from ProductTags
                    join Products on Products.productId = ProductTags.productId
                    where %s
                    order by ProductTags.tagValue asc
                    limit %s
                    offset %s
                """.formatted(
                        filterMapper.toCondition(criteria.getFilter()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<Tag> tags = statement.query(
                query,
                (ResultSet rs) -> {
                    List<Tag> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(new Tag(rs.getString("tagValue")));
                    }

                    return result;
                }
        );

        return metadata.createPage(tags);
    }

    @Override
    public Page<String> getShops(Criteria criteria) {
        int shopsNumber = getShopsNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(shopsNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = """
                select distinct Products.shop
                    from Products
                    where %s
                    order by Products.shop asc
                    limit %s
                    offset %s
                """.formatted(
                        filterMapper.toCondition(criteria.tryGetFilter()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<String> shops = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(shops);
    }

    @Override
    public Page<String> getGrades(Criteria criteria) {
        int gradesNumber = getGradesNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(gradesNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = """
                select distinct Products.grade
                    from Products
                    where %s
                    order by Products.grade asc
                    limit %s
                    offset %s
                """.formatted(
                        filterMapper.toCondition(criteria.tryGetFilter()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<String> grades = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(grades);
    }

    @Override
    public Page<String> getCategories(Criteria criteria) {
        int categoriesNumber = getCategoriesNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(categoriesNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = """
                select distinct Products.category
                    from Products
                    where %s
                    order by Products.category asc
                    limit %s
                    offset %s
                """.formatted(
                        filterMapper.toCondition(criteria.tryGetFilter()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<String> categories = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(categories);
    }

    @Override
    public Page<String> getManufacturers(Criteria criteria) {
        int manufacturersNumber = getManufacturersNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(manufacturersNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = """
                select distinct Products.manufacturer
                    from Products
                    where %s
                    order by Products.manufacturer asc
                    limit %s
                    offset %s
                """.
                formatted(
                        filterMapper.toCondition(criteria.tryGetFilter()),
                        metadata.getActualSize(),
                        metadata.getOffset()
                );

        List<String> manufacturers = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(manufacturers);
    }

    @Override
    public int getProductsNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().matchingTypesNumber(USER) == 1))
        );

        String query = selectCount().
                from("Products").
                where(filterMapper.toCondition(criteria.getFilter())).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public Optional<BigDecimal> getProductsSum(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> notNull(criteria.getFilter())).
                        and(() -> isTrue(criteria.getFilter().matchingTypesNumber(USER) == 1))
        );

        String query = select(sum(field("price", BigDecimal.class)).as("totalPrice")).
                from("Products").
                where(filterMapper.toCondition(criteria.getFilter())).
                getSQL();

        return Optional.ofNullable(
                statement.queryForObject(query, BigDecimal.class)
        );
    }


    private int getTagsNumber(Criteria criteria) {
        Validator.check("ProductRepository.criteria", notNull(criteria));

        String query = """
                select count(distinct ProductTags.tagValue)
                    from ProductTags
                    join Products on Products.productId = ProductTags.productId
                    where %s
                """.formatted(filterMapper.toCondition(criteria.tryGetFilter()));

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    private int getShopsNumber(Criteria criteria) {
        Validator.check("ProductRepository.criteria", notNull(criteria));

        String query = """
                select count(distinct Products.shop)
                    from Products
                    where %s
                """.formatted(filterMapper.toCondition(criteria.tryGetFilter()));

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    private int getGradesNumber(Criteria criteria) {
        Validator.check("ProductRepository.criteria", notNull(criteria));

        String query = """
                select count(distinct Products.grade)
                    from Products
                    where %s
                """.formatted(filterMapper.toCondition(criteria.getFilter()));

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    private int getCategoriesNumber(Criteria criteria) {
        Validator.check("ProductRepository.criteria", notNull(criteria));

        String query = """
                select count(distinct Products.category)
                    from Products
                    where %s
                """.formatted(filterMapper.toCondition(criteria.tryGetFilter()));

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    private int getManufacturersNumber(Criteria criteria) {
        Validator.check("ProductRepository.criteria", notNull(criteria));

        String query = """
                select count(distinct Products.manufacturer)
                    from Products
                    where %s
                """.formatted(filterMapper.toCondition(criteria.tryGetFilter()));

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }


    private void addNewProduct(Product product) {
        statement.update(
                """
                        INSERT INTO Products(
                          productId,
                          userId,
                          category,
                          
                          shop,
                          grade,
                          manufacturer,
                          
                          contextHash,
                          description,
                          imagePath,
                          
                          quantity,
                          unit,
                          price,
                          
                          packingSize
                        ) VALUES (?,?,?, ?,?,?, ?,?,?, ?,?,? ,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, product.getId());
                    ps.setObject(2, product.getUser().getId());
                    ps.setString(3, product.getContext().getCategory());
                    ps.setString(4, product.getContext().getShop());
                    ps.setString(5, product.getContext().getGrade());
                    ps.setString(6, product.getContext().getManufacturer());
                    ps.setString(7, product.getContext().hashKey());
                    ps.setString(8, product.getDescription());
                    ps.setString(9, product.getImageUrl() == null ? null : product.getImageUrl().toString());
                    ps.setBigDecimal(10, product.getQuantity());
                    ps.setString(11, product.getContext().getUnit());
                    ps.setBigDecimal(12, product.getContext().getPrice());
                    ps.setBigDecimal(13, product.getContext().getPackingSize());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO ProductTags(productId, tagValue, index)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    private final List<Tag> tags = product.getContext().getTags().asList();

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = tags.get(i);
                        ps.setObject(1, product.getId());
                        ps.setString(2, tag.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return tags.size();
                    }
                }
        );
    }

    private void updateProduct(Product newVersion) {
        statement.update(
                """
                        UPDATE Products SET
                          category=?,
                          shop=?,
                          grade=?,
                          manufacturer=?,
                          contextHash=?,
                          description=?,
                          imagePath=?,
                          quantity=?,
                          unit=?,
                          price=?,
                          packingSize=?
                          WHERE userId=? AND productId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, newVersion.getContext().getCategory());
                    ps.setString(2, newVersion.getContext().getShop());
                    ps.setString(3, newVersion.getContext().getGrade());
                    ps.setString(4, newVersion.getContext().getManufacturer());
                    ps.setString(5, newVersion.getContext().hashKey());
                    ps.setString(6, newVersion.getDescription());
                    ps.setString(7, newVersion.getImageUrl() == null ? null : newVersion.getImageUrl().toString());
                    ps.setBigDecimal(8, newVersion.getQuantity());
                    ps.setString(9, newVersion.getContext().getUnit());
                    ps.setBigDecimal(10, newVersion.getContext().getPrice());
                    ps.setBigDecimal(11, newVersion.getContext().getPackingSize());
                    ps.setObject(12, newVersion.getUser().getId());
                    ps.setObject(13, newVersion.getId());
                }
        );

        statement.update(
                """
                        DELETE FROM ProductTags
                          WHERE productId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, newVersion.getId());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO ProductTags(productId, tagValue, index)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = newVersion.getContext().getTags().get(i);
                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, tag.getValue());
                        ps.setObject(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getContext().getTags().size();
                    }
                }
        );
    }

    private boolean doesProductExist(UUID productId) {
        return statement.query(
                "select count(*) > 0 as doesProductExist from Products where productId = ?;",
                ps -> ps.setObject(1, productId),
                rs -> {
                    boolean result = false;
                    if(rs.next()) result = rs.getBoolean("doesProductExist");
                    return result;
                }
        );
    }

    private Optional<User> loadUser(UUID userId) {
        return statement.query("""
                select * from users where users.userId = ?;
                """,
                ps -> ps.setObject(1, userId),
                rs -> {
                    User result = null;

                    if(rs.next()) {
                        result = new User.LoadBuilder().
                                setId((UUID) rs.getObject("userId")).
                                setName(rs.getString("name")).
                                setEmail(rs.getString("email")).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }

                    return Optional.ofNullable(result);
                });
    }

    private void loadAndFillProductAggregateRoot(PreparedStatementCreator queryCreate,
                                                 ProductAggregateRootBuilders aggregateRootBuilders) {
        statement.query(
                queryCreate,
                (ResultSet rs) -> {
                    UUID productId = (UUID) rs.getObject("productId");

                    Product.Builder builder = new Product.Builder().
                            setAppConfiguration(conf).
                            setId(productId).
                            setCategory(rs.getString("category")).
                            setShop(rs.getString("shop")).
                            setGrade(rs.getString("grade")).
                            setManufacturer(rs.getString("manufacturer")).
                            setUnit(rs.getString("unit")).
                            setPrice(rs.getBigDecimal("price")).
                            setPackingSize(rs.getBigDecimal("packingSize")).
                            setQuantity(rs.getBigDecimal("quantity")).
                            setDescription(rs.getString("description")).
                            setImageUrl(rs.getString("imagePath"));

                    aggregateRootBuilders.products().add(builder);
                    aggregateRootBuilders.productsById().put(productId, builder);
                }
        );
    }

    private void loadAndFillProductTags(PreparedStatementCreator queryCreate,
                                        ProductAggregateRootBuilders aggregateRootBuilders) {
            statement.query(
                    queryCreate,
                    rs -> {
                        UUID productId = (UUID) rs.getObject("productId");
                        aggregateRootBuilders.productsById().get(productId).addTag(rs.getString("tagValue"));
                    });
    }


    private String getOrderFields(Sort productSort) {
        return productSort.getParametersAsStream().
                map(param -> "Products." + param.param() + " " + param.getDirectionAsString()).
                reduce((a, b) -> a + ", " + b).
                map(orderFields -> orderFields + ", Products.productId asc").
                orElse("Products.productId asc");
    }


    private record ProductAggregateRootBuilders(List<Product.Builder> products,
                                                Map<UUID, Product.Builder> productsById) {

        public ProductAggregateRootBuilders() {
            this(new ArrayList<>(), new HashMap<>());
        }

        public boolean isEmpty() {
            return products.isEmpty();
        }

    }

}

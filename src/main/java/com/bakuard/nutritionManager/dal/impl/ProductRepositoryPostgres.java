package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.impl.mappers.ProductFilterMapper;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        return statement.query(
                (Connection con) -> con.prepareStatement("""
                    SELECT Products.*, Users.*, ProductTags.*
                        FROM Products
                        LEFT JOIN Users
                          ON Products.userId = Users.userId
                        LEFT JOIN ProductTags
                          ON Products.productId = ProductTags.productId
                        WHERE Products.productId = ? AND Products.userId = ?
                        ORDER BY ProductTags.index;
                    """),
                (PreparedStatement ps) -> {
                    ps.setObject(1, productId);
                    ps.setObject(2, userId);
                },
                (ResultSet rs) -> {
                    Product.Builder builder = null;

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Product.Builder().
                                    setAppConfiguration(conf).
                                    setId(productId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userId")).
                                                    setName(rs.getString("name")).
                                                    setEmail(rs.getString("email")).
                                                    setPasswordHash(rs.getString("passwordHash")).
                                                    setSalt(rs.getString("salt")).
                                                    tryBuild()
                                    ).
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
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull()) builder.addTag(tagValue);
                    }

                    return builder == null ?
                            Optional.empty() :
                            Optional.ofNullable(builder.tryBuild());
                }
        );
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

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        List<String> fieldsName = new ArrayList<>();
        List<Field<?>> fields = new ArrayList<>();
        fields.add(field("*"));
        if(criteria.getFilter() != null) {
            List<Condition> conditions = filterMapper.toConditions(criteria.getFilter());
            for(int i = 0; i < conditions.size(); i++) {
                String fieldName = "condition" + i;
                fieldsName.add(fieldName);

                Field<?> field = field("(" + conditions.get(i) + ") as " + fieldName);
                fields.add(field);
            }
        }

        Condition condition = fieldsName.stream().
                map(field -> field(field).isTrue()).
                reduce(Condition::or).
                orElse(trueCondition());

        String query =
            select(field("*")).
                from(
                    select(field("*")).
                        from(
                            select(fields).
                                from("Products").
                                asTable("{LabeledProducts}")
                        ).
                        where(condition).
                        orderBy(getOrderFields(fieldsName, criteria.tryGetSort(), "LabeledProducts")).
                        limit(inline(metadata.getActualSize())).
                        offset(inline(metadata.getOffset())).
                        asTable("{P}")
                ).
                leftJoin("ProductTags").
                    on(field("P.productId").eq(field("ProductTags.productId"))).
                leftJoin("Users").
                    on(field("P.userId").eq(field("Users.userId"))).
                orderBy(getOrderFields(fieldsName, criteria.tryGetSort(), "P")).
                getSQL().
                replace("\"{P}\"", "as P").//Этот костыль исправляет странное поведение JOOQ в конструкциях asTable()
                replace("\"{LabeledProducts}\"", "as LabeledProducts");

        List<Product> products = statement.query(
                query,
                (ResultSet rs) -> {
                    List<Product> result = new ArrayList<>();

                    Product.Builder builder = null;
                    UUID lastProductId = null;
                    while(rs.next()) {
                        UUID productId = (UUID)rs.getObject("productId");
                        if(!productId.equals(lastProductId)) {
                            if(builder != null) result.add(builder.tryBuild());
                            builder = new Product.Builder().
                                    setAppConfiguration(conf).
                                    setId(productId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userId")).
                                                    setName(rs.getString("name")).
                                                    setEmail(rs.getString("email")).
                                                    setPasswordHash(rs.getString("passwordHash")).
                                                    setSalt(rs.getString("salt")).
                                                    tryBuild()
                                    ).
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
                            lastProductId = productId;
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull()) builder.addTag(tagValue);


                    }

                    if(builder != null) result.add(builder.tryBuild());

                    return result;
                }
        );

        return metadata.createPage(products);
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        int tagsNumber = getTagsNumber(criteria);
        Page.Metadata metadata = criteria.tryGetPageable(PageableByNumber.class).
                createPageMetadata(tagsNumber, conf.pagination().itemsMaxPageSize());

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("ProductTags.tagValue")).
                from("ProductTags").
                join("Products").
                    on(field("Products.productId").eq(field("ProductTags.productId"))).
                where(filterMapper.toCondition(criteria.getFilter())).
                orderBy(field("ProductTags.tagValue").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

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

        String query = selectDistinct(field("Products.shop")).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                orderBy(field("Products.shop").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

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

        String query = selectDistinct(field("Products.grade")).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                orderBy(field("Products.grade").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

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

        String query = selectDistinct(field("Products.category")).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                orderBy(field("Products.category").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

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

        String query = selectDistinct(field("Products.manufacturer")).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                orderBy(field("Products.manufacturer").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

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
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = selectCount().
                from("Products").
                where(filterMapper.toCondition(criteria.getFilter())).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("ProductTags.tagValue"))).
                from("ProductTags").
                join("Products").
                on(field("Products.productId").eq(field("ProductTags.productId"))).
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    @Override
    public int getShopsNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.getFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Products.shop"))).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    @Override
    public int getGradesNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Products.grade"))).
                from("Products").
                where(filterMapper.toCondition(criteria.getFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    @Override
    public int getCategoriesNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER) &&
                                criteria.tryGetFilter().containsOnly(USER)))
        );

        String query = select(countDistinct(field("Products.category"))).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    @Override
    public int getManufacturersNumber(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Products.manufacturer"))).
                from("Products").
                where(filterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }

    @Override
    public Optional<BigDecimal> getProductsSum(Criteria criteria) {
        Validator.check(
                "ProductRepository.criteria", notNull(criteria).
                        and(() -> notNull(criteria.getFilter())).
                        and(() -> isTrue(criteria.getFilter().containsAtLeast(USER)))
        );

        String query = select(sum(field("price", BigDecimal.class)).as("totalPrice")).
                from("Products").
                where(filterMapper.toCondition(criteria.getFilter())).
                getSQL();

        return Optional.ofNullable(
                statement.queryForObject(query, BigDecimal.class)
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



    private List<SortField<?>> getOrderFields(List<String> optionalFields,
                                              Sort productSort,
                                              String tableName) {
        ArrayList<SortField<?>> fields = new ArrayList<>();

        optionalFields.forEach(f -> fields.add(field(tableName + "." + f).desc()));

        for(int i = 0; i < productSort.getParametersNumber(); i++) {
            switch(productSort.getParameter(i)) {
                case "category" -> {
                    if(productSort.isAscending(i))
                        fields.add(field(tableName + ".category").asc());
                    else
                        fields.add(field(tableName + ".category").desc());
                }
                case "price" -> {
                    if(productSort.isAscending(i))
                        fields.add(field(tableName + ".price").asc());
                    else
                        fields.add(field(tableName + ".price").desc());
                }
            }
        }
        fields.add(field(tableName + ".productId").asc());
        if("P".equals(tableName)) {
            fields.add(field("ProductTags.index").asc());
        }
        return fields;
    }

}

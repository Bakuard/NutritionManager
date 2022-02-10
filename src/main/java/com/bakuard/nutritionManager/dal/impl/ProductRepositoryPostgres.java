package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.products.*;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.util.Page;

import com.google.common.collect.Sets;

import org.jooq.*;
import org.jooq.impl.DSL;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.*;

public class ProductRepositoryPostgres implements ProductRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;

    public ProductRepositoryPostgres(DataSource dataSource, AppConfigData appConfig) {
        statement = new JdbcTemplate(dataSource);
        this.appConfig = appConfig;
    }

    @Override
    public boolean save(Product product) {
        Checker checker = Checker.of(getClass(), "save").
                nullValue("product", product).
                checkWithServiceException("Fail to save product");

        Product oldProduct = getByIdOrReturnNull(product.getId());

        boolean newData = false;
        try {
            if (oldProduct == null) {
                addNewProduct(product);
                newData = true;
            } else if(!product.equalsFullState(oldProduct)) {
                updateProduct(product, oldProduct);
                newData = true;
            }
        } catch(DuplicateKeyException e) {
            throw checker.addConstraint("product", ConstraintType.ALREADY_EXISTS_IN_DB).
                    createServiceException("Fail to save product");
        }

        return newData;
    }

    @Override
    public Product remove(UUID productId) {
        Checker checker = Checker.of(getClass(), "remove").
                nullValue("productId", productId).
                checkWithServiceException("Fail to remove product. Unknown product with id=null");

        Product product = getByIdOrReturnNull(productId);

        if(product == null) {
             throw checker.
                    addConstraint("productId", ConstraintType.UNKNOWN_ENTITY).
                    createServiceException("Fail to remove product. Unknown product with id=" + productId);
        }

        statement.update(
                "DELETE FROM Products WHERE productId = ?;",
                (PreparedStatement ps) -> ps.setObject(1, productId)
        );

        return product;
    }

    @Override
    public Product getById(UUID productId) {
        Checker checker = Checker.of(getClass(), "getById").
                nullValue("productId", productId).
                checkWithServiceException("Fail to get product by id");

        Product product = getByIdOrReturnNull(productId);
        if(product == null) {
            throw checker.addConstraint("productId", ConstraintType.UNKNOWN_ENTITY).
                    createServiceException("Fail to get product by id");
        }

        return product;
    }

    @Override
    public Page<Product> getProducts(ProductCriteria criteria) {
        Checker.of(getClass(), "getProducts").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get product by criteria");

        Page.Info info = criteria.getPageable().
                createPageMetadata(getProductsNumber(criteria.getNumberCriteria()));

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.isOnlyFridge())
            condition = condition.and(onlyFridgeConstraint());
        if(criteria.getFilter().isPresent())
            condition = condition.and(switchConstraint(criteria.getFilter().get()));

        String query =
            select(table("P").fields()).
                select(table("ProductTags").fields()).
                from(
                    select(table("Products").fields()).
                        from("Products").
                        where(condition).
                        orderBy(getOrderFields(criteria.getProductSort(), "Products")).
                        limit(inline(info.getActualSize())).
                        offset(inline(info.getOffset())).
                        asTable("{P}")
                ).
                leftJoin("ProductTags").
                    on(field("P.productId").eq(field("ProductTags.productId"))).
                orderBy(getOrderFields(criteria.getProductSort(), "P")).
                getSQL().
                replace("\"{P}\"", "as P");//Этот костыль исправляет странное поведение JOOQ в конструкциях asTable()

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
                                    setAppConfiguration(appConfig).
                                    setId(productId).
                                    setUser(criteria.getUser()).
                                    setCategory(rs.getString("category")).
                                    setShop(rs.getString("shop")).
                                    setVariety(rs.getString("variety")).
                                    setManufacturer(rs.getString("manufacturer")).
                                    setUnit(rs.getString("unit")).
                                    setPrice(rs.getBigDecimal("price")).
                                    setPackingSize(rs.getBigDecimal("packingSize")).
                                    setQuantity(rs.getBigDecimal("quantity")).
                                    setDescription(rs.getString("description")).
                                    setImagePath(rs.getString("imagePath"));
                            lastProductId = productId;
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull()) builder.addTag(tagValue);
                    }

                    if(builder != null) result.add(builder.tryBuild());

                    return result;
                }
        );

        return info.createPage(products);
    }

    @Override
    public Page<Tag> getTags(ProductFieldCriteria criteria) {
        Checker.of(getClass(), "getTags").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get tags by criteria");

        Page.Info info = criteria.getPageable().createPageMetadata(
                getTagsNumber(criteria.getNumberCriteria())
        );
        if(info.isEmpty()) return info.createPage(List.of());

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = selectDistinct(field("ProductTags.tagValue")).
                from("ProductTags").
                join("Products").
                    on(field("Products.productId").eq(field("ProductTags.productId"))).
                where(condition).
                orderBy(field("ProductTags.tagValue").asc()).
                limit(inline(info.getActualSize())).
                offset(inline(info.getOffset())).
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

        return info.createPage(tags);
    }

    @Override
    public Page<String> getShops(ProductFieldCriteria criteria) {
        Checker.of(getClass(), "getShops").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get shops by criteria");

        Page.Info info = criteria.getPageable().createPageMetadata(
                getShopsNumber(criteria.getNumberCriteria())
        );
        if(info.isEmpty()) return info.createPage(List.of());

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = selectDistinct(field("Products.shop")).
                from("Products").
                where(condition).
                orderBy(field("Products.shop").asc()).
                limit(inline(info.getActualSize())).
                offset(inline(info.getOffset())).
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

        return info.createPage(shops);
    }

    @Override
    public Page<String> getVarieties(ProductFieldCriteria criteria) {
        Checker.of(getClass(), "getVarieties").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get varieties by criteria");

        Page.Info info = criteria.getPageable().createPageMetadata(
                getVarietiesNumber(criteria.getNumberCriteria())
        );
        if(info.isEmpty()) return info.createPage(List.of());

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = selectDistinct(field("Products.variety")).
                from("Products").
                where(condition).
                orderBy(field("Products.variety").asc()).
                limit(inline(info.getActualSize())).
                offset(inline(info.getOffset())).
                getSQL();

        List<String> varieties = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return info.createPage(varieties);
    }

    @Override
    public Page<String> getCategories(ProductCategoryCriteria criteria) {
        Checker.of(getClass(), "getCategories").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get categories by criteria");

        Page.Info info = criteria.getPageable().createPageMetadata(
                getCategoriesNumber(criteria.getNumberCriteria())
        );
        if(info.isEmpty()) return info.createPage(List.of());

        Condition condition = userConstraint(criteria.getUser());

        String query = selectDistinct(field("Products.category")).
                from("Products").
                where(condition).
                orderBy(field("Products.category").asc()).
                limit(inline(info.getActualSize())).
                offset(inline(info.getOffset())).
                getSQL();

        List<String> varieties = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return info.createPage(varieties);
    }

    @Override
    public Page<String> getManufacturers(ProductFieldCriteria criteria) {
        Checker.of(getClass(), "getManufacturers").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get manufacturers by criteria");

        Page.Info info = criteria.getPageable().createPageMetadata(
                getManufacturersNumber(criteria.getNumberCriteria())
        );
        if(info.isEmpty()) return info.createPage(List.of());

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = selectDistinct(field("Products.manufacturer")).
                from("Products").
                where(condition).
                orderBy(field("Products.manufacturer").asc()).
                limit(inline(info.getActualSize())).
                offset(inline(info.getOffset())).
                getSQL();

        List<String> varieties = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return info.createPage(varieties);
    }

    @Override
    public int getProductsNumber(ProductsNumberCriteria criteria) {
        Checker.of(getClass(), "getProductsNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get products number by criteria");

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.isOnlyFridge())
            condition = condition.and(onlyFridgeConstraint());
        if(criteria.getFilter().isPresent())
            condition = condition.and(switchConstraint(criteria.getFilter().get()));

        String query = selectCount().from("Products").
                where(condition).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public int getTagsNumber(ProductFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getTagsNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get tags number by criteria");

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = select(countDistinct(field("ProductTags.tagValue"))).
                from("ProductTags").
                join("Products").
                on(field("Products.productId").eq(field("ProductTags.productId"))).
                where(condition).
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
    public int getShopsNumber(ProductFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getShopsNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get shops number by criteria");

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = select(countDistinct(field("Products.shop"))).
                from("Products").
                where(condition).
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
    public int getVarietiesNumber(ProductFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getVarietiesNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get varieties number by criteria");

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = select(countDistinct(field("Products.variety"))).
                from("Products").
                where(condition).
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
    public int getCategoriesNumber(ProductCategoryNumberCriteria criteria) {
        Checker.of(getClass(), "getCategoriesNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get categories number by criteria");

        Condition condition = userConstraint(criteria.getUser());

        String query = select(countDistinct(field("Products.category"))).
                from("Products").
                where(condition).
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
    public int getManufacturersNumber(ProductFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getManufacturersNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get manufacturers number by criteria");

        Condition condition = userConstraint(criteria.getUser());
        if(criteria.getProductCategory().isPresent())
            condition = condition.and(categoryConstraint(criteria.getProductCategory().get()));

        String query = select(countDistinct(field("Products.manufacturer"))).
                from("Products").
                where(condition).
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
    public Optional<BigDecimal> getProductsSum(ProductSumCriteria criteria) {
        return Optional.empty();
    }


    private void addNewProduct(Product product) {
        statement.update(
                """
                        INSERT INTO Products(
                          productId,
                          userId,
                          category,
                          shop,
                          variety,
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
                    ps.setString(5, product.getContext().getVariety());
                    ps.setString(6, product.getContext().getManufacturer());
                    ps.setString(7, product.getContext().hashKey());
                    ps.setString(8, product.getDescription());
                    ps.setString(9, product.getImagePath());
                    ps.setBigDecimal(10, product.getQuantity());
                    ps.setString(11, product.getContext().getUnit());
                    ps.setBigDecimal(12, product.getContext().getPrice());
                    ps.setBigDecimal(13, product.getContext().getPackingSize());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO ProductTags(productId, tagValue)
                          VALUES(?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    private final List<Tag> tags = product.getContext().getTags().asList();

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = tags.get(i);
                        ps.setObject(1, product.getId());
                        ps.setString(2, tag.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return product.getContext().getTags().size();
                    }
                }
        );
    }

    private void updateProduct(Product newVersion, Product oldVersion) {
        statement.update(
                """
                        UPDATE Products SET
                          category=?,
                          shop=?,
                          variety=?,
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
                    ps.setString(3, newVersion.getContext().getVariety());
                    ps.setString(4, newVersion.getContext().getManufacturer());
                    ps.setString(5, newVersion.getContext().hashKey());
                    ps.setString(6, newVersion.getDescription());
                    ps.setString(7, newVersion.getImagePath());
                    ps.setBigDecimal(8, newVersion.getQuantity());
                    ps.setString(9, newVersion.getContext().getUnit());
                    ps.setBigDecimal(10, newVersion.getContext().getPrice());
                    ps.setBigDecimal(11, newVersion.getContext().getPackingSize());
                    ps.setObject(12, newVersion.getUser().getId());
                    ps.setObject(13, newVersion.getId());
                }
        );

        statement.batchUpdate(
                """
                        DELETE FROM ProductTags
                          WHERE productId=? AND tagValue=?;
                        """,
                new BatchPreparedStatementSetter() {

                    private final List<Tag> deletedTags = Sets.difference(
                            oldVersion.getContext().getTags(),
                            newVersion.getContext().getTags()
                    ).stream().toList();

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = deletedTags.get(i);
                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, tag.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return deletedTags.size();
                    }
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO ProductTags(productId, tagKey, tagValue)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    private final List<Tag> addedTags = Sets.difference(
                            newVersion.getContext().getTags(),
                            oldVersion.getContext().getTags()
                    ).stream().toList();

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = addedTags.get(i);
                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, tag.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return addedTags.size();
                    }
                }
        );
    }


    private Product getByIdOrReturnNull(UUID productId) {
        return statement.query(
                (Connection con) -> con.prepareStatement("""
                    SELECT Products.*, Users.*, ProductTags.*
                        FROM Products
                        LEFT JOIN Users
                          ON Products.userId = Users.userId
                        LEFT JOIN ProductTags
                          ON Products.productId = ProductTags.productId
                        WHERE Products.productId = ?;
                    """),
                (PreparedStatement ps) -> ps.setObject(1, productId),
                (ResultSet rs) -> {
                    Product.Builder builder = null;

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Product.Builder().
                                    setAppConfiguration(appConfig).
                                    setId(productId).
                                    setUser(new User(
                                            (UUID) rs.getObject("userId"),
                                            rs.getString("name"),
                                            rs.getString("passwordHash"),
                                            rs.getString("email"),
                                            rs.getString("salt")
                                    )).
                                    setCategory(rs.getString("category")).
                                    setShop(rs.getString("shop")).
                                    setVariety(rs.getString("variety")).
                                    setManufacturer(rs.getString("manufacturer")).
                                    setUnit(rs.getString("unit")).
                                    setPrice(rs.getBigDecimal("price")).
                                    setPackingSize(rs.getBigDecimal("packingSize")).
                                    setQuantity(rs.getBigDecimal("quantity")).
                                    setDescription(rs.getString("description")).
                                    setImagePath(rs.getString("imagePath"));
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull()) builder.addTag(tagValue);
                    }

                    return builder == null ? null : builder.tryBuild();
                }
        );
    }


    private Condition switchConstraint(Filter filter) {
        switch(filter.getType()) {
            case AND -> {
                return andConstraint((AndFilter) filter);
            }
            case MIN_TAGS -> {
                return minTags((MinTagsFilter) filter);
            }
            case CATEGORY -> {
                return categoryConstraint((CategoryFilter) filter);
            }
            case SHOPS -> {
                return shopsConstraint((ShopsFilter) filter);
            }
            case VARIETIES -> {
                return varietiesConstraint((VarietiesFilter) filter);
            }
            case MANUFACTURER -> {
                return manufacturerConstraint((ManufacturerFilter) filter);
            }
            default -> throw new UnsupportedOperationException(
                        "Unsupported operation for " + filter.getType() + " constraint");
        }
    }

    private Condition andConstraint(AndFilter andConstraint) {
        Condition condition = switchConstraint(andConstraint.getOperands().get(0));
        for(int i = 1; i < andConstraint.getOperands().size(); i++) {
            condition = condition.and(switchConstraint(andConstraint.getOperands().get(i)));
        }
        return condition;
    }

    private Condition minTags(MinTagsFilter minTagsFilter) {
        return field("Products.productId").in(
                select(field("ProductTags.productId")).
                        from("ProductTags").
                        where(field("ProductTags.tagValue").in(
                                minTagsFilter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("ProductTags.productId")).
                        having(count(field("ProductTags.productId")).eq(inline(minTagsFilter.getTags().size())))
        );
    }

    private Condition categoryConstraint(CategoryFilter categoryConstraint) {
        return field("Products.category").eq(inline(categoryConstraint.getCategory()));
    }

    private Condition categoryConstraint(String productName) {
        return field("Products.category").eq(inline(productName));
    }

    private Condition shopsConstraint(ShopsFilter shopsConstraint) {
        return field("Products.shop").in(
                shopsConstraint.getShops().stream().map(DSL::inline).toList()
        );
    }

    private Condition varietiesConstraint(VarietiesFilter varietiesConstraint) {
        return field("Products.variety").in(
                varietiesConstraint.getVarieties().stream().map(DSL::inline).toList()
        );
    }

    private Condition manufacturerConstraint(ManufacturerFilter constraint) {
        return field("Products.manufacturer").in(
                constraint.getManufacturers().stream().map(DSL::inline).toList()
        );
    }

    private Condition userConstraint(User user) {
        return field("Products.userId").eq(inline(user.getId()));
    }

    private Condition onlyFridgeConstraint() {
        return field("Products.quantity").greaterThan(inline(BigDecimal.ZERO));
    }


    private List<SortField<?>> getOrderFields(ProductSort productSort, String tableName) {
        ArrayList<SortField<?>> fields = new ArrayList<>();
        for(int i = 0; i < productSort.getCountParameters(); i++) {
            switch(productSort.getParameterType(i)) {
                case CATEGORY -> {
                    if(productSort.getDirection(i) == SortDirection.ASCENDING)
                        fields.add(field(tableName + ".category").asc());
                    else
                        fields.add(field(tableName + ".category").desc());
                }
                case SHOP -> {
                    if(productSort.getDirection(i) == SortDirection.ASCENDING)
                        fields.add(field(tableName + ".shop").asc());
                    else
                        fields.add(field(tableName + ".shop").desc());
                }
                case PRICE -> {
                    if(productSort.getDirection(i) == SortDirection.ASCENDING)
                        fields.add(field(tableName + ".price").asc());
                    else
                        fields.add(field(tableName + ".price").desc());
                }
                case VARIETY -> {
                    if(productSort.getDirection(i) == SortDirection.ASCENDING)
                        fields.add(field(".variety").asc());
                    else
                        fields.add(field(".variety").desc());
                }
                case MANUFACTURER -> {
                    if(productSort.getDirection(i) == SortDirection.ASCENDING)
                        fields.add(field(".manufacturer").asc());
                    else
                        fields.add(field(".manufacturer").desc());
                }
            }
        }
        fields.add(field(tableName + ".productId").asc());
        return fields;
    }

}

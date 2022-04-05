package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.DishIngredient;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import com.fasterxml.jackson.core.*;

import org.jooq.Condition;
import org.jooq.Param;
import org.jooq.SortField;
import org.jooq.impl.DSL;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bakuard.nutritionManager.model.filters.Filter.Type.*;
import static org.jooq.impl.DSL.*;

public class DishRepositoryPostgres implements DishRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;
    private ProductRepositoryPostgres productRepository;
    private JsonFactory jsonFactory;

    public DishRepositoryPostgres(DataSource dataSource,
                                  AppConfigData appConfig,
                                  ProductRepositoryPostgres productRepository) {
        this.appConfig = appConfig;
        this.productRepository = productRepository;
        statement = new JdbcTemplate(dataSource);
        jsonFactory = new JsonFactory();
    }

    @Override
    public boolean save(Dish dish) {
        ValidateException.check(
                Rule.of("DishRepository.dish").notNull(dish)
        );

        Dish oldDish = getByIdOrReturnNull(dish.getId());

        boolean newData = false;
        try {
            if(oldDish == null) {
                addNewDish(dish);
                newData = true;
            } else if(!dish.equalsFullState(oldDish)) {
                updateDish(dish);
                newData = true;
            }
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save dish").
                    addReason(Rule.of("DishRepository.dish").failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB));
        }

        return newData;
    }

    @Override
    public Dish remove(UUID dishId) {
        ValidateException.check(
                Rule.of("DishRepository.dishId").notNull(dishId)
        );

        Dish dish = getByIdOrReturnNull(dishId);

        if(dish == null) {
            throw new ValidateException("Fail to remove dish. Unknown dish with id=" + dishId).
                    addReason(Rule.of("DishRepository.dishId").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
        }

        statement.update(
                "DELETE FROM Dishes WHERE dishId = ?;",
                (PreparedStatement ps) -> ps.setObject(1, dishId)
        );

        return dish;
    }

    @Override
    public Dish getById(UUID dishId) {
        ValidateException.check(
                Rule.of("DishRepository.dishId").notNull(dishId)
        );

        Dish dish = getByIdOrReturnNull(dishId);
        if(dish == null) {
            throw new ValidateException("Fail to get dish by id=" + dishId).
                    addReason(Rule.of("DishRepository.dishId").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
        }

        return dish;
    }

    @Override
    public Dish getByName(String name) {
        ValidateException.check(
                Rule.of("DishRepository.name").notNull(name)
        );

        return statement.query(
                (Connection con) -> con.prepareStatement(
                        """
                                SELECT Dishes.*, DishTags.*,
                                       Users.userId,
                                       Users.name as userName,
                                       Users.passwordHash as userPassHash,
                                       Users.email as userEmail,
                                       Users.salt as userSalt,
                                       DishIngredients.name as ingredientName,
                                       DishIngredients.quantity as ingredientQuantity,
                                       DishIngredients.filter as ingredientFilter
                                    FROM Dishes
                                    LEFT JOIN Users
                                        ON Dishes.userId = Users.userId
                                    LEFT JOIN DishTags
                                        ON Dishes.dishId = DishTags.dishId
                                    LEFT JOIN DishIngredients
                                        ON Dishes.dishId = DishIngredients.dishId
                                    WHERE Dishes.name = ?
                                    ORDER BY DishTags.index, DishIngredients.index;
                                """
                ),
                (PreparedStatement ps) -> ps.setObject(1, name),
                (ResultSet rs) -> {
                    Dish.Builder builder = null;

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Dish.Builder().
                                    setId((UUID) rs.getObject("dishId")).
                                    setUser(new User(
                                            (UUID) rs.getObject("userId"),
                                            rs.getString("userName"),
                                            rs.getString("userPassHash"),
                                            rs.getString("userEmail"),
                                            rs.getString("userSalt")
                                    )).
                                    setName(name).
                                    setServingSize(rs.getBigDecimal("servingSize")).
                                    setUnit(rs.getString("unit")).
                                    setDescription(rs.getString("description")).
                                    setImagePath(rs.getString("imagePath")).
                                    setConfig(appConfig).
                                    setRepository(productRepository);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !builder.containsTag(tagValue)) builder.addTag(tagValue);

                        BigDecimal ingredientQuantity = rs.getBigDecimal("ingredientQuantity");
                        if(!rs.wasNull()) {
                            String ingredientName = rs.getString("ingredientName");
                            Filter filter = toFilter(rs.getString("ingredientFilter"));
                            if(!builder.containsIngredient(ingredientName, filter, ingredientQuantity))
                                builder.addIngredient(ingredientName, filter, ingredientQuantity);
                        }
                    }

                    if(builder != null) {
                        return builder.tryBuild();
                    } else {
                        throw new ValidateException("Fail to get dish by name=" + name).
                                addReason(Rule.of("DishRepository.name").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
                    }
                }
        );
    }

    @Override
    public Page<Dish> getDishes(Criteria criteria) {
        int dishesNumber = getDishesNumber(criteria);
        Page.Metadata metadata = criteria.getPageable().
                createPageMetadata(dishesNumber, 30);

        if(metadata.isEmpty()) return Pageable.firstEmptyPage();

        String query =
                select(field("D.*"),
                        field("DishIngredients.name as ingredientName"),
                        field("DishIngredients.quantity as ingredientQuantity"),
                        field("DishIngredients.filter as ingredientFilter"),
                        field("DishTags.tagValue as tagValue"),
                        field("Users.userId as userId"),
                        field("Users.name as userName"),
                        field("Users.email as userEmail"),
                        field("Users.passwordHash as userPasswordHash"),
                        field("Users.salt as userSalt")).
                        from(
                            select(field("*")).
                                from("Dishes").
                                where(switchFilter(criteria.getFilter())).
                                orderBy(getOrderFields(criteria.getSort(), "Dishes")).
                                limit(inline(metadata.getActualSize())).
                                offset(inline(metadata.getOffset())).
                                asTable("{D}")
                        ).
                        leftJoin("DishTags").
                            on(field("D.dishId").eq(field("DishTags.dishId"))).
                        leftJoin("DishIngredients").
                            on(field("D.dishId").eq(field("DishIngredients.dishId"))).
                        leftJoin("Users").
                            on(field("D.userId").eq(field("Users.userId"))).
                        orderBy(getOrderFields(criteria.getSort(), "D")).
                        getSQL().
                        replace("\"{D}\"", "as D");

        List<Dish> dishes = statement.query(
                query,
                (ResultSet rs) -> {
                    List<Dish> result = new ArrayList<>();

                    Dish.Builder builder = null;
                    UUID lastDishId = null;
                    while(rs.next()) {
                        UUID dishId = (UUID)rs.getObject("dishId");
                        if(!dishId.equals(lastDishId)) {
                            if (builder != null) result.add(builder.tryBuild());
                            builder = new Dish.Builder().
                                    setId(dishId).
                                    setUser(
                                            new User.Builder().
                                                    setId((UUID) rs.getObject("userId")).
                                                    setName(rs.getString("userName")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setPasswordHash(rs.getString("userPasswordHash")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(rs.getString("name")).
                                    setServingSize(rs.getBigDecimal("servingSize")).
                                    setUnit(rs.getString("unit")).
                                    setDescription(rs.getString("description")).
                                    setImagePath(rs.getString("imagePath")).
                                    setConfig(appConfig).
                                    setRepository(productRepository);
                            lastDishId = dishId;
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !builder.containsTag(tagValue)) builder.addTag(tagValue);

                        BigDecimal ingredientQuantity = rs.getBigDecimal("ingredientQuantity");
                        if(!rs.wasNull()) {
                            String ingredientName = rs.getString("ingredientName");
                            Filter filter = toFilter(rs.getString("ingredientFilter"));
                            if(!builder.containsIngredient(ingredientName, filter, ingredientQuantity))
                                builder.addIngredient(ingredientName, filter, ingredientQuantity);
                        }
                    }

                    if(builder != null) result.add(builder.tryBuild());

                    return result;
                }
        );

        return metadata.createPage(dishes);
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        int tagsNumber = getTagsNumber(criteria);
        Page.Metadata metadata = criteria.getPageable().
                createPageMetadata(tagsNumber, 200);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("DishTags.tagValue")).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
                where(switchFilter(criteria.getFilter())).
                orderBy(field("DishTags.tagValue").asc()).
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
    public Page<String> getUnits(Criteria criteria) {
        int unitsNumber = getUnitsNumber(criteria);
        Page.Metadata metadata = criteria.getPageable().
                createPageMetadata(unitsNumber, 200);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("Dishes.unit")).
                from("Dishes").
                where(switchFilter(criteria.getFilter())).
                orderBy(field("Dishes.unit").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

        List<String> units = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(units);
    }

    @Override
    public int getDishesNumber(Criteria criteria) {
        ValidateException.check(
                Rule.of("DishRepository.criteria").notNull(criteria).
                        and(r -> r.notNull(criteria.getFilter(), "filter")).
                        and(r -> r.isTrue(criteria.getFilter().containsAtLeast(USER)))
        );

        String query = selectCount().
                from("Dishes").
                where(switchFilter(criteria.getFilter())).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        ValidateException.check(
                Rule.of("DishRepository.criteria").notNull(criteria).
                        and(r -> r.notNull(criteria.getFilter(), "filter")).
                        and(r -> r.isTrue(criteria.getFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("DishTags.tagValue"))).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
                where(switchFilter(criteria.getFilter())).
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
    public int getUnitsNumber(Criteria criteria) {
        ValidateException.check(
                Rule.of("DishRepository.criteria").notNull(criteria).
                        and(r -> r.notNull(criteria.getFilter(), "filter")).
                        and(r -> r.isTrue(criteria.getFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Dishes.unit"))).
                from("Dishes").
                where(switchFilter(criteria.getFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }


    private void addNewDish(Dish dish) {
        statement.update(
                """
                        INSERT INTO Dishes (
                            dishId,
                            userId,
                            name,
                            servingSize,
                            unit,
                            description,
                            imagePath
                        ) VALUES (?,?,?, ?,?,?,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, dish.getId());
                    ps.setObject(2, dish.getUser().getId());
                    ps.setString(3, dish.getName());
                    ps.setBigDecimal(4, dish.getServingSize());
                    ps.setString(5, dish.getUnit());
                    ps.setString(6, dish.getDescription());
                    ps.setString(7, dish.getImageUrl() == null ? null : dish.getImageUrl().toString());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO DishTags(dishId, tagValue, index)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = dish.getTags().get(i);
                        ps.setObject(1, dish.getId());
                        ps.setString(2, tag.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return dish.getTags().size();
                    }

                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO DishIngredients(dishId, name, quantity, filter, filterQuery, index)
                          VALUES(?,?,?,jsonb(?),?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = dish.getIngredients().get(i);
                        String filterQuery = select(field("*")).
                                from(table("Products")).
                                where(productRepository.switchFilter(ingredient.getFilter())).
                                getSQL();

                        ps.setObject(1, dish.getId());
                        ps.setString(2, ingredient.getName());
                        ps.setBigDecimal(3, ingredient.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setString(4, toJson(ingredient.getFilter()));
                        ps.setString(5, filterQuery);
                        ps.setInt(6, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return dish.getIngredients().size();
                    }

                }
        );
    }

    private void updateDish(Dish newVersion) {
        statement.update(
                """
                        UPDATE Dishes SET
                            name=?,
                            servingSize=?,
                            unit=?,
                            description=?,
                            imagePath=?
                        WHERE userId=? AND dishId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, newVersion.getName());
                    ps.setBigDecimal(2, newVersion.getServingSize());
                    ps.setString(3, newVersion.getUnit());
                    ps.setString(4, newVersion.getDescription());
                    ps.setString(5, newVersion.getImageUrl() == null ? null : newVersion.getImageUrl().toString());
                    ps.setObject(6, newVersion.getUser().getId());
                    ps.setObject(7, newVersion.getId());
                }
        );

        statement.update(
                """
                    DELETE FROM DishTags
                        WHERE DishTags.dishId=?;
                    """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, newVersion.getId());
                }
        );

        statement.update(
                """
                    DELETE FROM DishIngredients
                        WHERE DishIngredients.dishId=?;
                    """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, newVersion.getId());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO DishTags(dishId, tagValue, index)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = newVersion.getTags().get(i);
                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, tag.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getTags().size();
                    }

                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO DishIngredients(dishId, name, quantity, filter, filterQuery, index)
                          VALUES(?,?,?,jsonb(?),?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = newVersion.getIngredients().get(i);
                        String filterQuery = select(field("*")).
                                from(table("Products")).
                                where(productRepository.switchFilter(ingredient.getFilter())).
                                getSQL();

                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, ingredient.getName());
                        ps.setBigDecimal(3, ingredient.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setString(4, toJson(ingredient.getFilter()));
                        ps.setString(5, filterQuery);
                        ps.setInt(6, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getIngredients().size();
                    }

                }
        );
    }


    private Dish getByIdOrReturnNull(UUID dishId) {
        return statement.query(
                (Connection con) -> con.prepareStatement(
                        """
                                SELECT Dishes.*, DishTags.*,
                                       Users.userId,
                                       Users.name as userName,
                                       Users.passwordHash as userPassHash,
                                       Users.email as userEmail,
                                       Users.salt as userSalt,
                                       DishIngredients.name as ingredientName,
                                       DishIngredients.quantity as ingredientQuantity,
                                       DishIngredients.filter as ingredientFilter
                                    FROM Dishes
                                    LEFT JOIN Users
                                        ON Dishes.userId = Users.userId
                                    LEFT JOIN DishTags
                                        ON Dishes.dishId = DishTags.dishId
                                    LEFT JOIN DishIngredients
                                        ON Dishes.dishId = DishIngredients.dishId
                                    WHERE Dishes.dishId = ?
                                    ORDER BY DishTags.index, DishIngredients.index;
                                """
                ),
                (PreparedStatement ps) -> ps.setObject(1, dishId),
                (ResultSet rs) -> {
                    Dish.Builder builder = null;

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Dish.Builder().
                                    setId(dishId).
                                    setUser(
                                            new User(
                                            (UUID) rs.getObject("userId"),
                                            rs.getString("userName"),
                                            rs.getString("userPassHash"),
                                            rs.getString("userEmail"),
                                            rs.getString("userSalt")
                                    )).
                                    setName(rs.getString("name")).
                                    setServingSize(rs.getBigDecimal("servingSize")).
                                    setUnit(rs.getString("unit")).
                                    setDescription(rs.getString("description")).
                                    setImagePath(rs.getString("imagePath")).
                                    setConfig(appConfig).
                                    setRepository(productRepository);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !builder.containsTag(tagValue)) builder.addTag(tagValue);

                        BigDecimal ingredientQuantity = rs.getBigDecimal("ingredientQuantity");
                        if(!rs.wasNull()) {
                            String ingredientName = rs.getString("ingredientName");
                            Filter filter = toFilter(rs.getString("ingredientFilter"));
                            if(!builder.containsIngredient(ingredientName, filter, ingredientQuantity))
                                builder.addIngredient(ingredientName, filter, ingredientQuantity);
                        }
                    }

                    return builder == null ? null : builder.tryBuild();
                }
        );
    }


    private Condition switchFilter(Filter filter) {
        switch(filter.getType()) {
            case AND -> {
                return andFilter((AndFilter) filter);
            }
            case MIN_TAGS -> {
                return minTagsFilter((MinTagsFilter) filter);
            }
            case INGREDIENTS -> {
                return ingredientsFilter((AnyFilter) filter);
            }
            case USER -> {
                return userFilter((UserFilter) filter);
            }
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = switchFilter(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(switchFilter(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition userFilter(UserFilter filter) {
        return field("userId").eq(inline(filter.getUserId()));
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("dishId").in(
                select(field("DishTags.dishId")).
                        from("DishTags").
                        where(field("DishTags.tagValue").in(
                                filter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("DishTags.dishId")).
                        having(count(field("DishTags.dishId")).eq(inline(filter.getTags().size())))
        );
    }

    private Condition ingredientsFilter(AnyFilter filter) {
        List<Param<String>> arrayData = filter.getValues().stream().
                map(DSL::inline).
                toList();

        return field("dishId").in(
                select(field("DishIngredients.dishId")).
                        from(table("DishIngredients")).
                        where(
                                "existProductsForFilter(?, DishIngredients.filterQuery)",
                                array(arrayData)
                        )
        );
    }


    private Filter toFilter(String json) {
        try {
            JsonParser parser = jsonFactory.createParser(json);
            Filter result = switchFilter(parser);
            parser.close();

            return result;
        } catch(IOException e) {
            throw new IllegalStateException("Fail to parse json to filter", e);
        }
    }

    private Filter switchFilter(JsonParser parser) throws IOException {
        Filter result = null;

        Filter.Type type = null;
        while(parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();

            if("type".equals(fieldName)) {
                type = Filter.Type.valueOf(parser.nextTextValue());
            }

            if("values".equals(fieldName)) {
                switch(type) {
                    case OR_ELSE -> result = toOrElseFilter(parser);
                    case AND -> result = toAndFilter(parser);
                    case MIN_TAGS -> result = toMinTagsFilter(parser);
                    case CATEGORY -> result = toCategoryFilter(parser);
                    case SHOPS -> result = toShopsFilter(parser);
                    case GRADES -> result = toGradesFilter(parser);
                    case MANUFACTURER -> result = toManufacturerFilter(parser);
                    case USER -> result = toUserFilter(parser);
                }
            }
        }

        return result;
    }

    private OrElseFilter toOrElseFilter(JsonParser parser) throws IOException {
        List<Filter> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(switchFilter(parser));
        }

        return Filter.orElse(values);
    }

    private AndFilter toAndFilter(JsonParser parser) throws IOException {
        List<Filter> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(switchFilter(parser));
        }

        return Filter.and(values);
    }

    private AnyFilter toCategoryFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyCategory(values);
    }

    private AnyFilter toManufacturerFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyManufacturer(values);
    }

    private AnyFilter toShopsFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyShop(values);
    }

    private AnyFilter toGradesFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyGrade(values);
    }

    private MinTagsFilter toMinTagsFilter(JsonParser parser) throws IOException {
        List<Tag> tags = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            tags.add(new Tag(parser.getValueAsString()));
        }

        return Filter.minTags(tags);
    }

    private UserFilter toUserFilter(JsonParser parser) throws IOException {
        parser.nextToken(); //BEGIN_ARRAY
        UUID userId = UUID.fromString(parser.nextTextValue());
        parser.nextToken(); //END_ARRAY

        return Filter.user(userId);
    }


    private String toJson(Filter filter) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            JsonGenerator writer = jsonFactory.createGenerator(buffer, JsonEncoding.UTF8);

            switchFilter(filter, writer);
            writer.close();

            writer.flush();

            return buffer.toString();
        } catch(IOException e) {
            throw new IllegalStateException("Fail to covert filter to json", e);
        }
    }

    private void switchFilter(Filter filter, JsonGenerator writer) throws IOException {
        switch(filter.getType()) {
            case OR_ELSE -> toJson((OrElseFilter) filter, writer);
            case AND -> toJson((AndFilter) filter, writer);
            case MIN_TAGS -> toJson((MinTagsFilter) filter, writer);
            case CATEGORY, SHOPS, GRADES, MANUFACTURER -> toJson((AnyFilter) filter, writer);
            case USER -> toJson((UserFilter) filter, writer);
        }
    }

    private void toJson(OrElseFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Filter f : filter.getOperands()) switchFilter(f, writer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(AndFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Filter f : filter.getOperands()) switchFilter(f, writer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(AnyFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(String manufacturer : filter.getValues()) writer.writeString(manufacturer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(MinTagsFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Tag tag : filter.getTags()) writer.writeString(tag.getValue());
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(UserFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        writer.writeString(filter.getUserId().toString());
        writer.writeEndArray();

        writer.writeEndObject();
    }


    private List<SortField<?>> getOrderFields(Sort dishSort,
                                              String tableName) {
        ArrayList<SortField<?>> fields = new ArrayList<>();

        for(int i = 0; i < dishSort.getParametersNumber(); i++) {
            switch(dishSort.getParameter(i)) {
                case "name" -> {
                    if(dishSort.isAscending(i))
                        fields.add(field(tableName + ".name").asc());
                    else
                        fields.add(field(tableName + ".name").desc());
                }
            }
        }
        fields.add(field(tableName + ".dishId").asc());
        if("D".equals(tableName)) {
            fields.add(field("DishIngredients.index").asc());
            fields.add(field("DishTags.index").asc());
        }
        return fields;
    }

}

package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishFieldCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishFieldNumberCriteria;
import com.bakuard.nutritionManager.dal.criteria.dishes.DishesNumberCriteria;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.DishIngredient;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.Checker;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.fasterxml.jackson.core.*;
import org.jooq.Condition;
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

import static org.jooq.impl.DSL.*;

public class DishRepositoryPostgres implements DishRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;
    private ProductRepository productRepository;
    private JsonFactory jsonFactory;

    public DishRepositoryPostgres(DataSource dataSource,
                                  AppConfigData appConfig,
                                  ProductRepository productRepository) {
        this.appConfig = appConfig;
        this.productRepository = productRepository;
        statement = new JdbcTemplate(dataSource);
        jsonFactory = new JsonFactory();
    }

    @Override
    public boolean save(Dish dish) {
        Checker checker = Checker.of(getClass(), "save").
                nullValue("dish", dish).
                checkWithServiceException("Fail to save dish");

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
            throw checker.addConstraint("dish", ConstraintType.ALREADY_EXISTS_IN_DB).
                    createServiceException("Fail to save dish", e);
        }

        return newData;
    }

    @Override
    public Dish remove(UUID dishId) {
        Checker checker = Checker.of(getClass(), "remove").
                nullValue("dishId", dishId).
                checkWithServiceException("Fail to remove dish. Unknown dish with id=null");

        Dish dish = getByIdOrReturnNull(dishId);

        if(dish == null) {
            throw checker.
                    addConstraint("dishId", ConstraintType.UNKNOWN_ENTITY).
                    createServiceException("Fail to remove dish. Unknown dish with id=" + dishId);
        }

        statement.update(
                "DELETE FROM Dishes WHERE dishId = ?;",
                (PreparedStatement ps) -> ps.setObject(1, dishId)
        );

        return dish;
    }

    @Override
    public Dish getById(UUID dishId) {
        Checker checker = Checker.of(getClass(), "getById").
                nullValue("dishId", dishId).
                checkWithServiceException("Fail to get dish by id");

        Dish dish = getByIdOrReturnNull(dishId);
        if(dish == null) {
            throw checker.addConstraint("dishId", ConstraintType.UNKNOWN_ENTITY).
                    createServiceException("Fail to get dish by id=" + dishId);
        }

        return dish;
    }

    @Override
    public Page<Dish> getDishes(DishCriteria criteria) {
        return null;
    }

    @Override
    public Page<Tag> getTags(DishFieldCriteria criteria) {
        Checker.of(getClass(), "getTags").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get dishes tags by criteria");

        Page.Metadata metadata = criteria.getPageable().createPageMetadata(
                getTagsNumber(criteria.getNumberCriteria()), 200
        );
        if(metadata.isEmpty()) return metadata.createPage(List.of());

        Condition condition = userFilter(criteria.getUser());

        String query = selectDistinct(field("DishTags.tagValue")).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
                where(condition).
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
    public Page<String> getUnits(DishFieldCriteria criteria) {
        Checker.of(getClass(), "getUnits").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get dishes shops by criteria");

        Page.Metadata metadata = criteria.getPageable().createPageMetadata(
                getUnitsNumber(criteria.getNumberCriteria()), 200
        );
        if(metadata.isEmpty()) return metadata.createPage(List.of());

        Condition condition = userFilter(criteria.getUser());

        String query = selectDistinct(field("Dishes.unit")).
                from("Dishes").
                where(condition).
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
    public int getDishesNumber(DishesNumberCriteria criteria) {
        
        return 0;
    }

    @Override
    public int getTagsNumber(DishFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getTagsNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get dishes tags number by criteria");

        Condition condition = userFilter(criteria.getUser());

        String query = select(countDistinct(field("DishTags.tagValue"))).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
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
    public int getUnitsNumber(DishFieldNumberCriteria criteria) {
        Checker.of(getClass(), "getUnitsNumber").
                nullValue("criteria", criteria).
                checkWithServiceException("Fail to get dishes units number by criteria");

        Condition condition = userFilter(criteria.getUser());

        String query = select(countDistinct(field("Dishes.unit"))).
                from("Dishes").
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


    private void addNewDish(Dish dish) {
        statement.update(
                """
                        INSERT INTO Dishes (
                            dishId,
                            userId,
                            name,
                            unit,
                            description,
                            imagePath
                        ) VALUES (?,?,?, ?,?,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, dish.getId());
                    ps.setObject(2, dish.getUser().getId());
                    ps.setString(3, dish.getName());
                    ps.setString(4, dish.getUnit());
                    ps.setString(5, dish.getDescription());
                    ps.setString(6, dish.getImagePath());
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
                        INSERT INTO DishIngredients(dishId, name, quantity, filter, index)
                          VALUES(?,?,?,jsonb(?),?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = dish.getIngredients().get(i);
                        ps.setObject(1, dish.getId());
                        ps.setString(2, ingredient.getName());
                        ps.setBigDecimal(3, ingredient.getNecessaryQuantity());
                        ps.setString(4, toJson(ingredient.getFilter()));
                        ps.setInt(5, i);
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
                            unit=?,
                            description=?,
                            imagePath=?
                        WHERE userId=? AND dishId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, newVersion.getName());
                    ps.setString(2, newVersion.getUnit());
                    ps.setString(3, newVersion.getDescription());
                    ps.setString(4, newVersion.getImagePath());
                    ps.setObject(5, newVersion.getUser().getId());
                    ps.setObject(6, newVersion.getId());
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
                        INSERT INTO DishIngredients(dishId, name, quantity, filter, index)
                          VALUES(?,?,?,jsonb(?),?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = newVersion.getIngredients().get(i);
                        ps.setObject(1, newVersion.getId());
                        ps.setString(2, ingredient.getName());
                        ps.setBigDecimal(3, ingredient.getNecessaryQuantity());
                        ps.setString(4, toJson(ingredient.getFilter()));
                        ps.setInt(5, i);
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
                                    setUser(new User(
                                            (UUID) rs.getObject("userId"),
                                            rs.getString("userName"),
                                            rs.getString("userPassHash"),
                                            rs.getString("userEmail"),
                                            rs.getString("userSalt")
                                    )).
                                    setName(rs.getString("name")).
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


    private Condition userFilter(User user) {
        return field("userId").eq(inline(user.getId()));
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
                    case VARIETIES -> result = toVarietiesFilter(parser);
                    case MANUFACTURER -> result = toManufacturerFilter(parser);
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

    private AnyFilter toVarietiesFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyVariety(values);
    }

    private MinTagsFilter toMinTagsFilter(JsonParser parser) throws IOException {
        List<Tag> tags = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            tags.add(new Tag(parser.getValueAsString()));
        }

        return Filter.minTags(tags);
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
            case CATEGORY, SHOPS, VARIETIES, MANUFACTURER -> toJson((AnyFilter) filter, writer);
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

}

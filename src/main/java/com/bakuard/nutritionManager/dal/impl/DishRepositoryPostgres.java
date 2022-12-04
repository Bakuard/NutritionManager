package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.impl.mappers.DishFilterMapper;
import com.bakuard.nutritionManager.dal.impl.mappers.ProductFilterJsonMapper;
import com.bakuard.nutritionManager.dal.impl.mappers.ProductFilterMapper;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.DishIngredient;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

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
import java.util.*;

import static com.bakuard.nutritionManager.model.filters.Filter.Type.USER;
import static com.bakuard.nutritionManager.validation.Rule.*;
import static org.jooq.impl.DSL.*;

public class DishRepositoryPostgres implements DishRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;
    private ProductRepositoryPostgres productRepository;
    private ProductFilterMapper filterMapper;
    private ProductFilterJsonMapper filterJsonMapper;
    private DishFilterMapper dishFilterMapper;

    public DishRepositoryPostgres(DataSource dataSource,
                                  AppConfigData appConfig,
                                  ProductRepositoryPostgres productRepository) {
        this.appConfig = appConfig;
        this.productRepository = productRepository;
        statement = new JdbcTemplate(dataSource);
        filterMapper = new ProductFilterMapper();
        filterJsonMapper = new ProductFilterJsonMapper();
        dishFilterMapper = new DishFilterMapper();
    }

    @Override
    public void save(Dish dish) {
        Validator.check("DishRepository.dish", notNull(dish));

        try {
            if(doesDishExist(dish.getId())) updateDish(dish);
            else addNewDish(dish);
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save dish", e).
                    addReason(Rule.of("DishRepository.dish", failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB)));
        }
    }

    @Override
    public Optional<Dish> getById(UUID userId, UUID dishId) {
        Validator.check(
                "DishRepository.userId", notNull(userId),
                "DishRepository.dishId", notNull(dishId)
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
                                       DishIngredients.ingredientId as ingredientId,
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
                                    WHERE Dishes.dishId = ? AND Dishes.userId = ?
                                    ORDER BY DishTags.index, DishIngredients.index;
                                """
                ),
                (PreparedStatement ps) -> {
                    ps.setObject(1, dishId);
                    ps.setObject(2, userId);
                },
                (ResultSet rs) -> {
                    Dish.Builder builder = null;
                    HashSet<String> tags = new HashSet<>();
                    HashSet<UUID> ingredients = new HashSet<>();

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Dish.Builder().
                                    setId(dishId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userId")).
                                                    setName(rs.getString("userName")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setPasswordHash(rs.getString("userPassHash")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(rs.getString("name")).
                                    setServingSize(rs.getBigDecimal("servingSize")).
                                    setUnit(rs.getString("unit")).
                                    setDescription(rs.getString("description")).
                                    setImageUrl(rs.getString("imagePath")).
                                    setConfig(appConfig).
                                    setRepository(productRepository);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !tags.contains(tagValue)) {
                            builder.addTag(tagValue);
                            tags.add(tagValue);
                        }

                        UUID ingredientId = (UUID) rs.getObject("ingredientId");
                        if(!rs.wasNull()) {
                            if(!ingredients.contains(ingredientId)) {
                                builder.addIngredient(
                                        new DishIngredient.Builder().
                                                setId(ingredientId).
                                                setName(rs.getString("ingredientName")).
                                                setQuantity(rs.getBigDecimal("ingredientQuantity")).
                                                setFilter(filterJsonMapper.toFilter(rs.getString("ingredientFilter"))).
                                                setConfig(appConfig)
                                );
                                ingredients.add(ingredientId);
                            }
                        }
                    }

                    return builder == null ?
                            Optional.empty() :
                            Optional.ofNullable(builder.tryBuild());
                }
        );
    }

    @Override
    public Optional<Dish> getByName(UUID userId, String name) {
        Validator.check(
                "DishRepository.userId", notNull(userId),
                "DishRepository.name", notNull(name)
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
                                       DishIngredients.ingredientId as ingredientId,
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
                                    WHERE Dishes.name = ? AND Dishes.userId = ?
                                    ORDER BY DishTags.index, DishIngredients.index;
                                """
                ),
                (PreparedStatement ps) -> {
                    ps.setObject(1, name);
                    ps.setObject(2, userId);
                },
                (ResultSet rs) -> {
                    Dish.Builder builder = null;
                    HashSet<String> tags = new HashSet<>();
                    HashSet<UUID> ingredients = new HashSet<>();

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Dish.Builder().
                                    setId((UUID) rs.getObject("dishId")).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userId")).
                                                    setName(rs.getString("userName")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setPasswordHash(rs.getString("userPassHash")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(name).
                                    setServingSize(rs.getBigDecimal("servingSize")).
                                    setUnit(rs.getString("unit")).
                                    setDescription(rs.getString("description")).
                                    setImageUrl(rs.getString("imagePath")).
                                    setConfig(appConfig).
                                    setRepository(productRepository);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !tags.contains(tagValue)) {
                            builder.addTag(tagValue);
                            tags.add(tagValue);
                        }

                        UUID ingredientId = (UUID) rs.getObject("ingredientId");
                        if(!rs.wasNull()) {
                            if(!ingredients.contains(ingredientId)) {
                                builder.addIngredient(
                                        new DishIngredient.Builder().
                                                setId(ingredientId).
                                                setName(rs.getString("ingredientName")).
                                                setQuantity(rs.getBigDecimal("ingredientQuantity")).
                                                setFilter(filterJsonMapper.toFilter(rs.getString("ingredientFilter"))).
                                                setConfig(appConfig)
                                );
                                ingredients.add(ingredientId);
                            }
                        }
                    }

                    return builder == null ?
                            Optional.empty() :
                            Optional.ofNullable(builder.tryBuild());
                }
        );
    }

    @Override
    public Dish tryRemove(UUID userId, UUID dishId) {
        Dish dish = getById(userId, dishId).
                orElseThrow(
                        () -> new ValidateException("Unknown dish with id=" + dishId + " for userId=" + userId).
                                addReason(Rule.of("DishRepository.dishId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );

        statement.update(
                "DELETE FROM Dishes WHERE dishId = ? AND userId = ?;",
                (PreparedStatement ps) -> {
                    ps.setObject(1, dishId);
                    ps.setObject(2, userId);
                }
        );

        return dish;
    }

    @Override
    public Dish tryGetById(UUID userId, UUID dishId) {
        return getById(userId, dishId).
                orElseThrow(
                        () -> new ValidateException("Unknown dish with id=" + dishId + " for userId=" + userId).
                                addReason(Rule.of("DishRepository.dishId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public Dish tryGetByName(UUID userId, String name) {
        return getByName(userId, name).
                orElseThrow(
                        () -> new ValidateException("Unknown dish with name=" + name + " for userId=" + userId).
                                addReason(Rule.of("DishRepository.name", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public Page<Dish> getDishes(Criteria criteria) {
        int dishesNumber = getDishesNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(dishesNumber, 30);

        if(metadata.isEmpty()) return Page.empty();

        String query =
                select(field("D.*"),
                        field("DishIngredients.ingredientId as ingredientId"),
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
                                where(dishFilterMapper.toCondition(criteria.getFilter())).
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

        List<Dish> dishes = statement.query(query, this::mapToDishes);

        return metadata.createPage(dishes);
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        int tagsNumber = getTagsNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(tagsNumber, 1000);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("DishTags.tagValue")).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
                where(dishFilterMapper.toCondition(criteria.getFilter())).
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
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(unitsNumber, 1000);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("Dishes.unit")).
                from("Dishes").
                where(dishFilterMapper.toCondition(criteria.getFilter())).
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
    public Page<String> getNames(Criteria criteria) {
        int namesNumber = getNamesNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(namesNumber, 1000);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("Dishes.name")).
                from("Dishes").
                where(dishFilterMapper.toCondition(criteria.getFilter())).
                orderBy(field("Dishes.name").asc()).
                limit(inline(metadata.getActualSize())).
                offset(inline(metadata.getOffset())).
                getSQL();

        List<String> names = statement.query(
                query,
                (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();

                    while(rs.next()) {
                        result.add(rs.getString(1));
                    }

                    return result;
                }
        );

        return metadata.createPage(names);
    }

    @Override
    public int getDishesNumber(Criteria criteria) {
        Validator.check(
                "DishRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = selectCount().
                from("Dishes").
                where(dishFilterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        Validator.check(
                "DishRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("DishTags.tagValue"))).
                from("DishTags").
                join("Dishes").
                on(field("Dishes.dishId").eq(field("DishTags.dishId"))).
                where(dishFilterMapper.toCondition(criteria.tryGetFilter())).
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
        Validator.check(
                "DishRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Dishes.unit"))).
                from("Dishes").
                where(dishFilterMapper.toCondition(criteria.tryGetFilter())).
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
    public int getNamesNumber(Criteria criteria) {
        Validator.check(
                "DishRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Dishes.name"))).
                from("Dishes").
                where(dishFilterMapper.toCondition(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }


    List<Dish> mapToDishes(ResultSet rs) throws SQLException {
        List<Dish> result = new ArrayList<>();

        Dish.Builder builder = null;
        HashSet<String> tags = new HashSet<>();
        HashSet<UUID> ingredients = new HashSet<>();
        UUID lastDishId = null;
        while(rs.next()) {
            UUID dishId = (UUID)rs.getObject("dishId");
            if(!dishId.equals(lastDishId)) {
                if (builder != null) result.add(builder.tryBuild());
                builder = new Dish.Builder().
                        setId(dishId).
                        setUser(
                                new User.LoadBuilder().
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
                        setImageUrl(rs.getString("imagePath")).
                        setConfig(appConfig).
                        setRepository(productRepository);

                lastDishId = dishId;
                tags.clear();
                ingredients.clear();
            }

            String tagValue = rs.getString("tagValue");
            if(!rs.wasNull() && !tags.contains(tagValue)) {
                builder.addTag(tagValue);
                tags.add(tagValue);
            }

            UUID ingredientId = (UUID) rs.getObject("ingredientId");
            if(!rs.wasNull()) {
                if(!ingredients.contains(ingredientId)) {
                    builder.addIngredient(
                            new DishIngredient.Builder().
                                    setId(ingredientId).
                                    setName(rs.getString("ingredientName")).
                                    setQuantity(rs.getBigDecimal("ingredientQuantity")).
                                    setFilter(filterJsonMapper.toFilter(rs.getString("ingredientFilter"))).
                                    setConfig(appConfig)
                    );
                    ingredients.add(ingredientId);
                }
            }
        }

        if(builder != null) result.add(builder.tryBuild());

        return result;
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
                        INSERT INTO DishIngredients(ingredientId, dishId, name, quantity, filter, filterQuery, index)
                          VALUES(?,?,?,?,jsonb(?),?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = dish.getIngredients().get(i);
                        String filterQuery = select(field("*")).
                                from(table("Products")).
                                where(filterMapper.toCondition(ingredient.getFilter())).
                                getSQL();

                        ps.setObject(1, ingredient.getId());
                        ps.setObject(2, dish.getId());
                        ps.setString(3, ingredient.getName());
                        ps.setBigDecimal(4, ingredient.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setString(5, filterJsonMapper.toJson(ingredient.getFilter()));
                        ps.setString(6, filterQuery);
                        ps.setInt(7, i);
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
                        INSERT INTO DishIngredients(ingredientId, dishId, name, quantity, filter, filterQuery, index)
                          VALUES(?,?,?,?,jsonb(?),?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        DishIngredient ingredient = newVersion.getIngredients().get(i);
                        String filterQuery = select(field("*")).
                                from(table("Products")).
                                where(filterMapper.toCondition(ingredient.getFilter())).
                                getSQL();

                        ps.setObject(1, ingredient.getId());
                        ps.setObject(2, newVersion.getId());
                        ps.setString(3, ingredient.getName());
                        ps.setBigDecimal(4, ingredient.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setString(5, filterJsonMapper.toJson(ingredient.getFilter()));
                        ps.setString(6, filterQuery);
                        ps.setInt(7, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getIngredients().size();
                    }

                }
        );
    }

    private boolean doesDishExist(UUID dishId) {
        return statement.query(
                "select count(*) > 0 as doesDishExist from Dishes where dishId = ?;",
                ps -> ps.setObject(1, dishId),
                rs -> {
                    boolean result = false;
                    if(rs.next()) result = rs.getBoolean("doesDishExist");
                    return result;
                }
        );
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

package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.bakuard.nutritionManager.model.filters.Filter.Type.USER;
import static com.bakuard.nutritionManager.validation.Rule.*;
import static org.jooq.impl.DSL.*;

public class MenuRepositoryPostgres implements MenuRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;
    private DishRepositoryPostgres dishRepository;
    private ProductRepository productRepository;

    public MenuRepositoryPostgres(DataSource dataSource,
                                  AppConfigData appConfig,
                                  DishRepositoryPostgres dishRepository,
                                  ProductRepository productRepository) {
        statement = new JdbcTemplate(dataSource);
        this.dishRepository = dishRepository;
        this.productRepository = productRepository;
        this.appConfig = appConfig;
    }

    @Override
    public boolean save(Menu menu) {
        Validator.check("MenuRepository.menu", notNull(menu));

        Menu oldMenu = getById(menu.getUser().getId(), menu.getId()).orElse(null);

        boolean newData = false;
        try {
            if(oldMenu == null) {
                addNewMenu(menu);
                newData = true;
            } else if(!menu.equalsFullState(oldMenu)) {
                updateMenu(menu);
                newData = true;
            }
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save menu", e).
                    addReason(Rule.of("MenuRepository.menu", failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB)));
        }

        return newData;
    }

    @Override
    public Menu tryRemove(UUID userId, UUID menuId) {
        Menu menu = getById(userId, menuId).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with id=" + menuId + " for userId=" + userId).
                                addReason(Rule.of("MenuRepository.menuId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );

        statement.update(
                "DELETE FROM Menus WHERE menuId = ? AND userId = ?;",
                (PreparedStatement ps) -> {
                    ps.setObject(1, menuId);
                    ps.setObject(2, userId);
                }
        );

        return menu;
    }

    @Override
    public Optional<Menu> getById(UUID userId, UUID menuId) {
        Validator.check(
                "MenuRepository.userId", notNull(userId),
                "MenuRepository.menuId", notNull(menuId)
        );

        Menu.Builder result = statement.query(
                connection -> connection.prepareStatement("""
                        SELECT Menus.*,
                               MenuItems.*,
                               MenuTags.*,
                               Users.userId,
                               Users.name as userName,
                               Users.passwordHash as userPasswordHash,
                               Users.email as userEmail,
                               Users.salt as userSalt
                            FROM Menus
                            LEFT JOIN MenuItems
                                ON Menus.menuId = MenuItems.menuId
                            LEFT JOIN MenuTags
                                ON Menus.menuId = MenuTags.menuId
                            LEFT JOIN Users
                                ON Menus.userId = Users.userId
                            WHERE Menus.menuId = ? AND Menus.userId = ?
                            ORDER BY MenuItems.index, MenuTags.index;
                        """),
                ps -> {
                    ps.setObject(1, menuId);
                    ps.setObject(2, userId);
                },
                rs -> {
                    Menu.Builder builder = null;
                    HashSet<String> tags = new HashSet<>();
                    HashSet<UUID> items = new HashSet<>();

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Menu.Builder().
                                    setId(menuId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userID")).
                                                    setName(rs.getString("userName")).
                                                    setPasswordHash(rs.getString("userPasswordHash")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(rs.getString("name")).
                                    setDescription(rs.getString("description")).
                                    setImageUrl(rs.getString("imagePath")).
                                    setConfig(appConfig);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !tags.contains(tagValue)) {
                            builder.addTag(tagValue);
                            tags.add(tagValue);
                        }

                        UUID itemId = (UUID) rs.getObject("itemId");
                        if(!rs.wasNull() && !items.contains(itemId)) {
                            builder.addItem(
                                    new MenuItem.LoadBuilder().
                                            setId(itemId).
                                            setConfig(appConfig).
                                            setQuantity(rs.getBigDecimal("quantity"))
                            );
                            items.add(itemId);
                        }
                    }

                    return builder;
                }
        );

        if(result != null) {
            List<Dish> dishes = statement.query(
                    connection -> connection.prepareStatement("""
                            SELECT MenuItems.menuId,
                                   MenuItems.index,
                                   Dishes.*,
                                   DishTags.*,
                                   Users.userId,
                                   Users.name as userName,
                                   Users.passwordHash as userPasswordHash,
                                   Users.email as userEmail,
                                   Users.salt as userSalt,
                                   DishIngredients.ingredientId as ingredientId,
                                   DishIngredients.name as ingredientName,
                                   DishIngredients.quantity as ingredientQuantity,
                                   DishIngredients.filter as ingredientFilter
                                FROM MenuItems
                                INNER JOIN Menus
                                    ON MenuItems.menuId = Menus.menuId
                                INNER JOIN Users
                                    ON Menus.userId = Users.userId
                                INNER JOIN Dishes
                                    ON MenuItems.dishId = Dishes.dishId
                                LEFT JOIN DishTags
                                    ON MenuItems.dishId = DishTags.dishId
                                LEFT JOIN DishIngredients
                                    ON MenuItems.dishId = DishIngredients.dishId
                                WHERE MenuItems.menuId = ?
                                ORDER BY MenuItems.index, DishTags.index, DishIngredients.index;
                            """),
                    ps -> ps.setObject(1, menuId),
                    dishRepository::mapToDishes
            );

            for(int i = 0; i < result.getItems().size(); i++)
                ((MenuItem.LoadBuilder) result.getItems().get(i)).setDish(dishes.get(i));
        }

        return Optional.ofNullable(result == null ? null : result.tryBuild());
    }

    @Override
    public Optional<Menu> getByName(UUID userId, String name) {
        Validator.check(
                "MenuRepository.userId", notNull(userId),
                "MenuRepository.name", notNull(name)
        );

        Menu.Builder result = statement.query(
                connection -> connection.prepareStatement("""
                        SELECT Menus.*,
                               MenuItems.*,
                               MenuTags.*,
                               Users.userId,
                               Users.name as userName,
                               Users.passwordHash as userPasswordHash,
                               Users.email as userEmail,
                               Users.salt as userSalt
                            FROM Menus
                            LEFT JOIN MenuItems
                                ON Menus.menuId = MenuItems.menuId
                            LEFT JOIN MenuTags
                                ON Menus.menuId = MenuTags.menuId
                            LEFT JOIN Users
                                ON Menus.userId = Users.userId
                            WHERE Menus.name = ? AND Menus.userId = ?
                            ORDER BY MenuItems.index, MenuTags.index;
                        """),
                ps -> {
                    ps.setObject(1, name);
                    ps.setObject(2, userId);
                },
                rs -> {
                    Menu.Builder builder = null;
                    HashSet<String> tags = new HashSet<>();
                    HashSet<UUID> items = new HashSet<>();

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Menu.Builder().
                                    setId((UUID) rs.getObject("menuId")).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userID")).
                                                    setName(rs.getString("userName")).
                                                    setPasswordHash(rs.getString("userPasswordHash")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(rs.getString("name")).
                                    setDescription(rs.getString("description")).
                                    setImageUrl(rs.getString("imagePath")).
                                    setConfig(appConfig);
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !tags.contains(tagValue)) {
                            builder.addTag(tagValue);
                            tags.add(tagValue);
                        }

                        UUID itemId = (UUID) rs.getObject("itemId");
                        if(!rs.wasNull() && !items.contains(itemId)) {
                            builder.addItem(
                                    new MenuItem.LoadBuilder().
                                            setId(itemId).
                                            setConfig(appConfig).
                                            setQuantity(rs.getBigDecimal("quantity"))
                            );
                            items.add(itemId);
                        }
                    }

                    return builder;
                }
        );

        if(result != null) {
            List<Dish> dishes = statement.query(
                    connection -> connection.prepareStatement("""
                            SELECT MenuItems.index,
                                   Dishes.*,
                                   DishTags.*,
                                   Users.userId,
                                   Users.name as userName,
                                   Users.passwordHash as userPasswordHash,
                                   Users.email as userEmail,
                                   Users.salt as userSalt,
                                   DishIngredients.ingredientId as ingredientId,
                                   DishIngredients.name as ingredientName,
                                   DishIngredients.quantity as ingredientQuantity,
                                   DishIngredients.filter as ingredientFilter
                                FROM MenuItems
                                INNER JOIN Menus
                                    ON MenuItems.menuId = Menus.menuId
                                INNER JOIN Users
                                    ON Menus.userId = Users.userId
                                INNER JOIN Dishes
                                    ON MenuItems.dishId = Dishes.dishId
                                LEFT JOIN DishTags
                                    ON MenuItems.dishId = DishTags.dishId
                                LEFT JOIN DishIngredients
                                    ON MenuItems.dishId = DishIngredients.dishId
                                WHERE Menus.name = ? AND Menus.userId = ?
                                ORDER BY MenuItems.index, DishTags.index, DishIngredients.index;
                            """),
                    ps -> {
                        ps.setString(1, name);
                        ps.setObject(2, userId);
                    },
                    dishRepository::mapToDishes
            );

            for(int i = 0; i < dishes.size(); i++)
                ((MenuItem.LoadBuilder) result.getItems().get(i)).setDish(dishes.get(i));
        }

        return Optional.ofNullable(result == null ? null : result.tryBuild());
    }

    @Override
    public Menu tryGetById(UUID userId, UUID menuId) {
        return getById(userId, menuId).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with id=" + menuId + " for userId=" + userId).
                                addReason(Rule.of("MenuRepository.menuId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public Menu tryGetByName(UUID userId, String name) {
        return getByName(userId, name).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with name=" + name + " for userId=" + userId).
                                addReason(Rule.of("MenuRepository.name", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public Page<Menu> getMenus(Criteria criteria) {
        int menusNumber = getMenusNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(menusNumber, 30);

        if(metadata.isEmpty()) return Page.empty();

        String query =
                select(field("M.*"),
                        field("MenuItems.itemId as itemId"),
                        field("MenuItems.quantity as itemQuantity"),
                        field("MenuTags.tagValue as tagValue"),
                        field("Users.userId as userId"),
                        field("Users.name as userName"),
                        field("Users.email as userEmail"),
                        field("Users.passwordHash as userPasswordHash"),
                        field("Users.salt as userSalt")).
                        from(
                                select(field("*")).
                                        from("Menus").
                                        where(switchFilter(criteria.getFilter())).
                                        orderBy(getOrderFields(criteria.getSort(), "Menus")).
                                        limit(inline(metadata.getActualSize())).
                                        offset(inline(metadata.getOffset())).
                                        asTable("{M}")
                        ).
                        innerJoin("Users").
                            on(field("M.userId").eq(field("Users.userId"))).
                        leftJoin("MenuTags").
                            on(field("M.menuId").eq(field("MenuTags.menuId"))).
                        leftJoin("MenuItems").
                            on(field("M.menuId").eq(field("MenuItems.menuId"))).
                        orderBy(getOrderFields(criteria.getSort(), "M")).
                        getSQL().
                        replace("\"{M}\"", "as M");

        List<Menu.Builder> result = statement.query(
                query,
                rs -> {
                    ArrayList<Menu.Builder> builders = new ArrayList<>();

                    Menu.Builder builder = null;
                    HashSet<String> tags = new HashSet<>();
                    HashSet<UUID> items = new HashSet<>();
                    UUID lastMenuId = null;
                    while(rs.next()) {
                        UUID menuId = (UUID)rs.getObject("menuId");
                        if(!menuId.equals(lastMenuId)) {
                            if(builder != null) builders.add(builder);
                            builder = new Menu.Builder().
                                    setId(menuId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userID")).
                                                    setName(rs.getString("userName")).
                                                    setPasswordHash(rs.getString("userPasswordHash")).
                                                    setEmail(rs.getString("userEmail")).
                                                    setSalt(rs.getString("userSalt")).
                                                    tryBuild()
                                    ).
                                    setName(rs.getString("name")).
                                    setDescription(rs.getString("description")).
                                    setImageUrl(rs.getString("imagePath")).
                                    setConfig(appConfig);

                            lastMenuId = menuId;
                            tags.clear();
                            items.clear();
                        }

                        String tagValue = rs.getString("tagValue");
                        if(!rs.wasNull() && !tags.contains(tagValue)) {
                            builder.addTag(tagValue);
                            tags.add(tagValue);
                        }

                        UUID itemId = (UUID) rs.getObject("itemId");
                        if(!rs.wasNull() && !items.contains(itemId)) {
                            builder.addItem(
                                    new MenuItem.LoadBuilder().
                                            setId(itemId).
                                            setConfig(appConfig).
                                            setQuantity(rs.getBigDecimal("itemQuantity"))
                            );
                            items.add(itemId);
                        }
                    }

                    if(builder != null) builders.add(builder);

                    return builders;
                }
        );

        for(Menu.Builder builder : result) {
            if(!builder.getItems().isEmpty()) {
                List<Dish> dishes = statement.query(
                        connection -> connection.prepareStatement("""
                                SELECT MenuItems.index,
                                       MenuItems.menuId,
                                       Dishes.*,
                                       DishTags.*,
                                       Users.userId,
                                       Users.name as userName,
                                       Users.passwordHash as userPasswordHash,
                                       Users.email as userEmail,
                                       Users.salt as userSalt,
                                       DishIngredients.ingredientId as ingredientId,
                                       DishIngredients.name as ingredientName,
                                       DishIngredients.quantity as ingredientQuantity,
                                       DishIngredients.filter as ingredientFilter
                                    FROM MenuItems
                                    INNER JOIN Menus
                                        ON MenuItems.menuId = Menus.menuId
                                    INNER JOIN Users
                                        ON Menus.userId = Users.userId
                                    INNER JOIN Dishes
                                        ON MenuItems.dishId = Dishes.dishId
                                    LEFT JOIN DishTags
                                        ON MenuItems.dishId = DishTags.dishId
                                    LEFT JOIN DishIngredients
                                        ON MenuItems.dishId = DishIngredients.dishId
                                    WHERE MenuItems.menuId = ?
                                    ORDER BY MenuItems.index, DishTags.index, DishIngredients.index;
                                """),
                        ps -> ps.setObject(1, builder.getId()),
                        dishRepository::mapToDishes
                );

                for(int i = 0; i < dishes.size(); i++)
                    ((MenuItem.LoadBuilder) builder.getItems().get(i)).setDish(dishes.get(i));
            }
        }

        return metadata.createPage(
                result.stream().map(Menu.Builder::tryBuild).toList()
        );
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        int tagsNumber = getTagsNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(tagsNumber, 1000);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("MenuTags.tagValue")).
                from("MenuTags").
                join("Menus").
                    on(field("MenuTags.menuId").eq(field("Menus.menuId"))).
                where(switchFilter(criteria.getFilter())).
                orderBy(field("MenuTags.tagValue").asc()).
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
    public Page<String> getNames(Criteria criteria) {
        int namesNumber = getNamesNumber(criteria);
        Page.Metadata metadata = criteria.getPageable(PageableByNumber.class).
                createPageMetadata(namesNumber, 1000);

        if(metadata.isEmpty()) return metadata.createPage(List.of());

        String query = selectDistinct(field("Menus.name")).
                from("Menus").
                where(switchFilter(criteria.getFilter())).
                orderBy(field("Menus.name").asc()).
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
    public int getMenusNumber(Criteria criteria) {
        Validator.check(
                "MenuRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = selectCount().
                from("Menus").
                where(switchFilter(criteria.tryGetFilter())).
                getSQL();

        return statement.queryForObject(query, Integer.class);
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        Validator.check(
                "MenuRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("MenuTags.tagValue"))).
                from("MenuTags").
                innerJoin("Menus").
                    on(field("Menus.menuId").eq(field("MenuTags.menuId"))).
                where(switchFilter(criteria.tryGetFilter())).
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
                "MenuRepository.criteria", notNull(criteria).
                        and(() -> isTrue(criteria.tryGetFilter().containsAtLeast(USER)))
        );

        String query = select(countDistinct(field("Menus.name"))).
                from("Menus").
                where(switchFilter(criteria.tryGetFilter())).
                getSQL();

        return statement.query(
                query,
                (ResultSet rs) -> {
                    rs.next();
                    return rs.getInt(1);
                }
        );
    }


    private void addNewMenu(Menu menu) {
        statement.update(
                """
                        INSERT INTO Menus (
                            menuId,
                            userId,
                            name,
                            description,
                            imagePath
                        ) VALUES (?,?,?,?,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, menu.getId());
                    ps.setObject(2, menu.getUser().getId());
                    ps.setString(3, menu.getName());
                    ps.setString(4, menu.getDescription());
                    ps.setString(5, menu.getImageUrl() == null ? null : menu.getImageUrl().toString());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO MenuTags(menuId, tagValue, index)
                          VALUES(?,?,?);
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tag tag = menu.getTags().get(i);
                        ps.setObject(1, menu.getId());
                        ps.setString(2, tag.getValue());
                        ps.setInt(3, i);
                    }

                    @Override
                    public int getBatchSize() {
                        return menu.getTags().size();
                    }

                }
        );

        int[] rows = statement.batchUpdate(
                """
                        INSERT INTO MenuItems(itemId, menuId, dishId, quantity, index)
                            (
                                SELECT ?, ?, Dishes.dishId, ?, ?
                                    FROM Dishes
                                    WHERE Dishes.name = ? AND Dishes.userId = ?
                            );
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MenuItem item = menu.getItems().get(i);

                        ps.setObject(1, item.getId());
                        ps.setObject(2, menu.getId());
                        ps.setBigDecimal(3, item.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setInt(4, i);
                        ps.setString(5, item.getDishName());
                        ps.setObject(6, menu.getUser().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return menu.getMenuItemNumbers();
                    }

                }
        );

        for(int i = 0; i < rows.length; i++) {
            if(rows[i] == 0)
                throw new ValidateException("User haven't dish with name=" + menu.getItems().get(i).getDishName()).
                        addReason(Rule.of("MenuRepository.itemExists", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)));
        }
    }

    private void updateMenu(Menu newVersion) {
        statement.update(
                """
                        UPDATE Menus SET
                                name=?,
                                description=?,
                                imagePath=?
                            WHERE menuId = ? AND userId = ?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, newVersion.getName());
                    ps.setString(2, newVersion.getDescription());
                    ps.setString(3, newVersion.getImageUrl() == null ? null : newVersion.getImageUrl().toString());
                    ps.setObject(4, newVersion.getId());
                    ps.setObject(5, newVersion.getUser().getId());
                }
        );

        statement.update(
                """
                    DELETE FROM MenuTags
                        WHERE MenuTags.menuId=?;
                    """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, newVersion.getId());
                }
        );

        statement.update(
                """
                    DELETE FROM MenuItems
                        WHERE MenuItems.menuId=?;
                    """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, newVersion.getId());
                }
        );

        statement.batchUpdate(
                """
                        INSERT INTO MenuTags(menuId, tagValue, index)
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

        int[] rows = statement.batchUpdate(
                """
                        INSERT INTO MenuItems(itemId, menuId, dishId, quantity, index)
                            (
                                SELECT ?, ?, Dishes.dishId, ?, ?
                                    FROM Dishes
                                    WHERE Dishes.name = ? AND Dishes.userId = ?
                            );
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MenuItem item = newVersion.getItems().get(i);

                        ps.setObject(1, item.getId());
                        ps.setObject(2, newVersion.getId());
                        ps.setBigDecimal(3, item.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setInt(4, i);
                        ps.setString(5, item.getDishName());
                        ps.setObject(6, newVersion.getUser().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getMenuItemNumbers();
                    }

                }
        );

        for(int i = 0; i < rows.length; i++) {
            if(rows[i] == 0)
                throw new ValidateException("User haven't dish with name=" + newVersion.getItems().get(i).getDishName()).
                        addReason(Rule.of("MenuRepository.itemExists", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)));
        }
    }


    private Condition switchFilter(Filter filter) {
        Condition result = null;

        switch(filter.getType()) {
            case USER -> result = userFilter((UserFilter) filter);
            case DISHES -> result = dishesFilter((AnyFilter) filter);
            case MIN_TAGS -> result = minTagsFilter((MinTagsFilter) filter);
            case AND -> result = andFilter((AndFilter) filter);
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }

        return result;
    }

    private Condition userFilter(UserFilter filter) {
        return field("userId").eq(inline(filter.getUserId()));
    }

    private Condition dishesFilter(AnyFilter filter) {
        return field("menuId").in(
                select(field("MenuItems.menuId")).
                        from(table("MenuItems")).
                        innerJoin(table("Dishes")).
                            on(field("MenuItems.dishId").eq(field("Dishes.dishId"))).
                        where(field("Dishes.name").in(
                                filter.getValues().stream().map(DSL::inline).toList()
                        ))
        );
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("menuId").in(
                select(field("MenuTags.menuId")).
                        from("MenuTags").
                        where(field("MenuTags.tagValue").in(
                                filter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("MenuTags.menuId")).
                        having(count(field("MenuTags.menuId")).eq(inline(filter.getTags().size())))
        );
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = switchFilter(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(switchFilter(filter.getOperands().get(i)));
        }
        return condition;
    }


    private List<SortField<?>> getOrderFields(Sort menuSort,
                                              String tableName) {
        ArrayList<SortField<?>> fields = new ArrayList<>();

        for(int i = 0; i < menuSort.getParametersNumber(); i++) {
            switch(menuSort.getParameter(i)) {
                case "name" -> {
                    if(menuSort.isAscending(i))
                        fields.add(field(tableName + ".name").asc());
                    else
                        fields.add(field(tableName + ".name").desc());
                }
            }
        }
        fields.add(field(tableName + ".menuId").asc());
        if("M".equals(tableName)) {
            fields.add(field("MenuItems.index").asc());
            fields.add(field("MenuTags.index").asc());
        }
        return fields;
    }

}

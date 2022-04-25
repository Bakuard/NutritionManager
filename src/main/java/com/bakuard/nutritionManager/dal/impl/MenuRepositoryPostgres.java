package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        Validator.check(
                Rule.of("MenuRepository.menu").notNull(menu)
        );

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
                    addReason(Rule.of("MenuRepository.menu").failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB));
        }

        return newData;
    }

    @Override
    public Menu tryRemove(UUID userId, UUID menuId) {
        Menu menu = getById(userId, menuId).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with id=" + menuId + " for userId=" + userId).
                                addReason(Rule.of("MenuRepository.menuId").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB))
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
                Rule.of("MenuRepository.userId").notNull(userId),
                Rule.of("MenuRepository.menuId").notNull(menuId)
        );

        Menu.Builder result = statement.query(
                connection -> connection.prepareStatement("""
                        SELECT Menus.*,
                               MenuItems.*,
                               Dishes.name as dishName,
                               MenuTags.*,
                               Users.userId,
                               Users.name as userName,
                               Users.passwordHash as userPassHash,
                               Users.email as userEmail,
                               Users.salt as userSalt
                            FROM Menus
                            LEFT JOIN MenuItems
                                ON Menus.menuId = MenuItems.menuId
                            LEFT JOIN Dishes
                                ON MenuItems.dishId = Dishes.dishId
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

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Menu.Builder().
                                    setId(menuId).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userID")).
                                                    setName(rs.getString("userName")).
                                                    setPasswordHash(rs.getString("userPassHash")).
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
                        if(!rs.wasNull() && !builder.containsTag(tagValue)) builder.addTag(tagValue);

                        String dishName = rs.getString("dishName");
                        if(!rs.wasNull() && !builder.containsItem(dishName)) {
                            builder.addItem(
                                    new MenuItem.Builder().
                                            setConfig(appConfig).
                                            setDishName(dishName).
                                            setQuantity(rs.getBigDecimal("quantity"))
                            );
                        }
                    }

                    return builder;
                }
        );

        if(result != null) {
            List<Dish> dishes = statement.query(
                    connection -> connection.prepareStatement("""
                            SELECT MenuItems.menuId,
                                   Dishes.*,
                                   DishTags.*,
                                   Users.userId,
                                   Users.name as userName,
                                   Users.passwordHash as userPasswordHash,
                                   Users.email as userEmail,
                                   Users.salt as userSalt,
                                   DishIngredients.name as ingredientName,
                                   DishIngredients.quantity as ingredientQuantity,
                                   DishIngredients.filter as ingredientFilter
                                FROM MenuItems
                                INNER JOIN Menus
                                    ON MenuItems.menuId = Menus.menuId
                                LEFT JOIN Users
                                    ON Menus.userId = Users.userId
                                LEFT JOIN Dishes
                                    ON MenuItems.dishId = Dishes.dishId
                                LEFT JOIN DishTags
                                    ON MenuItems.dishId = DishTags.dishId
                                LEFT JOIN DishIngredients
                                    ON MenuItems.dishId = DishIngredients.dishId
                                WHERE MenuItems.menuId = ?
                                ORDER BY Dishes.dishId, DishTags.index, DishIngredients.index;
                            """),
                    ps -> ps.setObject(1, menuId),
                    dishRepository::map
            );

            dishes.forEach(dish -> result.getItem(dish.getName()).setDish(() -> dish));
        }

        return Optional.ofNullable(result == null ? null : result.tryBuild());
    }

    @Override
    public Optional<Menu> getByName(UUID userId, String name) {
        Validator.check(
                Rule.of("MenuRepository.userId").notNull(userId),
                Rule.of("MenuRepository.name").notNull(name)
        );

        Menu.Builder result = statement.query(
                connection -> connection.prepareStatement("""
                        SELECT Menus.*,
                               MenuItems.*,
                               Dishes.name as dishName,
                               MenuTags.*,
                               Users.userId,
                               Users.name as userName,
                               Users.passwordHash as userPassHash,
                               Users.email as userEmail,
                               Users.salt as userSalt
                            FROM Menus
                            LEFT JOIN MenuItems
                                ON Menus.menuId = MenuItems.menuId
                            LEFT JOIN Dishes
                                ON MenuItems.dishId = Dishes.dishId
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

                    while(rs.next()) {
                        if(builder == null) {
                            builder = new Menu.Builder().
                                    setId((UUID) rs.getObject("menuId")).
                                    setUser(
                                            new User.LoadBuilder().
                                                    setId((UUID) rs.getObject("userID")).
                                                    setName(rs.getString("userName")).
                                                    setPasswordHash(rs.getString("userPassHash")).
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
                        if(!rs.wasNull() && !builder.containsTag(tagValue)) builder.addTag(tagValue);

                        String dishName = rs.getString("dishName");
                        if(!rs.wasNull() && !builder.containsItem(dishName)) {
                            builder.addItem(
                                    new MenuItem.Builder().
                                            setConfig(appConfig).
                                            setDishName(dishName).
                                            setQuantity(rs.getBigDecimal("quantity"))
                            );
                        }
                    }

                    return builder;
                }
        );

        if(result != null) {
            List<Dish> dishes = statement.query(
                    connection -> connection.prepareStatement("""
                            SELECT Menus.name,
                                   Dishes.*,
                                   DishTags.*,
                                   Users.userId,
                                   Users.name as userName,
                                   Users.passwordHash as userPasswordHash,
                                   Users.email as userEmail,
                                   Users.salt as userSalt,
                                   DishIngredients.name as ingredientName,
                                   DishIngredients.quantity as ingredientQuantity,
                                   DishIngredients.filter as ingredientFilter
                                FROM MenuItems
                                INNER JOIN Menus
                                    ON MenuItems.menuId = Menus.menuId
                                LEFT JOIN Users
                                    ON Menus.userId = Users.userId
                                LEFT JOIN Dishes
                                    ON MenuItems.dishId = Dishes.dishId
                                LEFT JOIN DishTags
                                    ON MenuItems.dishId = DishTags.dishId
                                LEFT JOIN DishIngredients
                                    ON MenuItems.dishId = DishIngredients.dishId
                                WHERE Menus.name = ?
                                ORDER BY Dishes.dishId, DishTags.index, DishIngredients.index;
                            """),
                    ps -> ps.setString(1, name),
                    dishRepository::map
            );

            dishes.forEach(dish -> result.getItem(dish.getName()).setDish(() -> dish));
        }

        return Optional.ofNullable(result == null ? null : result.tryBuild());
    }

    @Override
    public Menu tryGetById(UUID userId, UUID menuId) {
        return getById(userId, menuId).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with id=" + menuId + " for userId=" + userId)
                );
    }

    @Override
    public Menu tryGetByName(UUID userId, String name) {
        return getByName(userId, name).
                orElseThrow(
                        () -> new ValidateException("Unknown menu with name=" + name + " for userId=" + userId)
                );
    }

    @Override
    public Page<Menu> getMenus(Criteria criteria) {
        return null;
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        return null;
    }

    @Override
    public Page<String> getNames(Criteria criteria) {
        return null;
    }

    @Override
    public int getMenusNumber(Criteria criteria) {
        return 0;
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        return 0;
    }

    @Override
    public int getNamesNumber(Criteria criteria) {
        return 0;
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

        statement.batchUpdate(
                """
                        INSERT INTO MenuItems(menuId, dishId, quantity, index)
                            (
                                SELECT ?, Dishes.dishId, ?, ?
                                    FROM Dishes
                                    WHERE Dishes.name = ? AND Dishes.userId = ?
                            );
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MenuItem item = menu.getItems().get(i);

                        ps.setObject(1, menu.getId());
                        ps.setBigDecimal(2, item.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setInt(3, i);
                        ps.setString(4, item.getDishName());
                        ps.setObject(5, menu.getUser().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return menu.getMenuItemNumbers();
                    }

                }
        );
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

        statement.batchUpdate(
                """
                        INSERT INTO MenuItems(menuId, dishId, quantity, index)
                            (
                                SELECT ?, Dishes.dishId, ?, ?
                                    FROM Dishes
                                    WHERE Dishes.name = ? AND Dishes.userId = ?
                            );
                        """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MenuItem item = newVersion.getItems().get(i);

                        ps.setObject(1, newVersion.getId());
                        ps.setBigDecimal(2, item.getNecessaryQuantity(BigDecimal.ONE));
                        ps.setInt(3, i);
                        ps.setString(4, item.getDishName());
                        ps.setObject(5, newVersion.getUser().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return newVersion.getMenuItemNumbers();
                    }

                }
        );
    }

}

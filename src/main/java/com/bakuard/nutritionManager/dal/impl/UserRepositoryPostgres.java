package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Validator;
import com.bakuard.nutritionManager.validation.Constraint;

import com.bakuard.nutritionManager.validation.ValidateException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class UserRepositoryPostgres implements UserRepository {

    private JdbcTemplate statement;

    public UserRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean save(User user) {
        Validator.create().
                field("user").notNull(user).end().
                validate("Fail to save user");

        User oldUser = getByIdOrReturnNull(user.getId());

        boolean wasSaved = false;
        try {
            if(oldUser == null) {
                addNewUser(user);
                wasSaved = true;
            } else if(!user.equalsFullState(oldUser)) {
                updateUser(user);
                wasSaved = true;
            }
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save user").
                    addReason("user", Constraint.ENTITY_MUST_UNIQUE_IN_DB);
        }

        return wasSaved;
    }

    @Override
    public User getById(UUID userId) {
        Validator.create().
                field("userId").notNull(userId).end().
                validate("Fail to get user by id");

        User user = getByIdOrReturnNull(userId);

        if(user == null) {
            throw new ValidateException("Fail to get user by i").
                    addReason("userId", Constraint.ENTITY_MUST_EXISTS_IN_DB);
        }
        return user;
    }

    @Override
    public User getByName(String name) {
        Validator.create().
                field("name").notNull(name).end().
                validate("Fail to get user by name");

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.name = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, name),
                (ResultSet rs) -> {
                    if(rs.next()) {
                        return new User(
                                (UUID) rs.getObject("userId"),
                                name,
                                rs.getString("passwordHash"),
                                rs.getString("email"),
                                rs.getString("salt")
                        );
                    }
                    throw new ValidateException("Fail to get user by name=" + name).
                            addReason("name", Constraint.ENTITY_MUST_EXISTS_IN_DB);
                }
        );
    }

    @Override
    public User getByEmail(String email) {
        Validator.create().
                field("email").notNull(email).end().
                validate("Fail to get user by email");

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.email = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, email),
                (ResultSet rs) -> {
                    if(rs.next()) {
                        return new User(
                                (UUID) rs.getObject("userId"),
                                rs.getString("name"),
                                rs.getString("passwordHash"),
                                email,
                                rs.getString("salt")
                        );
                    }
                    throw new ValidateException("Fail to get user by email=" + email).
                            addReason("email", Constraint.ENTITY_MUST_EXISTS_IN_DB);
                }
        );
    }


    private User getByIdOrReturnNull(UUID userId) {
        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.userId = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, userId),
                (ResultSet rs) -> {
                    if(rs.next()) {
                        return new User(
                                userId,
                                rs.getString("name"),
                                rs.getString("passwordHash"),
                                rs.getString("email"),
                                rs.getString("salt")
                        );
                    }
                    return null;
                }
        );
    }

    private void addNewUser(User user) {
        statement.update(
                """
                        INSERT INTO Users(userId, name, passwordHash, email, salt)
                         VALUES (?,?,?,?,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, user.getId());
                    ps.setString(2, user.getName());
                    ps.setString(3, user.getPasswordHash());
                    ps.setString(4, user.getEmail());
                    ps.setString(5, user.getSalt());
                }
        );
    }

    private void updateUser(User user) {
        statement.update(
                """
                        UPDATE Users
                         SET name=?, passwordHash=?, email=?, salt=?
                         WHERE userId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getSalt());
                    ps.setObject(5, user.getId());
                }
        );
    }

}

package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.Validator;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;

import com.bakuard.nutritionManager.model.exceptions.ValidateException;
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
                notNull("user", user).
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
            throw new ValidateException(
                    "Fail to save user",
                    getClass(),
                    "save"
            ).addReason("user", ConstraintType.ALREADY_EXISTS_IN_DB);
        }

        return wasSaved;
    }

    @Override
    public User getById(UUID userId) {
        Validator validator = Validator.create().
                notNull("userId", userId).
                validate("Fail to get user by id");

        User user = getByIdOrReturnNull(userId);

        if(user == null) {
            throw new ValidateException(
                    "Fail to get user by i",
                    getClass(),
                    "getById"
            ).addReason("userId", ConstraintType.UNKNOWN_ENTITY);
        }
        return user;
    }

    @Override
    public User getByName(String name) {
        Validator.create().
                notNull("name", name).
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
                    throw new ValidateException(
                            "Fail to get user by name=" + name,
                            getClass(),
                            "getByName"
                    ).addReason("name", ConstraintType.UNKNOWN_ENTITY);
                }
        );
    }

    @Override
    public User getByEmail(String email) {
        Validator.create().
                notNull("email", email).
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
                    throw new ValidateException(
                            "Fail to get user by email=" + email,
                            getClass(),
                            "getByEmail"
                    ).addReason("email", ConstraintType.UNKNOWN_ENTITY);
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

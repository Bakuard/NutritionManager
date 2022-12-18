package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class UserRepositoryPostgres implements UserRepository {

    private JdbcTemplate statement;

    public UserRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(User user) {
        Validator.check("UserRepository.user", notNull(user));

        try {
            if(doesUserExist(user.getId())) updateUser(user);
            else addNewUser(user);
        } catch(DuplicateKeyException e) {
            throw new ValidateException("Fail to save user", e).
                    addReason(Rule.of("UserRepository.user", failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB)));
        }
    }

    @Override
    public Optional<User> getById(UUID userId) {
        Validator.check("UserRepository.userId", notNull(userId));

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.userId = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, userId),
                (ResultSet rs) -> {
                    User user = null;

                    if(rs.next()) {
                        user = new User.LoadBuilder().
                                setId(userId).
                                setName(rs.getString("name")).
                                setEmail(rs.getString("email")).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }

                    return Optional.ofNullable(user);
                }
        );
    }

    @Override
    public Optional<User> getByName(String name) {
        Validator.check("UserRepository.name", notNull(name));

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.name = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, name),
                (ResultSet rs) -> {
                    User user = null;

                    if(rs.next()) {
                        user = new User.LoadBuilder().
                                setId((UUID) rs.getObject("userId")).
                                setName(name).
                                setEmail(rs.getString("email")).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }

                    return Optional.ofNullable(user);
                }
        );
    }

    @Override
    public Optional<User> getByEmail(String email) {
        Validator.check("UserRepository.email", notNull(email));

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.email = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, email),
                (ResultSet rs) -> {
                    User user = null;

                    if(rs.next()) {
                        user = new User.LoadBuilder().
                                setId((UUID) rs.getObject("userId")).
                                setName(rs.getString("name")).
                                setEmail(email).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }

                    return Optional.ofNullable(user);
                }
        );
    }

    @Override
    public User tryGetById(UUID userId) {
        return getById(userId).
                orElseThrow(
                       () -> new ValidateException("Unknown user with id = " + userId).
                               addReason(Rule.of("UserRepository.userId", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public User tryGetByName(String name) {
        return getByName(name).
                orElseThrow(
                        () -> new ValidateException("Unknown user with name = " + name).
                                addReason(Rule.of("UserRepository.name", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
                );
    }

    @Override
    public User tryGetByEmail(String email) {
        return getByEmail(email).
                orElseThrow(
                        () -> new ValidateException("Unknown user with email = " + email).
                                addReason(Rule.of("UserRepository.email", failure(Constraint.ENTITY_MUST_EXISTS_IN_DB)))
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

    private boolean doesUserExist(UUID userId) {
        return statement.query(
                "select count(*) > 0 as doesUserExist from Users where userId = ?;",
                ps -> ps.setObject(1, userId),
                rs -> {
                    boolean result = false;
                    if(rs.next()) result = rs.getBoolean("doesUserExist");
                    return result;
                }
        );
    }

}

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

public class UserRepositoryPostgres implements UserRepository {

    private JdbcTemplate statement;

    public UserRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean save(User user) {
        Validator.check(
                Rule.of("UserRepositoryPostgres.user").notNull(user)
        );

        User oldUser = getById(user.getId()).orElse(null);

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
                    addReason(Rule.of("UserRepositoryPostgres.user").failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB));
        }

        return wasSaved;
    }

    @Override
    public Optional<User> getById(UUID userId) {
        Validator.check(
                Rule.of("UserRepositoryPostgres.userId").notNull(userId)
        );

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
        Validator.check(
                Rule.of("UserRepositoryPostgres.name").notNull(name)
        );

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
        Validator.check(
                Rule.of("UserRepositoryPostgres.email").notNull(email)
        );

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
                       () -> new ValidateException("Unknown user with id = " + userId)
                );
    }

    @Override
    public User tryGetByName(String name) {
        return getByName(name).
                orElseThrow(
                        () -> new ValidateException("Unknown user with name = " + name)
                );
    }

    @Override
    public User tryGetByEmail(String email) {
        return getByEmail(email).
                orElseThrow(
                        () -> new ValidateException("Unknown user with email = " + email)
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

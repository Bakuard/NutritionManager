package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.Rule;
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
        ValidateException.check(
                Rule.of("UserRepositoryPostgres.user").notNull(user)
        );

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
                    addReason(Rule.of("UserRepositoryPostgres.user").failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB));
        }

        return wasSaved;
    }

    @Override
    public User getById(UUID userId) {
        ValidateException.check(
                Rule.of("UserRepositoryPostgres.userId").notNull(userId)
        );

        User user = getByIdOrReturnNull(userId);

        if(user == null) {
            throw new ValidateException("Fail to get user by id").
                    addReason(Rule.of("UserRepositoryPostgres.userId").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
        }
        return user;
    }

    @Override
    public User getByName(String name) {
        ValidateException.check(
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
                    if(rs.next()) {
                        return new User.LoadBuilder().
                                setId((UUID) rs.getObject("userId")).
                                setName(name).
                                setEmail(rs.getString("email")).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }
                    throw new ValidateException("Fail to get user by name=" + name).
                            addReason(Rule.of("UserRepositoryPostgres.name").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
                }
        );
    }

    @Override
    public User getByEmail(String email) {
        ValidateException.check(
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
                    if(rs.next()) {
                        return new User.LoadBuilder().
                                setId((UUID) rs.getObject("userId")).
                                setName(rs.getString("name")).
                                setEmail(email).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
                    }
                    throw new ValidateException("Fail to get user by email=" + email).
                            addReason(Rule.of("UserRepositoryPostgres.email").failure(Constraint.ENTITY_MUST_EXISTS_IN_DB));
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
                        return new User.LoadBuilder().
                                setId(userId).
                                setName(rs.getString("name")).
                                setEmail(rs.getString("email")).
                                setPasswordHash(rs.getString("passwordHash")).
                                setSalt(rs.getString("salt")).
                                tryBuild();
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

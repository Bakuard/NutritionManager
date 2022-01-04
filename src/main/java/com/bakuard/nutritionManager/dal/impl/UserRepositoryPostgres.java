package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;
import com.bakuard.nutritionManager.model.exceptions.UnknownUserException;
import com.bakuard.nutritionManager.model.exceptions.UserAlreadyExistsException;

import com.google.common.hash.Hashing;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
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
        if(user == null) {
            throw new MissingValueException("user can't be null", getClass(), "user");
        }

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
            throw new UserAlreadyExistsException("Such user=" + user + " already exists in DB.");
        }

        return wasSaved;
    }

    @Override
    public User getById(UUID userId) {
        User user = getByIdOrReturnNull(userId);
        if(user == null) throw new UnknownUserException("Unknown user with id=" + userId);
        return user;
    }

    @Override
    public User getByName(String name) {
        if(name == null)
            throw new UnknownUserException("Unknown user with name=null");

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
                                rs.getString("emailHash"),
                                rs.getString("salt")
                        );
                    }
                    throw new UnknownUserException("Unknown user with name=" + name);
                }
        );
    }

    @Override
    public User getByEmail(String email) {
        if(email == null)
            throw new UnknownUserException("Unknown user with email=null");

        String emailHash = Hashing.sha256().hashBytes(email.getBytes(StandardCharsets.UTF_8)).toString();

        return statement.query(
                (Connection conn) -> conn.prepareStatement("""
                        SELECT Users.*
                            FROM Users
                            WHERE Users.emailHash = ?
                        """),
                (PreparedStatement ps) -> ps.setObject(1, emailHash),
                (ResultSet rs) -> {
                    if(rs.next()) {
                        return new User(
                                (UUID) rs.getObject("userId"),
                                rs.getString("name"),
                                rs.getString("passwordHash"),
                                emailHash,
                                rs.getString("salt")
                        );
                    }
                    throw new UnknownUserException("Unknown user with email=" + email);
                }
        );
    }


    private User getByIdOrReturnNull(UUID userId) {
        if(userId == null)
            throw new UnknownUserException("Unknown user with id=null");

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
                                rs.getString("emailHash"),
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
                        INSERT INTO Users(userId, name, passwordHash, emailHash, salt)
                         VALUES (?,?,?,?,?);
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, user.getId());
                    ps.setString(2, user.getName());
                    ps.setString(3, user.getPasswordHash());
                    ps.setString(4, user.getEmailHash());
                    ps.setString(5, user.getSalt());
                }
        );
    }

    private void updateUser(User user) {
        statement.update(
                """
                        UPDATE Users
                         SET name=?, passwordHash=?, emailHash=?, salt=?
                         WHERE userId=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getEmailHash());
                    ps.setString(4, user.getSalt());
                    ps.setObject(5, user.getId());
                }
        );
    }

}

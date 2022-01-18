package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.exceptions.*;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private String passwordHash;
    private String email;
    private final String salt;

    public User(User other) {
        id = other.id;
        name = other.name;
        passwordHash = other.passwordHash;
        email = other.email;
        salt = other.salt;
    }

    public User(UUID id, String name, String password, String email) {
        UserValidateException validateException = new UserValidateException("Fail to create user.");
        validateException.addReason(checkId(id));
        validateException.addReason(checkName(name));
        validateException.addReason(checkPassword(password));
        validateException.addReason(checkEmail(email));
        if(validateException.violatedConstraints()) throw validateException;

        this.id = id;
        this.name = name;
        this.salt = Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        this.passwordHash = calculatePasswordHash(password, salt);
        this.email = email;
    }

    public User(UUID id, String name, String passwordHash, String email, String salt) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
        this.salt = salt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        tryThrow(checkName(name));
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        tryThrow(checkPassword(password));
        this.passwordHash = calculatePasswordHash(password, salt);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        tryThrow(checkEmail(email));
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isCorrectPassword(String password) {
        tryThrow(Constraint.nullValue(password).check(getClass(), "password"));
        return passwordHash.equals(calculatePasswordHash(password, salt));
    }

    public boolean equalsFullState(User other) {
        if(this == other) return true;
        if(getClass() != other.getClass()) return false;
        return id.equals(other.id) &&
                name.equals(other.name) &&
                passwordHash.equals(other.passwordHash) &&
                email.equals(other.email) &&
                salt.equals(other.salt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }


    private void tryThrow(Constraint constraint) {
        if(constraint != null) {
            UserValidateException e = new UserValidateException("Fail to update user.");
            e.addReason(constraint);
            throw e;
        }
    }

    private Constraint checkId(UUID id) {
        return Constraint.nullValue(id).check(getClass(), "id");
    }

    private Constraint checkName(String name) {
        return Constraint.check(getClass(), "name",
                Constraint.nullValue(name),
                Constraint.blankValue(name),
                Constraint.incorrectStringLength(name, 1, 40)
        );
    }

    private Constraint checkPassword(String password) {
        return Constraint.check(getClass(), "password",
                Constraint.nullValue(password),
                Constraint.blankValue(password),
                Constraint.incorrectStringLength(password, 8, 100)
        );
    }

    private Constraint checkEmail(String email) {
        return Constraint.check(getClass(), "password",
                Constraint.nullValue(email),
                Constraint.blankValue(email)
        );
    }

    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

}

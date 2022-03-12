package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.validation.*;

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
        Validator.create().
                field("id").notNull(id).end().
                field("name").notNull(name).
                    and(v -> v.notBlank(name)).
                    and(v -> v.stringLength(name, 1, 40)).end().
                field("password").notNull(password).
                    and(v -> v.notBlank(password)).
                    and(v -> v.stringLength(password, 8, 100)).end().
                field("email").notNull(email).
                    and(v -> v.notBlank(email)).end().
                validate("Fail to create user");
 
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
        Validator.create().
                field("name").notNull(name).
                    and(v -> v.notBlank(name)).
                    and(v -> v.stringLength(name, 1, 40)).end().
                validate("Fail to set user name");
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        Validator.create().
                field("password").notNull(password).
                    and(v -> v.notBlank(password)).
                    and(v -> v.stringLength(password, 8, 100)).end().
                validate("Fail to set user password");
        this.passwordHash = calculatePasswordHash(password, salt);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validator.create().
                field("email").notNull(email).and(v -> v.notBlank(email)).end().
                validate("Fail to set user email");
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isCorrectPassword(String password) {
        Validator.create().
                field("password").notNull(password).end().
                validate();
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


    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

}

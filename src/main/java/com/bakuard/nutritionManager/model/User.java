package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.util.AbstractBuilder;
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
        ValidateException.check(
                Rule.of("User.id").notNull(id),
                Rule.of("User.name").notNull(name).
                        and(v -> v.notBlank(name)).
                        and(v -> v.stringLength(name, 1, 40)),
                Rule.of("User.password").notNull(password).
                        and(v -> v.notBlank(password)).
                        and(v -> v.stringLength(password, 8, 100)),
                Rule.of("User.email").notNull(email).
                        and(v -> v.notBlank(email))
        );
 
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
        ValidateException.check(
                Rule.of("User.name").notNull(name).
                        and(v -> v.notBlank(name)).
                        and(v -> v.stringLength(name, 1, 40))
        );
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        ValidateException.check(
                Rule.of("User.password").notNull(password).
                        and(v -> v.notBlank(password)).
                        and(v -> v.stringLength(password, 8, 100))
        );
        this.passwordHash = calculatePasswordHash(password, salt);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        ValidateException.check(
                Rule.of("User.email").notNull(email).
                        and(v -> v.notBlank(email))
        );
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isCorrectPassword(String password) {
        ValidateException.check(
                Rule.of("User.password").notNull(password)
        );
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


    public static class Builder implements AbstractBuilder<User> {

        private UUID id;
        private String name;
        private String passwordHash;
        private String email;
        private String salt;

        public Builder() {

        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setSalt(String salt) {
            this.salt = salt;
            return this;
        }

        @Override
        public User tryBuild() throws ValidateException {
            return new User(id, name, passwordHash, email, salt);
        }

    }


    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }

}

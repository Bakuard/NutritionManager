package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class User implements Entity<User> {

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

    private User(UUID id, String name, String password, String email) {
        Validator.check(
                "User.id", notNull(id),
                "User.name", notNull(name).
                        and(() -> notBlank(name)).
                        and(() -> stringLength(name, 1, 40)),
                "User.password", notNull(password).
                        and(() -> notBlank(password)).
                        and(() -> stringLength(password, 8, 100)),
                "User.email", notNull(email).
                        and(() -> notBlank(email))
        );
 
        this.id = id;
        this.name = name;
        this.salt = Base64.getEncoder().encodeToString(SecureRandom.getSeed(255));
        this.passwordHash = calculatePasswordHash(password, salt);
        this.email = email;
    }

    private User(UUID id, String name, String passwordHash, String email, String salt) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
        this.salt = salt;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Validator.check("User.name", 
                notNull(name).
                        and(() -> notBlank(name)).
                        and(() -> stringLength(name, 1, 40))
        );
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String password) {
        Validator.check("User.password", 
                notNull(password).
                        and(() -> notBlank(password)).
                        and(() -> stringLength(password, 8, 100))
        );
        this.passwordHash = calculatePasswordHash(password, salt);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validator.check(
                "User.email", notNull(email).and(() -> notBlank(email))
        );
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isCorrectPassword(String password) {
        Validator.check("User.password", notNull(password));
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
                ", passwordHash='" + passwordHash + '\'' +
                ", email='" + email + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }


    private String calculatePasswordHash(String password, String salt) {
        return Hashing.sha256().hashBytes(password.concat(salt).getBytes(StandardCharsets.UTF_8)).toString();
    }


    public static class Builder implements AbstractBuilder<User> {

        private UUID id;
        private String name;
        private String password;
        private String email;

        public Builder() {

        }

        public Builder generateId() {
            id = UUID.randomUUID();
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public User tryBuild() throws ValidateException {
            return new User(id, name, password, email);
        }

    }


    public static class LoadBuilder implements AbstractBuilder<User> {

        private UUID id;
        private String name;
        private String passwordHash;
        private String email;
        private String salt;

        public LoadBuilder() {

        }

        public LoadBuilder setId(UUID id) {
            this.id = id;
            return this;
        }

        public LoadBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public LoadBuilder setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public LoadBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public LoadBuilder setSalt(String salt) {
            this.salt = salt;
            return this;
        }

        @Override
        public User tryBuild() throws ValidateException {
            return new User(id, name, passwordHash, email, salt);
        }

    }

}

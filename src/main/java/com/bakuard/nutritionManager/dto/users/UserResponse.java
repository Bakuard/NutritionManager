package com.bakuard.nutritionManager.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.UUID;

@Schema(description = "Данные пользователя")
public class UserResponse {

    @Schema(description = "Уникальный идентификатор пользователя в формате UUID")
    private UUID id;
    @Schema(description = "Имя пользователя")
    private String name;
    @Schema(description = "Почта пользователя")
    private String email;

    public UserResponse() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse response = (UserResponse) o;
        return Objects.equals(id, response.id) &&
                Objects.equals(name, response.name) &&
                Objects.equals(email, response.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.users;

import java.util.Objects;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse that = (UserResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}

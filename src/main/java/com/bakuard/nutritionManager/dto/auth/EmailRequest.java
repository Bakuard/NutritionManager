package com.bakuard.nutritionManager.dto.auth;

import java.util.Objects;

public class EmailRequest {

    private String email;

    public EmailRequest() {

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
        EmailRequest that = (EmailRequest) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "email='" + email + '\'' +
                '}';
    }

}

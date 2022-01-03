package com.bakuard.nutritionManager.dto.auth;

import java.util.Objects;

public class CredentialsRequest {

    private String userName;
    private String userPassword;

    public CredentialsRequest() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialsRequest that = (CredentialsRequest) o;
        return Objects.equals(userName, that.userName) && Objects.equals(userPassword, that.userPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, userPassword);
    }

    @Override
    public String toString() {
        return "CredentialsRequest{" +
                "userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }

}

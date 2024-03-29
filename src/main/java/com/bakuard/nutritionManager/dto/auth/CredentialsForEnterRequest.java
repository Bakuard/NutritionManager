package com.bakuard.nutritionManager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Учетные данные пользователя указываемые им при аутентификации, регистрации или смене учетных данных.
        """)
public class CredentialsForEnterRequest {

    @Schema(description = "Имя пользователя. Не может быть null.")
    private String userName;

    @Schema(description = "Пароль пользователя. Не может быть null.")
    private String userPassword;

    public CredentialsForEnterRequest() {

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
        CredentialsForEnterRequest that = (CredentialsForEnterRequest) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(userPassword, that.userPassword);
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

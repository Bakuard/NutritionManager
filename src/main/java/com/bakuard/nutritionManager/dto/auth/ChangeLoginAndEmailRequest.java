package com.bakuard.nutritionManager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Учетные данные пользователя (логин и почта) указываемые им при смене почты и логина через личный кабинет
        """)
public class ChangeLoginAndEmailRequest {

    @Schema(description = "Новое или текущее имя пользователя (логин)")
    private String userName;

    @Schema(description = "Новая или текущая почта пользователя")
    private String userEmail;

    @Schema(description = "Текущий пароль пользователя для подтверждения")
    private String userPassword;

    public ChangeLoginAndEmailRequest() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
        ChangeLoginAndEmailRequest that = (ChangeLoginAndEmailRequest) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(userEmail, that.userEmail) &&
                Objects.equals(userPassword, that.userPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, userEmail, userPassword);
    }

    @Override
    public String toString() {
        return "CredentialForPersonalAreaRequest{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }

}

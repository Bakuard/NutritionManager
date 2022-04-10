package com.bakuard.nutritionManager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Новый и текущий пароль пользователя. Используется для смены пароля через личный кабинет.
        """)
public class ChangePasswordRequest {

    @Schema(description = "Новый пароль пользователя. Не может быть null.")
    private String userNewPassword;

    @Schema(description = "Текущий пароль пользователя. Не может быть null.")
    private String userCurrentPassword;

    public ChangePasswordRequest() {

    }

    public String getUserNewPassword() {
        return userNewPassword;
    }

    public void setUserNewPassword(String userNewPassword) {
        this.userNewPassword = userNewPassword;
    }

    public String getUserCurrentPassword() {
        return userCurrentPassword;
    }

    public void setUserCurrentPassword(String userCurrentPassword) {
        this.userCurrentPassword = userCurrentPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangePasswordRequest that = (ChangePasswordRequest) o;
        return Objects.equals(userNewPassword, that.userNewPassword) &&
                Objects.equals(userCurrentPassword, that.userCurrentPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userNewPassword, userCurrentPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "userNewPassword='" + userNewPassword + '\'' +
                ", userCurrentPassword='" + userCurrentPassword + '\'' +
                '}';
    }

}

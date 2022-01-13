package com.bakuard.nutritionManager.dto.auth;

import com.bakuard.nutritionManager.dto.users.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        JWS токен доступа возвращаемый пользователю в случае успешного заверешения регистрации,
         смены учетных данны или аутентификации. Используется для вызова методов котролерров продуктов, блюд и меню.
        """)
public class JwsResponse {

    @Schema(description = "JWS токен доступа")
    private String jws;
    @Schema(description = "Учетные данные пользователя")
    private UserResponse user;

    public JwsResponse() {
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwsResponse that = (JwsResponse) o;
        return Objects.equals(jws, that.jws) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jws, user);
    }

    @Override
    public String toString() {
        return "JwsResponse{" +
                "jws='" + jws + '\'' +
                ", user=" + user +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.auth;

import java.util.Objects;

public class JwsResponse {

    private String jws;

    public JwsResponse() {
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwsResponse that = (JwsResponse) o;
        return Objects.equals(jws, that.jws);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jws);
    }

    @Override
    public String toString() {
        return "JwsResponse{" +
                "jws='" + jws + '\'' +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.products;

import java.util.Objects;

public class VarietyResponse {

    private String name;
    private String code;

    public VarietyResponse() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarietyResponse that = (VarietyResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }

    @Override
    public String toString() {
        return "VarietyResponse{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.products;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Данные о произовдителе продукта")
public class ManufacturerResponse {

    @Schema(description = "Наименование производителя. Данное поле не отличается от поля code")
    private String name;
    @Schema(description = "Наименование производителя. Данное поле не отличается от поля name")
    private String code;

    public ManufacturerResponse() {

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
        ManufacturerResponse that = (ManufacturerResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }

    @Override
    public String toString() {
        return "ManufacturerResponse{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

}

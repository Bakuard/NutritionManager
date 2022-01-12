package com.bakuard.nutritionManager.dto.tags;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = """
        Данные о теге продукта
        """)
public class TagRequestAndResponse {

    @Schema(description = """
            Значение тега. Данное поле ничем не оличается от поля code.
            """)
    private String name;
    @Schema(description = """
            Значение тега. Данное поле ничем не оличается от поля name.
            """)
    private String code;

    public TagRequestAndResponse() {

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
        TagRequestAndResponse that = (TagRequestAndResponse) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }

    @Override
    public String toString() {
        return "TagResponse{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Содержит данные об одной конкретной ошибке связанной с конкретным полем какого-либо объекта")
public class ConstraintResponse {

    @Schema(description = "Наименование поля")
    private String field;
    @Schema(description = "Заголовок сообщения пользователю об ошибке")
    private String title;
    @Schema(description = "Текст сообщения пользователю об ошибке")
    private String message;

    public ConstraintResponse() {

    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintResponse that = (ConstraintResponse) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(title, that.title) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, title, message);
    }

    @Override
    public String toString() {
        return "FieldExceptionResponse{" +
                "field='" + field + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}

package com.bakuard.nutritionManager.dto.exceptions;

import java.util.Objects;

public class FieldExceptionResponse {

    private String field;
    private String title;
    private String message;

    public FieldExceptionResponse() {

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
        FieldExceptionResponse that = (FieldExceptionResponse) o;
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

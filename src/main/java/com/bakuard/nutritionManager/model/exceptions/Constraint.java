package com.bakuard.nutritionManager.model.exceptions;

import java.util.Objects;

public class Constraint {

    private Class<?> classLocation;
    private String fieldName;
    private ConstraintType type;
    private String defaultMessage;

    public Constraint(Class<?> classLocation, String fieldName, ConstraintType type) {
        this.classLocation = classLocation;
        this.fieldName = fieldName;
        this.type = type;
        defaultMessage = "Incorrect " + classLocation.getName() + "." + fieldName + " -> " + type;
    }

    public Constraint(Class<?> classLocation, String fieldName, ConstraintType type, String defaultMessage) {
        this.classLocation = classLocation;
        this.fieldName = fieldName;
        this.type = type;
        this.defaultMessage = defaultMessage;
    }

    public Class<?> getClassLocation() {
        return classLocation;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ConstraintType getType() {
        return type;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getMessageKey() {
        return classLocation.getSimpleName() + "." + fieldName + "->" + type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return Objects.equals(classLocation, that.classLocation) &&
                Objects.equals(fieldName, that.fieldName) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classLocation, fieldName, type);
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "classLocation=" + classLocation +
                ", fieldName='" + fieldName + '\'' +
                ", type=" + type +
                ", defaultMessage='" + defaultMessage + '\'' +
                '}';
    }

}

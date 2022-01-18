package com.bakuard.nutritionManager.model.exceptions;

import com.bakuard.nutritionManager.model.Tag;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Constraint {

    public static Constraint check(Class<?> classLocation, String fieldName, Checker... checkers) {
        Constraint result = null;
        for(int i = 0; i < checkers.length && result == null; i++) {
            result = checkers[i].check(classLocation, fieldName);
        }
        return result;
    }


    public static <T> Constraint.Checker nullValue(T checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue == null) {
                return new Constraint(classLocation, fieldName, ConstraintType.MISSING_VALUE);
            }
            return null;
        };
    }

    public static Constraint.Checker blankValue(String checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue.isBlank()) {
                return new Constraint(classLocation, fieldName, ConstraintType.BLANK_VALUE);
            }
            return null;
        };
    }

    public static Constraint.Checker containsBlank(List<String> checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue.stream().anyMatch(String::isBlank)) {
                return new Constraint(classLocation, fieldName, ConstraintType.BLANK_VALUE,
                        classLocation.getSimpleName() + "." + fieldName + " can't contains blank item.");
            }
            return null;
        };
    }

    public static Constraint.Checker incorrectStringLength(String checkedValue, int minLength, int maxLength) {
        return (classLocation, fieldName) -> {
            if(checkedValue.length() < minLength || checkedValue.length() > maxLength) {
                return new Constraint(classLocation, fieldName, ConstraintType.INCORRECT_STRING_LENGTH,
                        "Incorrect value length for " + classLocation.getSimpleName() + "." +
                        fieldName + ". Min length = " + minLength + ", max length = " + ", actual length = " +
                        checkedValue.length());
            }
            return null;
        };
    }

    public static Constraint.Checker negativeValue(BigDecimal checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue.signum() < 0) {
                return new Constraint(classLocation, fieldName, ConstraintType.NEGATIVE_VALUE);
            }
            return null;
        };
    }

    public static Constraint.Checker notPositiveValue(BigDecimal checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue.signum() <= 0) {
                return new Constraint(classLocation, fieldName, ConstraintType.NOT_POSITIVE_VALUE);
            }
            return null;
        };
    }

    public static Constraint.Checker containsNull(List<?> checkedValue) {
        return (classLocation, fieldName) -> {
            if(checkedValue.stream().anyMatch(Objects::isNull)) {
                return new Constraint(classLocation, fieldName, ConstraintType.CONTAINS_NULL,
                        classLocation.getSimpleName() + "." + fieldName + " can't contains null item.");
            }
            return null;
        };
    }

    public static Constraint.Checker duplicateTag(List<Tag> checkedValue) {
        return (classLocation, fieldName) -> {
            boolean duplicate = false;
            for(int i = 0; i < checkedValue.size() && !duplicate; i++) {
                for(int j = i + 1; j < checkedValue.size() && !duplicate; j++) {
                    duplicate = checkedValue.get(i).equals(checkedValue.get(j));
                }
            }
            if(duplicate) {
                return new Constraint(classLocation, fieldName, ConstraintType.DUPLICATE_TAG,
                        "All tags in " + classLocation.getSimpleName() + "." + fieldName +
                        " must be unique.");
            }
            return null;
        };
    }

    public static Constraint.Checker duplicateTag(Collection<Tag> checkedValue, Tag tag) {
        return (classLocation, fieldName) -> {
            if(checkedValue.contains(tag)) {
                return new Constraint(classLocation, fieldName, ConstraintType.DUPLICATE_TAG,
                        "Tag " + tag + " already contains in " +
                                classLocation.getSimpleName() + "." + fieldName);
            }
            return null;
        };
    }

    public static Constraint.Checker notEnoughItems(List<?> checkedValue, int minItems) {
        return (classLocation, fieldName) -> {
            if(checkedValue.size() < minItems) {
                return new Constraint(classLocation, fieldName, ConstraintType.NOT_ENOUGH_ITEMS,
                        classLocation.getSimpleName() + "." + fieldName + " must contains at least " +
                        minItems + " items. Actual items number = " + checkedValue.size());
            }
            return null;
        };
    }


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
        return type + "." + classLocation.getSimpleName() + "." + fieldName;
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


    public static interface Checker {

        public Constraint check(Class<?> classLocation, String fieldName);

    }

}

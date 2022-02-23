package com.bakuard.nutritionManager.model.exceptions;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Checker {

    public static <T>Container<T> container() {
        return new Container<>();
    }

    public static Checker of(Class<?> checkedType, String operationName) {
        return new Checker(checkedType, operationName);
    }


    private Class<?> checkedType;
    private String operationName;
    private List<Constraint> constraints;
    private List<ValidateException> validateExceptions;

    private Checker(Class<?> checkedType, String operationName) {
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }


    public Checker addConstraint(String fieldName, ConstraintType type) {
        constraints.add(new Constraint(checkedType, fieldName, type));
        return this;
    }


    public <T> Checker nullValue(String fieldName, T checkedValue) {
        if(checkedValue == null) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.MISSING_VALUE,
                            fieldName + " can't be null")
            );
        }
        return this;
    }

    public Checker blankValue(String fieldName, String checkedValue) {
        if(isValid(fieldName) && checkedValue.isBlank()) {
            constraints.add(new Constraint(checkedType, fieldName, ConstraintType.BLANK_VALUE,
                    fieldName + " can't be blank"));
        }
        return this;
    }

    public Checker containsBlankValue(String fieldName, Collection<String> checkedValue) {
        if(isValid(fieldName) && checkedValue.stream().anyMatch(String::isBlank)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_BLANK,
                            fieldName + " can't contains blank item.")
            );
        }
        return this;
    }

    public Checker incorrectStringLength(String fieldName, String checkedValue, int minLength, int maxLength) {
        if(isValid(fieldName) && (checkedValue.length() < minLength || checkedValue.length() > maxLength)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.INCORRECT_STRING_LENGTH,
                    "Incorrect value length for " +  fieldName +
                            ". Min length = " + minLength +
                            ", max length = " + maxLength +
                            ", actual length = " + checkedValue.length())
            );
        }
        return this;
    }

    public Checker negativeValue(String fieldName, BigDecimal checkedValue) {
        if(isValid(fieldName) && checkedValue.signum() < 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NEGATIVE_VALUE,
                             fieldName + " can't be negative. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker negativeValue(String fieldName, long checkedValue) {
        if(isValid(fieldName) && checkedValue < 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NEGATIVE_VALUE,
                            fieldName + " can't be negative. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker notPositiveValue(String fieldName, BigDecimal checkedValue) {
        if(isValid(fieldName) && checkedValue.signum() <= 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NOT_POSITIVE_VALUE,
                            fieldName + " mus be positive. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker containsNull(String fieldName, Collection<?> checkedValue) {
        if(isValid(fieldName) && checkedValue.stream().anyMatch(Objects::isNull)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_NULL,
                            fieldName + " can't contains null item")
            );
        }
        return this;
    }

    public Checker containsNegative(String fieldName, Collection<Integer> checkedValue) {
        if(isValid(fieldName) && checkedValue.stream().anyMatch(i -> i < 0)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_NEGATIVE,
                            fieldName + " can't contains negative value")
            );
        }
        return this;
    }

    public Checker duplicateTag(String fieldName, Container<List<Tag>> checkedValue) {
        if(checkedValue.isOpen) {
            List<Tag> tags = checkedValue.get();

            boolean duplicate = false;
            for (int i = 0; i < tags.size() && !duplicate; i++) {
                for (int j = i + 1; j < tags.size() && !duplicate; j++) {
                    duplicate = tags.get(i).equals(tags.get(j));
                }
            }
            if(duplicate) {
                constraints.add(
                        new Constraint(checkedType, fieldName, ConstraintType.DUPLICATE_TAG,
                                "All tags in " + fieldName + " must be unique.")
                );
            }
        }
        return this;
    }

    public Checker duplicateTag(String fieldName, Collection<Tag> checkedValue, Tag tag) {
        if(isValid(fieldName) && checkedValue.contains(tag)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.DUPLICATE_TAG,
                    "Tag " + tag + " already contains in " + fieldName)
            );
        }
        return this;
    }

    public Checker notEnoughItems(String fieldName, Collection<?> checkedValue, int minItems) {
        if(isValid(fieldName) && checkedValue.size() < minItems) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NOT_ENOUGH_ITEMS,
                    fieldName + " must contains at least " +
                            minItems + " items. Actual items number = " + checkedValue.size())
            );
        }
        return this;
    }

    public Checker outOfRange(String fieldName, int checkedValue, int min, int max) {
        if(isValid(fieldName) && (checkedValue < min || checkedValue > max)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.OUT_OF_RANGE,
                            fieldName + " must belong [" + min + ", " + max + "]. Actual = " + checkedValue)
            );
        }
        return this;
    }


    public <T>Checker tryBuildForEach(Collection<? extends AbstractBuilder<T>> values,
                                      Container<List<T>> container) {
        List<T> result = new ArrayList<>();
        container.set(result);

        for(AbstractBuilder<T> builder : values) {
            try {
                result.add(builder.tryBuild());
            } catch (ValidateException e) {
                validateExceptions.add(e);
                container.close();
            }
        }

        return this;
    }

    public <S, T>Checker tryBuildForEach(Collection<? extends S> values,
                                         Function<S, T> factory,
                                         Container<List<T>> container) {
        List<T> result = new ArrayList<>();
        container.set(result);

        for(S value : values) {
            try {
                result.add(factory.apply(value));
            } catch (ValidateException e) {
                validateExceptions.add(e);
                container.close();
            }
        }

        return this;
    }

    public <T>Checker tryBuild(AbstractBuilder<T> builder, Container<T> container) {
        try {
            container.set(builder.tryBuild());
        } catch(ValidateException e) {
            validateExceptions.add(e);
            container.close();
        }

        return this;
    }


    public Checker checkWithServiceException() {
        if(!constraints.isEmpty()) {
            throw new ServiceException(checkedType, operationName).
                    addReasons(constraints);
        }
        return this;
    }

    public Checker checkWithServiceException(String message) {
        if(!constraints.isEmpty()) {
            throw new ServiceException(message, checkedType, operationName).
                    addReasons(constraints);
        }
        return this;
    }

    public Checker checkWithValidateException() {
        if(!validateExceptions.isEmpty() || !constraints.isEmpty()) {
            throw new ValidateException(checkedType, operationName).
                    addExcReasons(validateExceptions).
                    addReasons(constraints);
        }
        return this;
    }

    public Checker checkWithValidateException(String message) {
        if(!validateExceptions.isEmpty() || !constraints.isEmpty()) {
            throw new ValidateException(message, checkedType, operationName).
                    addExcReasons(validateExceptions).
                    addReasons(constraints);
        }
        return this;
    }

    public ServiceException createServiceException(String message) {
        return new ServiceException(message, checkedType, operationName).
                addReasons(constraints);
    }

    public ServiceException createServiceException(String message, Throwable cause) {
        return new ServiceException(message, cause, checkedType, operationName).
                addReasons(constraints);
    }


    private boolean isValid(String fieldName) {
        boolean isValid  = true;
        for(int i = 0; i < constraints.size() && isValid; i++) {
            isValid = !constraints.get(i).getFieldName().equals(fieldName);
        }
        return isValid;
    }


    @FunctionalInterface
    public static interface ServiceExceptionBuilder {

        public AbstractDomainException build(String operationName, Constraint[] constraints);

    }

    @FunctionalInterface
    public static interface ValidateExceptionBuilder {

        public AbstractDomainException build(List<ValidateException> exceptions, Constraint[] constraints);

    }

    public static class Container<T> {

        private T value;
        private boolean isOpen;

        private Container() {
            isOpen = true;
        }

        public void close() {
            this.isOpen = false;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }

    }

}

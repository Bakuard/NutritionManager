package com.bakuard.nutritionManager.model.exceptions;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class Checker {

    private static final StackWalker walker = StackWalker.getInstance(
            Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE),
            2
    );

    public static <T>Container<T> container() {
        return new Container<>();
    }

    public static Checker of() {
        StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());
        return new Checker(frame.getDeclaringClass(), frame.getMethodName());
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


    public Checker failure(String fieldName, ConstraintType type) {
        constraints.add(new Constraint(checkedType, fieldName, type));
        return this;
    }


    public <T> Checker notNull(String fieldName, T checkedValue) {
        if(canCheck(fieldName) && checkedValue == null) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.MISSING_VALUE,
                            fieldName + " can't be null")
            );
        }
        return this;
    }

    public <T> Checker isNull(String fieldName, T checkedValue) {
        if(canCheck(fieldName) && checkedValue != null) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.MUST_BE_NULL,
                            fieldName + " must be be null")
            );
        }
        return this;
    }

    public Checker notBlank(String fieldName, String checkedValue) {
        if(canCheck(fieldName) && checkedValue.isBlank()) {
            constraints.add(new Constraint(checkedType, fieldName, ConstraintType.BLANK_VALUE,
                    fieldName + " can't be blank"));
        }
        return this;
    }

    public Checker notContainsBlankValue(String fieldName, Collection<String> checkedValue) {
        if(canCheck(fieldName) && checkedValue.stream().anyMatch(String::isBlank)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_BLANK,
                            fieldName + " can't contains blank item.")
            );
        }
        return this;
    }

    public Checker stringLength(String fieldName, String checkedValue, int minLength, int maxLength) {
        if(canCheck(fieldName) && (checkedValue.length() < minLength || checkedValue.length() > maxLength)) {
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

    public Checker notNegativeValue(String fieldName, BigDecimal checkedValue) {
        if(canCheck(fieldName) && checkedValue.signum() < 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NEGATIVE_VALUE,
                             fieldName + " can't be negative. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker notNegativeValue(String fieldName, long checkedValue) {
        if(canCheck(fieldName) && checkedValue < 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NEGATIVE_VALUE,
                            fieldName + " can't be negative. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker positiveValue(String fieldName, BigDecimal checkedValue) {
        if(canCheck(fieldName) && checkedValue.signum() <= 0) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NOT_POSITIVE_VALUE,
                            fieldName + " mus be positive. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public Checker notContainsNull(String fieldName, Collection<?> checkedValue) {
        if(canCheck(fieldName) && checkedValue.stream().anyMatch(Objects::isNull)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_NULL,
                            fieldName + " can't contains null item")
            );
        }
        return this;
    }

    public Checker notContainsNegative(String fieldName, Collection<Integer> checkedValue) {
        if(canCheck(fieldName) && checkedValue.stream().anyMatch(i -> i < 0)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.CONTAINS_NEGATIVE,
                            fieldName + " can't contains negative value")
            );
        }
        return this;
    }

    public Checker notContainsDuplicateTag(String fieldName, Container<List<Tag>> checkedValue) {
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

    public Checker notContainsDuplicateTag(String fieldName, Collection<Tag> checkedValue, Tag tag) {
        if(canCheck(fieldName) && checkedValue.contains(tag)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.DUPLICATE_TAG,
                    "Tag " + tag + " already contains in " + fieldName)
            );
        }
        return this;
    }

    public Checker containsAtLeast(String fieldName, Collection<?> checkedValue, int minItems) {
        if(canCheck(fieldName) && checkedValue.size() < minItems) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NOT_ENOUGH_ITEMS,
                    fieldName + " must contains at least " +
                            minItems + " items. Actual items number = " + checkedValue.size())
            );
        }
        return this;
    }

    public Checker range(String fieldName, int checkedValue, int min, int max) {
        if(canCheck(fieldName) && (checkedValue < min || checkedValue > max)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.OUT_OF_RANGE,
                            fieldName + " must belong [" + min + ", " + max + "]. Actual = " + checkedValue)
            );
        }
        return this;
    }

    public <T> Checker equal(String fieldName, T checkedValue, T expected) {
        if(canCheck(fieldName) && !checkedValue.equals(expected)) {
            constraints.add(
                    new Constraint(checkedType, fieldName, ConstraintType.NOT_EQUALS,
                            fieldName + " has value " + checkedValue + ", but expected " + expected)
            );
        }
        return this;
    }

    public Checker correctUrl(String fieldName, String checkedValue, Container<URL> container) {
        if(canCheck(fieldName)) {
            try {
                URL url = new URL(checkedValue);
                container.set(url);
            } catch (Exception e) {
                container.close();
                constraints.add(
                        new Constraint(checkedType, fieldName, ConstraintType.MALFORMED_URL,
                                "Malformed URL = " + checkedValue)
                );
            }
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


    public Checker validate() {
        if(!validateExceptions.isEmpty() || !constraints.isEmpty()) {
            throw new ValidateException(checkedType, operationName).
                    addExcReasons(validateExceptions).
                    addReasons(constraints);
        }
        return this;
    }

    public Checker validate(String message) {
        if(!validateExceptions.isEmpty() || !constraints.isEmpty()) {
            throw new ValidateException(message, checkedType, operationName).
                    addExcReasons(validateExceptions).
                    addReasons(constraints);
        }
        return this;
    }


    private boolean canCheck(String fieldName) {
        return constraints.stream().noneMatch(c -> c.getFieldName().equals(fieldName));
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

package com.bakuard.nutritionManager.validation;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class Validator {

    private static final StackWalker walker = StackWalker.getInstance(
            Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE),
            2
    );

    public static <T>Container<T> container() {
        return new Container<>();
    }

    public static Validator create() {
        StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());
        return new Validator(
                frame.getDeclaringClass(),
                frame.getMethodName()
        );
    }


    private Class<?> checkedClass;
    private String methodName;
    private String currentField;
    private List<Result> results;
    private List<Exception> exceptions;

    private Validator(Class<?> checkedClass, String methodName) {
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        results = new ArrayList<>();
        exceptions = new ArrayList<>();
    }


    public Validator field(String fieldName) {
        currentField = fieldName;
        return this;
    }


    public Result failure(Constraint constraint) {
        return createResult(constraint, (String) null, false);
    }

    public Result failure(Constraint constraint, String messageKey) {
        return createResult(
                constraint,
                messageKey,
                null,
                true);
    }


    public <T> Result notNull(T checkedValue) {
        return createResult(
                Constraint.NOT_NULL,
                currentField + " can't be null",
                checkedValue == null
        );
    }

    public <T> Result notNull(T checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_NULL,
                messageKey,
                currentField + " can't be null",
                checkedValue == null
        );
    }

    public <T> Result isNull(T checkedValue) {
        return createResult(
                Constraint.MUST_BE_NULL,
                currentField + " must be null",
                checkedValue != null
        );
    }

    public <T> Result isNull(T checkedValue, String messageKey) {
        return createResult(
                Constraint.MUST_BE_NULL,
                messageKey,
                currentField + " must be null",
                checkedValue != null
        );
    }

    public Result notBlank(String checkedValue) {
        return createResult(
                Constraint.NOT_BLANK,
                currentField + " can't be blank",
                checkedValue.isBlank()
        );
    }

    public Result notBlank(String checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_BLANK,
                messageKey,
                currentField + " can't be blank",
                checkedValue.isBlank()
        );
    }

    public Result notContainsBlank(Collection<String> checkedValue) {
        return createResult(
                Constraint.NOT_CONTAINS_BLANK,
                currentField + " can't contains blank item.",
                checkedValue.stream().anyMatch(String::isBlank)
        );
    }

    public Result notContainsBlank(Collection<String> checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_CONTAINS_BLANK,
                messageKey,
                currentField + " can't contains blank item.",
                checkedValue.stream().anyMatch(String::isBlank)
        );
    }

    public Result stringLength(String checkedValue, int minLength, int maxLength) {
        return createResult(
                Constraint.STRING_LENGTH,
                "Incorrect string length for " +  currentField +
                        ". Min length = " + minLength +
                        ", max length = " + maxLength +
                        ", actual length = " + checkedValue.length(),
                checkedValue.length() < minLength || checkedValue.length() > maxLength
        );
    }

    public Result stringLength(String checkedValue, int minLength, int maxLength, String messageKey) {
        return createResult(
                Constraint.STRING_LENGTH,
                messageKey,
                "Incorrect string length for " +  currentField +
                        ". Min length = " + minLength +
                        ", max length = " + maxLength +
                        ", actual length = " + checkedValue.length(),
                checkedValue.length() < minLength || checkedValue.length() > maxLength
        );
    }

    public Result notNegative(BigDecimal checkedValue) {
        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                currentField + " can't be negative. Actual = " + checkedValue,
                checkedValue.signum() < 0
        );
    }

    public Result notNegative(BigDecimal checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                messageKey,
                currentField + " can't be negative. Actual = " + checkedValue,
                checkedValue.signum() < 0
        );
    }

    public Result notNegative(long checkedValue) {
        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                currentField + " can't be negative. Actual = " + checkedValue,
                checkedValue < 0
        );
    }

    public Result notNegative(long checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                messageKey,
                currentField + " can't be negative. Actual = " + checkedValue,
                checkedValue < 0
        );
    }

    public Result positiveValue(BigDecimal checkedValue) {
        return createResult(
                Constraint.POSITIVE_VALUE,
                currentField + " must be positive. Actual = " + checkedValue,
                checkedValue .signum() <= 0
        );
    }

    public Result positiveValue(BigDecimal checkedValue, String messageKey) {
        return createResult(
                Constraint.POSITIVE_VALUE,
                messageKey,
                currentField + " must be positive. Actual = " + checkedValue,
                checkedValue .signum() <= 0
        );
    }

    public Result notContainsNull(Collection<?> checkedValue) {
        return createResult(
                Constraint.NOT_CONTAINS_NULL,
                currentField + " must be positive. Actual = " + checkedValue,
                checkedValue.stream().anyMatch(Objects::isNull)
        );
    }

    public Result notContainsNull(Collection<?> checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_CONTAINS_NULL,
                messageKey,
                currentField + " must be positive. Actual = " + checkedValue,
                checkedValue.stream().anyMatch(Objects::isNull)
        );
    }

    public Result notContainsNegative(Collection<Integer> checkedValue) {
        return createResult(
                Constraint.NOT_CONTAINS_NEGATIVE,
                currentField + " can't contains negative value",
                checkedValue.stream().anyMatch(i -> i < 0)
        );
    }

    public Result notContainsNegative(Collection<Integer> checkedValue, String messageKey) {
        return createResult(
                Constraint.NOT_CONTAINS_NEGATIVE,
                messageKey,
                currentField + " can't contains negative value",
                checkedValue.stream().anyMatch(i -> i < 0)
        );
    }

    public <T> Result notContainsDuplicate(Container<List<T>> checkedValue) {
        List<T> tags = checkedValue.get();

        boolean duplicate = false;
        for (int i = 0; i < tags.size() && !duplicate; i++) {
            for (int j = i + 1; j < tags.size() && !duplicate; j++) {
                duplicate = tags.get(i).equals(tags.get(j));
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                "All items in " + currentField + " must be unique.",
                duplicate
        );
    }

    public <T> Result notContainsDuplicate(Container<List<T>> checkedValue, String messageKey) {
        List<T> tags = checkedValue.get();

        boolean duplicate = false;
        for (int i = 0; i < tags.size() && !duplicate; i++) {
            for (int j = i + 1; j < tags.size() && !duplicate; j++) {
                duplicate = tags.get(i).equals(tags.get(j));
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                messageKey,
                "All items in " + currentField + " must be unique.",
                duplicate
        );
    }

    public <T> Result notContainsItem(Collection<T> checkedValue, T item) {
        return createResult(
                Constraint.NOT_CONTAINS_ITEM,
                "Item " + item + " already contains in " + currentField,
                checkedValue.contains(item)
        );
    }

    public <T> Result notContainsItem(Collection<T> checkedValue, T item, String messageKey) {
        return createResult(
                Constraint.NOT_CONTAINS_ITEM,
                messageKey,
                "Item " + item + " already contains in " + currentField,
                checkedValue.contains(item)
        );
    }

    public Result containsAtLeast(Collection<?> checkedValue, int minItems) {
        return createResult(
                Constraint.CONTAINS_AT_LEAST,
                currentField + " must contains at least " +
                        minItems + " items. Actual items number = " + checkedValue.size(),
                checkedValue.size() < minItems
        );
    }

    public Result containsAtLeast(Collection<?> checkedValue, int minItems, String messageKey) {
        return createResult(
                Constraint.CONTAINS_AT_LEAST,
                messageKey,
                currentField + " must contains at least " +
                        minItems + " items. Actual items number = " + checkedValue.size(),
                checkedValue.size() < minItems
        );
    }

    public <T, R> Result containsTheSameItems(Collection<R> checkedValue,
                                              Collection<T> other,
                                              Function<T, R> fabric) {
        boolean isFail = checkedValue.size() != other.size() ||
                checkedValue.containsAll(other.stream().map(fabric).toList());

        return createResult(
                Constraint.CONTAINS_THE_SAME_ITEMS,
                currentField + " must contains " + other,
                isFail
        );
    }

    public <T, R> Result containsTheSameItems(Collection<R> checkedValue,
                                              Collection<T> other,
                                              Function<T, R> fabric,
                                              String messageKey) {
        boolean isFail = checkedValue.size() != other.size() ||
                checkedValue.containsAll(other.stream().map(fabric).toList());

        return createResult(
                Constraint.CONTAINS_THE_SAME_ITEMS,
                messageKey,
                currentField + " must contains " + other,
                isFail
        );
    }

    public Result range(int checkedValue, int min, int max) {
        return createResult(
                Constraint.RANGE,
                currentField + " must belong [" + min + ", " + max + "]. Actual = " + checkedValue,
                checkedValue < min || checkedValue > max
        );
    }

    public Result range(int checkedValue, int min, int max, String messageKey) {
        return createResult(
                Constraint.RANGE,
                messageKey,
                currentField + " must belong [" + min + ", " + max + "]. Actual = " + checkedValue,
                checkedValue < min || checkedValue > max
        );
    }

    public <T> Result equal(T checkedValue, T expected) {
        return createResult(
                Constraint.EQUAL,
                currentField + " has value " + checkedValue + ", but expected " + expected,
                !checkedValue.equals(expected)
        );
    }

    public <T> Result equal(T checkedValue, T expected, String messageKey) {
        return createResult(
                Constraint.EQUAL,
                messageKey,
                currentField + " has value " + checkedValue + ", but expected " + expected,
                !checkedValue.equals(expected)
        );
    }

    public Result correctUrl(String checkedValue, Container<URL> container) {
        boolean isFail = false;

        try {
            URL url = new URL(checkedValue);
            container.set(url);
        } catch (Exception e) {
            container.close();
            isFail = true;
        }

        return createResult(
                Constraint.CORRECT_URL,
                "Malformed URL = " + checkedValue,
                isFail
        );
    }

    public Result correctUrl(String checkedValue, Container<URL> container, String messageKey) {
        boolean isFail = false;

        try {
            URL url = new URL(checkedValue);
            container.set(url);
        } catch (Exception e) {
            container.close();
            isFail = true;
        }

        return createResult(
                Constraint.CORRECT_URL,
                messageKey,
                "Malformed URL = " + checkedValue,
                isFail
        );
    }

    public <S, T> Result doesNotThrow(Collection<? extends S> source,
                                      Function<S, T> factory,
                                      Container<List<T>> container) {
        List<Exception> unexpectedExceptions = new ArrayList<>();
        List<T> result = new ArrayList<>();
        container.set(result);

        for(S value : source) {
            try {
                result.add(factory.apply(value));
            } catch(Exception e) {
                unexpectedExceptions.add(e);
                container.close();
            }
        }

        exceptions.addAll(unexpectedExceptions);

        return createResult(
                Constraint.DOES_NOT_THROW,
                "Unexpected exceptions throw: " +
                        unexpectedExceptions.stream().
                                map(e -> e.getClass().getName()).
                                reduce((e1, e2) -> e1 + ", " + e2),
                !unexpectedExceptions.isEmpty()
        );
    }

    public <S, T> Result doesNotThrow(Collection<? extends S> source,
                                      Function<S, T> factory,
                                      Container<List<T>> container,
                                      String messageKey) {
        List<Exception> unexpectedExceptions = new ArrayList<>();
        List<T> result = new ArrayList<>();
        container.set(result);

        for(S value : source) {
            try {
                result.add(factory.apply(value));
            } catch(Exception e) {
                unexpectedExceptions.add(e);
                container.close();
            }
        }

        exceptions.addAll(unexpectedExceptions);

        return createResult(
                Constraint.DOES_NOT_THROW,
                messageKey,
                "Unexpected exceptions throw: " +
                        unexpectedExceptions.stream().
                                map(e -> e.getClass().getName()).
                                reduce((e1, e2) -> e1 + ", " + e2),
                !unexpectedExceptions.isEmpty()
        );
    }

    public <S, T> Result doesNotThrow(S source,
                                      Function<S, T> factory,
                                      Container<T> container) {
        boolean isFail = false;
        String logMessage = null;

        try {
            container.set(factory.apply(source));
        } catch(Exception e) {
            isFail = true;
            logMessage = "Unexpected exception throw: " + e.getClass().getName();
            exceptions.add(e);
            container.close();
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                logMessage,
                isFail
        );
    }

    public <S, T> Result doesNotThrow(S source,
                                      Function<S, T> factory,
                                      Container<T> container,
                                      String messageKey) {
        boolean isFail = false;
        String logMessage = null;

        try {
            container.set(factory.apply(source));
        } catch(Exception e) {
            isFail = true;
            logMessage = "Unexpected exception throw: " + e.getClass().getName();
            exceptions.add(e);
            container.close();
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                messageKey,
                logMessage,
                isFail
        );
    }


    public void validate() {
        if(!exceptions.isEmpty() || !results.isEmpty()) {
            ValidateException validateException = new ValidateException(checkedClass, methodName);
            exceptions.forEach(validateException::addReason);
            results.forEach(validateException::addReason);
            throw validateException;
        }
    }

    public void validate(String message) {
        if(!exceptions.isEmpty() || !results.isEmpty()) {
            ValidateException validateException = new ValidateException(message, checkedClass, methodName);
            exceptions.forEach(validateException::addReason);
            results.forEach(validateException::addReason);
            throw validateException;
        }
    }


    protected Result createResult(Constraint constraint,
                                  String userMessageKey,
                                  String logMessage,
                                  boolean isFail) {
        Result.State state = Result.State.FAIL;

        if(!isFail) {
            state = Result.State.SUCCESS;
            logMessage = null;
        }

        return new Result(
                checkedClass,
                methodName,
                currentField,
                constraint,
                state,
                null,
                userMessageKey,
                logMessage,
                this
        );
    }

    protected Result createResult(Constraint constraint,
                                  String logMessage,
                                  boolean isFail) {
        Result.State state = Result.State.FAIL;

        if(!isFail) {
            state = Result.State.SUCCESS;
            logMessage = null;
        }

        return new Result(
                checkedClass,
                methodName,
                currentField,
                constraint,
                state,
                null,
                logMessage,
                this
        );
    }


    void flushCurrentField(Result result) {
        while(result != null) {
            if(result.getState() != Result.State.SUCCESS) results.add(result);
            result = result.getNextResult();
        }
    }

}

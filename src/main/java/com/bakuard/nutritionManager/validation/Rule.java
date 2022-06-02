package com.bakuard.nutritionManager.validation;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Rule {

    public static Rule of(String ruleName) {
        return new Rule(ruleName);
    }


    private final String ruleName;

    private Rule(String ruleName) {
        this.ruleName = Objects.requireNonNull(ruleName, "ruleName can't be null");
    }

    public Result failure(Constraint constraint) {
        return createResult(
                constraint,
                null,
                ruleName,
                Result.State.FAIL
        );
    }

    public Result failure(Constraint constraint, String logMessage) {
        return createResult(
                constraint,
                logMessage,
                ruleName,
                Result.State.FAIL
        );
    }

    public Result success(Constraint constraint) {
        return createResult(
                constraint,
                null,
                ruleName,
                Result.State.SUCCESS
        );
    }

    public Result success(Constraint constraint, String logMessage) {
        return createResult(
                constraint,
                logMessage,
                ruleName,
                Result.State.SUCCESS
        );
    }

    public Result unknown(Constraint constraint) {
        return createResult(
                constraint,
                null,
                ruleName,
                Result.State.UNKNOWN
        );
    }

    public Result unknown(Constraint constraint, String logMessage) {
        return createResult(
                constraint,
                logMessage,
                ruleName,
                Result.State.UNKNOWN
        );
    }


    public <T> Result notNull(T checkedValue) {
        return notNull(checkedValue, null);
    }

    public <T> Result notNull(T checkedValue, String field) {
        String logMessage = null;
        Result.State state = Result.State.of(checkedValue != null);

        if(field != null && state != Result.State.FAIL) {
            logMessage = field + " can't be null";
        }

        return createResult(
                Constraint.NOT_NULL,
                logMessage,
                field == null ? ruleName : ruleName + "." + field,
                state
        );
    }

    public <T> Result isNull(T checkedValue) {
        Result.State state = Result.State.of(checkedValue == null);

        return createResult(
                Constraint.MUST_BE_NULL,
                null,
                ruleName,
                state
        );
    }

    public Result notBlank(String checkedValue) {
        Result.State state = checkedValue == null ?
                Result.State.UNKNOWN :
                Result.State.of(!checkedValue.isBlank());

        return createResult(
                Constraint.NOT_BLANK,
                null,
                ruleName,
                state
        );
    }

    public <T> Result noneMatch(Collection<T> checkedValue, Predicate<T> matcher) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            List<T> invalidItems = checkedValue.stream().filter(matcher).toList();

            state = Result.State.of(invalidItems.isEmpty());

            if(state != Result.State.SUCCESS) {
                logMessage = "Invalid items: " + invalidItems;
            }
        }

        return createResult(
                Constraint.NONE_MATCH,
                logMessage,
                ruleName,
                state
        );
    }

    public Result notContainsNull(Collection<?> checkedValue) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.stream().noneMatch(Objects::isNull));
        }

        return createResult(
                Constraint.NOT_CONTAINS_NULL,
                null,
                ruleName,
                state
        );
    }

    public <T> Result notContainsDuplicate(Collection<T> checkedValue) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        if(checkedValue != null) {
            List<T> duplicates = checkedValue.stream().
                    filter(v -> Collections.frequency(checkedValue, v) > 1).
                    toList();
            state = Result.State.of(duplicates.isEmpty());

            if(state == Result.State.FAIL) {
                logMessage = "Duplicate items: " + duplicates;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                logMessage,
                ruleName,
                state
        );
    }

    public <T, R> Result notContainsDuplicate(Collection<T> checkedValue, Function<T, R> mapper) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            Set<R> items = new HashSet<>();
            List<T> duplicates = checkedValue.stream().
                    filter(i -> !items.add(mapper.apply(i))).
                    toList();
            state = Result.State.of(duplicates.isEmpty());

            if(state == Result.State.FAIL) {
                logMessage = "Duplicate items: " + duplicates;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result containsTheSameItems(Collection<T> a, Collection<T> b) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            Set<T> setA = new HashSet<>(a);
            Set<T> setB = new HashSet<>(b);
            state = Result.State.of(setA.equals(setB));

            if(state == Result.State.FAIL) {
                logMessage = "a and b must contains the same items: a=" + a + ", b=" + b;
            }
        }

        return createResult(
                Constraint.CONTAINS_THE_SAME_ITEMS,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result anyMatch(Collection<T> collection, T item) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(collection.stream().anyMatch(v -> Objects.equals(v, item)));

            if(state == Result.State.FAIL) {
                logMessage = "must contain " + item;
            }
        }

        return createResult(
                Constraint.ANY_MATCH,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result anyMatch(Collection<T> collection, Predicate<T> matcher) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(collection.stream().anyMatch(matcher));

            if(state == Result.State.FAIL) {
                logMessage = "must contain item by predicate";
            }
        }

        return createResult(
                Constraint.ANY_MATCH,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result isEmpty(Collection<T> collection) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(collection.isEmpty());

            if(state == Result.State.FAIL) {
                logMessage = "collection must be empty";
            }
        }

        return createResult(
                Constraint.IS_EMPTY_COLLECTION,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result notEmpty(Collection<T> collection) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(!collection.isEmpty());

            if(state == Result.State.FAIL) {
                logMessage = "collection can't be empty";
            }
        }

        return createResult(
                Constraint.NOT_EMPTY_COLLECTION,
                logMessage,
                ruleName,
                state
        );
    }

    public Result stringLength(String checkedValue, int minLength, int maxLength) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        if(checkedValue != null) {
            state = Result.State.of(checkedValue.length() >= minLength && checkedValue.length() <= maxLength);
            if(state == Result.State.FAIL) {
                logMessage = "Incorrect string length. Min length = " + minLength +
                        ", max length = " + maxLength +
                        ", actual length = " + checkedValue.length();
            }
        }

        return createResult(
                Constraint.STRING_LENGTH,
                logMessage,
                ruleName,
                state
        );
    }

    public Result notNegative(BigDecimal checkedValue) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.signum() >= 0);

            if(state == Result.State.FAIL) {
                logMessage =  "Can't be negative. Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                logMessage,
                ruleName,
                state
        );
    }

    public Result notNegative(long checkedValue) {
        Result.State state =  Result.State.of(checkedValue >= 0);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage =  "Can't be negative. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                logMessage,
                ruleName,
                state
        );
    }

    public Result positiveValue(BigDecimal checkedValue) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.signum() > 0);

            if(state == Result.State.FAIL) {
                logMessage =  "Must be positive. Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.POSITIVE_VALUE,
                logMessage,
                ruleName,
                state
        );
    }

    public Result positiveValue(long checkedValue) {
        Result.State state = Result.State.of(checkedValue > 0);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage =  "Must be positive. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.POSITIVE_VALUE,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result notContainsItem(Collection<T> checkedValue, T item) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.stream().noneMatch(v -> Objects.equals(v, item)));

            if(state == Result.State.FAIL) {
                logMessage = "Can't contains " + item;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_ITEM,
                logMessage,
                ruleName,
                state
        );
    }

    public Result range(long checkedValue, long minInclusive, long maxExclusive) {
        Result.State state = Result.State.of(checkedValue >= minInclusive && checkedValue < maxExclusive);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Must belong [" + minInclusive + ", " + maxExclusive + "). Actual = " + checkedValue;
        }

        return createResult(
                Constraint.RANGE,
                logMessage,
                ruleName,
                state
        );
    }

    public Result rangeClosed(long checkedValue, long minInclusive, long maxInclusive) {
        Result.State state = Result.State.of(checkedValue >= minInclusive && checkedValue <= maxInclusive);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Must belong [" + minInclusive + ", " + maxInclusive + "]. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.RANGE_CLOSED,
                logMessage,
                ruleName,
                state
        );
    }

    public Result min(BigDecimal checkedValue, BigDecimal min) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.compareTo(min) >= 0);

            if(state == Result.State.FAIL) {
                logMessage = "Must be greater or equal " + min + ". Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.MIN,
                logMessage,
                ruleName,
                state
        );
    }

    public Result min(long checkedValue, long min) {
        Result.State state = Result.State.of(checkedValue >= min);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Must be greater or equal " + min + ". Actual = " + checkedValue;
        }

        return createResult(
                Constraint.MIN,
                logMessage,
                ruleName,
                state
        );
    }

    public Result max(BigDecimal checkedValue, BigDecimal max) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.compareTo(max) <= 0);

            if(state == Result.State.FAIL) {
                logMessage = "Must be less or equal " + max + ". Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.MAX,
                logMessage,
                ruleName,
                state
        );
    }

    public Result max(long checkedValue, long max) {
        Result.State state = Result.State.of(checkedValue <= max);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Must be less or equal " + max + ". Actual = " + checkedValue;
        }

        return createResult(
                Constraint.MAX,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result equal(T a, T b) {
        Result.State state = Result.State.of(Objects.equals(a, b));
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Not equal: " + a + ", " + b;
        }

        return createResult(
                Constraint.EQUAL,
                logMessage,
                ruleName,
                state
        );
    }

    public <T> Result equal(T a, T b, Comparator<T> comparator) {
        Result.State state = Result.State.of(comparator.compare(a, b) == 0);
        String logMessage = null;

        if(state == Result.State.FAIL) {
            logMessage = "Not equal: " + a + ", " + b;
        }

        return createResult(
                Constraint.EQUAL,
                logMessage,
                ruleName,
                state
        );
    }
    
    public Result lessThen(BigDecimal a, BigDecimal b) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.compareTo(b) < 0);

            if(state == Result.State.FAIL) {
                logMessage = "a must be less than b: a=" + a + ", b=" + b;
            }
        }

        return createResult(
                Constraint.LESS_THEN,
                logMessage,
                ruleName,
                state
        );
    }

    public Result greaterThen(BigDecimal a, BigDecimal b) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.compareTo(b) > 0);

            if(state == Result.State.FAIL) {
                logMessage = "a must be greater than b: a=" + a + ", b=" + b;
            }
        }

        return createResult(
                Constraint.GREATER_THEN,
                logMessage,
                ruleName,
                state
        );
    }

    public Result differentSigns(BigDecimal a, BigDecimal b) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.signum() != b.signum());

            if(state == Result.State.FAIL) {
                logMessage = "a and b must have different signs: a=" + a + ", b=" + b;
            }
        }

        return createResult(
                Constraint.DIFFERENT_SIGNS,
                logMessage,
                ruleName,
                state
        );
    }

    public Result isUrl(String checkedValue, Container<URL> container) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            try {
                URL url = new URL(checkedValue);
                container.set(url);
                state = Result.State.SUCCESS;
            } catch(Exception e) {
                logMessage = "Incorrect url = " + checkedValue;
                state = Result.State.FAIL;
            }
        }

        return createResult(
                Constraint.IS_URL,
                logMessage,
                ruleName,
                state
        );
    }

    public Result isBigDecimal(String checkedValue, Container<BigDecimal> container) {
        container.clear();

        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            try {
                BigDecimal decimal = new BigDecimal(checkedValue);
                state = Result.State.SUCCESS;
                container.set(decimal);
            } catch (Exception e) {
                state = Result.State.FAIL;
                logMessage = "Incorrect BigDecimal format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_BIG_DECIMAL,
                logMessage,
                ruleName,
                state
        );
    }

    public Result isLong(String checkedValue, Container<Long> container) {
        container.clear();

        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            try {
                long decimal = Long.parseLong(checkedValue);
                state = Result.State.SUCCESS;
                container.set(decimal);
            } catch (Exception e) {
                state = Result.State.FAIL;
                logMessage = "Incorrect Long format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_LONG,
                logMessage,
                ruleName,
                state
        );
    }

    public Result isInteger(String checkedValue, Container<Integer> container) {
        container.clear();

        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            try {
                int decimal = Integer.parseInt(checkedValue);
                state = Result.State.SUCCESS;
                container.set(decimal);
            } catch (Exception e) {
                state = Result.State.FAIL;
                logMessage = "Incorrect Integer format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_INTEGER,
                logMessage,
                ruleName,
                state
        );
    }

    public Result isTrue(boolean checkedValue) {
        Result.State state = Result.State.of(checkedValue);

        return createResult(
                Constraint.IS_TRUE,
                null,
                ruleName,
                state
        );
    }

    public <S, T> Result doesNotThrows(Collection<? extends S> source,
                                       Function<? super S, ? extends T> factory,
                                       Container<List<T>> container) {
        container.clear();

        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        List<Exception> unexpectedExceptions = new ArrayList<>();

        if(source != null) {
            List<T> result = new ArrayList<>();
            container.set(result);

            state = Result.State.SUCCESS;
            for(S value : source) {
                try {
                    result.add(factory.apply(value));
                } catch(Exception e) {
                    unexpectedExceptions.add(e);
                    state = Result.State.FAIL;
                }
            }

            logMessage = unexpectedExceptions.stream().
                    map(e -> e.getClass().getName()).
                    reduce((a, b) -> a + ", " + b).
                    orElse(null);
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                logMessage,
                ruleName,
                state,
                unexpectedExceptions
        );
    }

    public <S, T> Result doesNotThrow(S source,
                                      Function<S, T> factory,
                                      Container<T> container) {
        container.clear();

        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        List<Exception> unexpectedExceptions = new ArrayList<>();

        if(source != null) {
            try {
                container.set(factory.apply(source));
                state = Result.State.SUCCESS;
            } catch(Exception e) {
                state = Result.State.FAIL;
                unexpectedExceptions.add(e);

                logMessage = e.getClass().getName();
            }
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                logMessage,
                ruleName,
                state,
                unexpectedExceptions
        );
    }


    protected Result createResult(Constraint constraint,
                                  String logMessage,
                                  String ruleName,
                                  Result.State state) {
        return new Result(
                constraint,
                state,
                logMessage,
                ruleName,
                this,
                List.of()
        );
    }

    protected Result createResult(Constraint constraint,
                                  String logMessage,
                                  String ruleName,
                                  Result.State state,
                                  List<Exception> suppressedExceptions) {
        return new Result(
                constraint,
                state,
                logMessage,
                ruleName,
                this,
                suppressedExceptions
        );
    }

}

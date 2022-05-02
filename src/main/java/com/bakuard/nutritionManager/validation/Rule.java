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
                getRuleName(field),
                state
        );
    }

    public <T> Result isNull(T checkedValue) {
        return isNull(checkedValue, null);
    }

    public <T> Result isNull(T checkedValue, String field) {
        String logMessage = null;
        Result.State state = Result.State.of(checkedValue == null);

        if(field != null && state != Result.State.FAIL) {
            logMessage = field + " must be null";
        }

        return createResult(
                Constraint.MUST_BE_NULL,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result notBlank(String checkedValue) {
        return notBlank(checkedValue, null);
    }

    public Result notBlank(String checkedValue, String field) {
        String logMessage = null;
        Result.State state = checkedValue == null ?
                Result.State.UNKNOWN :
                Result.State.of(!checkedValue.isBlank());

        if(field != null) {
            logMessage = field + " cant' be blank";
        }

        return createResult(
                Constraint.NOT_BLANK,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result notContains(Collection<T> checkedValue,
                                  Function<T, Result.State> function) {
        return notContains(checkedValue, function, null);
    }

    public <T> Result notContains(Collection<T> checkedValue,
                                  Function<T, Result.State> function,
                                  String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = checkedValue.stream().
                    map(function).
                    reduce(Result.State::and).
                    orElse(Result.State.SUCCESS);

            List<T> invalidItems = checkedValue.stream().
                    filter(v -> function.apply(v) == Result.State.FAIL).
                    toList();

            List<T> unknownItems = checkedValue.stream().
                    filter(v -> function.apply(v) == Result.State.UNKNOWN).
                    toList();

            if(field == null && state != Result.State.SUCCESS) {
                logMessage = "Invalid items: " + invalidItems + ", Unknown items: " + unknownItems;
            } else if(state != Result.State.SUCCESS) {
                logMessage = field + " can't contains items: " + invalidItems +
                        ", Unknown items: " + unknownItems;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_BY_CONDITION,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result notContainsNull(Collection<?> checkedValue) {
        return notContainsNull(checkedValue, null);
    }

    public Result notContainsNull(Collection<?> checkedValue, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.stream().noneMatch(Objects::isNull));

            if(field != null && state == Result.State.FAIL) {
                logMessage = field + " can't contains null";
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_NULL,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result notContainsDuplicate(Collection<T> checkedValue) {
        return notContainsDuplicate(checkedValue, (String) null);
    }

    public <T> Result notContainsDuplicate(Collection<T> checkedValue, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        if(checkedValue != null) {
            List<T> duplicates = checkedValue.stream().
                    filter(v -> Collections.frequency(checkedValue, v) > 1).
                    toList();
            state = Result.State.of(duplicates.isEmpty());

            if(field != null && state == Result.State.FAIL) {
                logMessage = field + " cant' contains duplicate items. Duplicate items: " + duplicates;
            } else if(state == Result.State.FAIL) {
                logMessage = "Duplicate items: " + duplicates;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T, R> Result notContainsDuplicate(Collection<T> checkedValue, Function<T, R> mapper) {
        return notContainsDuplicate(checkedValue, mapper, null);
    }

    public <T, R> Result notContainsDuplicate(Collection<T> checkedValue, Function<T, R> mapper, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            Set<R> items = new HashSet<>();
            List<T> duplicates = checkedValue.stream().
                    filter(i -> !items.add(mapper.apply(i))).
                    toList();
            state = Result.State.of(duplicates.isEmpty());

            if(field != null && state == Result.State.FAIL) {
                logMessage = field + " cant' contains duplicate items. Duplicate items: " + duplicates;
            } else if(state == Result.State.FAIL) {
                logMessage = "Duplicate items: " + duplicates;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_DUPLICATE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result containsTheSameItems(Collection<T> a, Collection<T> b) {
        return containsTheSameItems(a, b, null, null);
    }

    public <T> Result containsTheSameItems(Collection<T> a, Collection<T> b, String firstField, String secondField) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            Set<T> setA = new HashSet<>(a);
            Set<T> setB = new HashSet<>(b);
            state = Result.State.of(setA.equals(setB));

            if((firstField == null || secondField == null) && state == Result.State.FAIL) {
                logMessage = "a and b must contains the same items: a=" + a + ", b=" + b;
            } else if(state == Result.State.FAIL) {
                logMessage = firstField + " and " + secondField + " must contains the same items: " +
                        firstField + "=" + a + ", " + secondField + "=" + b;
            }
        }

        return createResult(
                Constraint.CONTAINS_THE_SAME_ITEMS,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }

    public <T> Result containsItem(Collection<T> collection, T item) {
        return containsItem(collection, item, null);
    }

    public <T> Result containsItem(Collection<T> collection, T item, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(collection.stream().anyMatch(v -> Objects.equals(v, item)));

            if(field == null && state == Result.State.FAIL) {
                logMessage = "must contain " + item;
            } else if(state == Result.State.FAIL) {
                logMessage = field + " must contain " + item;
            }
        }

        return createResult(
                Constraint.CONTAINS_ITEM,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result containsItem(Collection<T> collection, Predicate<T> matcher) {
        return containsItem(collection, matcher, null);
    }

    public <T> Result containsItem(Collection<T> collection, Predicate<T> matcher, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(collection != null) {
            state = Result.State.of(collection.stream().anyMatch(matcher));

            if(field == null && state == Result.State.FAIL) {
                logMessage = "must contain item by predicate";
            } else if(state == Result.State.FAIL) {
                logMessage = field + " must contain item by predicate";
            }
        }

        return createResult(
                Constraint.CONTAINS_ITEM,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result stringLength(String checkedValue, int minLength, int maxLength) {
        return stringLength(checkedValue, minLength, maxLength, null);
    }

    public Result stringLength(String checkedValue, int minLength, int maxLength, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;
        if(checkedValue != null) {
            state = Result.State.of(checkedValue.length() >= minLength && checkedValue.length() <= maxLength);
            if(field == null && state == Result.State.FAIL) {
                logMessage = "Incorrect string length. Min length = " + minLength +
                        ", max length = " + maxLength +
                        ", actual length = " + checkedValue.length();
            } else if(state == Result.State.FAIL) {
                logMessage = "Incorrect string length for field " + field +
                        ". Min length = " + minLength +
                        ", max length = " + maxLength +
                        ", actual length = " + checkedValue.length();
            }
        }

        return createResult(
                Constraint.STRING_LENGTH,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result notNegative(BigDecimal checkedValue) {
        return notNegative(checkedValue, null);
    }

    public Result notNegative(BigDecimal checkedValue, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.signum() >= 0);

            if(field == null && state == Result.State.FAIL) {
                logMessage =  "Can't be negative. Actual = " + checkedValue;
            } else if(state == Result.State.FAIL) {
                logMessage =  field + " can't be negative. Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result notNegative(long checkedValue) {
        return notNegative(checkedValue, null);
    }

    public Result notNegative(long checkedValue, String field) {
        Result.State state =  Result.State.of(checkedValue >= 0);
        String logMessage = null;

        if(field == null && state == Result.State.FAIL) {
            logMessage =  "Can't be negative. Actual = " + checkedValue;
        } else if(state == Result.State.FAIL) {
            logMessage =  field + " can't be negative. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.NOT_NEGATIVE_VALUE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result positiveValue(BigDecimal checkedValue) {
        return positiveValue(checkedValue, null);
    }

    public Result positiveValue(BigDecimal checkedValue, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.signum() > 0);

            if(field == null && state == Result.State.FAIL) {
                logMessage =  "Must be positive. Actual = " + checkedValue;
            } else if(state == Result.State.FAIL) {
                logMessage =  field + " must be positive. Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.POSITIVE_VALUE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result positiveValue(long checkedValue) {
        return positiveValue(checkedValue, null);
    }

    public Result positiveValue(long checkedValue, String field) {
        Result.State state = Result.State.of(checkedValue > 0);
        String logMessage = null;

        if(field == null && state == Result.State.FAIL) {
            logMessage =  "Must be positive. Actual = " + checkedValue;
        } else if(state == Result.State.FAIL) {
            logMessage =  field + " must be positive. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.POSITIVE_VALUE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result notContainsItem(Collection<T> checkedValue, T item) {
        return notContainsItem(checkedValue, item, null);
    }

    public <T> Result notContainsItem(Collection<T> checkedValue, T item, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.stream().noneMatch(v -> Objects.equals(v, item)));

            if(field == null && state == Result.State.FAIL) {
                logMessage = "Can't contains " + item;
            } else if(state == Result.State.FAIL) {
                logMessage = field + " can't contains " + item;
            }
        }

        return createResult(
                Constraint.NOT_CONTAINS_ITEM,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result range(long checkedValue, long min, long max) {
        return range(checkedValue, min, max, null);
    }

    public Result range(long checkedValue, long min, long max, String field) {
        Result.State state = Result.State.of(checkedValue >= min && checkedValue <= max);
        String logMessage = null;

        if(field == null && state == Result.State.FAIL) {
            logMessage = "Must belong [" + min + ", " + max + "]. Actual = " + checkedValue;
        } else if(state == Result.State.FAIL) {
            logMessage = field + " must belong [" + min + ", " + max + "]. Actual = " + checkedValue;
        }

        return createResult(
                Constraint.RANGE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result min(BigDecimal checkedValue, BigDecimal min) {
        return min(checkedValue, min, null);
    }

    public Result min(BigDecimal checkedValue, BigDecimal min, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.compareTo(min) >= 0);

            if(field == null && state == Result.State.FAIL) {
                logMessage = "Must be greater or equal " + min + ". Actual = " + checkedValue;
            } else if(state == Result.State.FAIL) {
                logMessage = field + " must be greater or equal " + min + ". Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.MIN,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result min(long checkedValue, long min) {
        return min(checkedValue, min, null);
    }

    public Result min(long checkedValue, long min, String field) {
        Result.State state = Result.State.of(checkedValue >= min);
        String logMessage = null;

        if(field == null && state == Result.State.FAIL) {
            logMessage = "Must be greater or equal " + min + ". Actual = " + checkedValue;
        } else if(state == Result.State.FAIL) {
            logMessage = field + " must be greater or equal " + min + ". Actual = " + checkedValue;
        }

        return createResult(
                Constraint.MIN,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result max(BigDecimal checkedValue, BigDecimal max) {
        return min(checkedValue, max, null);
    }

    public Result max(BigDecimal checkedValue, BigDecimal max, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            state = Result.State.of(checkedValue.compareTo(max) <= 0);

            if(field == null && state == Result.State.FAIL) {
                logMessage = "Must be less or equal " + max + ". Actual = " + checkedValue;
            } else if(state == Result.State.FAIL) {
                logMessage = field + " must be less or equal " + max + ". Actual = " + checkedValue;
            }
        }

        return createResult(
                Constraint.MAX,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result max(long checkedValue, long max) {
        return min(checkedValue, max, null);
    }

    public Result max(long checkedValue, long max, String field) {
        Result.State state = Result.State.of(checkedValue <= max);
        String logMessage = null;

        if(field == null && state == Result.State.FAIL) {
            logMessage = "Must be less or equal " + max + ". Actual = " + checkedValue;
        } else if(state == Result.State.FAIL) {
            logMessage = field + " must be less or equal " + max + ". Actual = " + checkedValue;
        }

        return createResult(
                Constraint.MAX,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <T> Result equal(T a, T b) {
        return equal(a, b, null, null);
    }

    public <T> Result equal(T a, T b, String firstField, String secondField) {
        Result.State state = Result.State.of(Objects.equals(a, b));
        String logMessage = null;

        if((firstField == null || secondField == null) && state == Result.State.FAIL) {
            logMessage = "Not equal: " + a + ", " + b;
        } else if(state == Result.State.FAIL) {
            logMessage = firstField + " must be equal " + secondField + ": " + a + ", " + b;
        }

        return createResult(
                Constraint.EQUAL,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }

    public <T> Result equal(T a, T b, Comparator<T> comparator) {
        return equal(a, b, comparator, null, null);
    }

    public <T> Result equal(T a, T b, Comparator<T> comparator, String firstField, String secondField) {
        Result.State state = Result.State.of(comparator.compare(a, b) == 0);
        String logMessage = null;

        if((firstField == null || secondField == null) && state == Result.State.FAIL) {
            logMessage = "Not equal: " + a + ", " + b;
        } else if(state == Result.State.FAIL) {
            logMessage = firstField + " must be equal " + secondField + ": " + a + ", " + b;
        }

        return createResult(
                Constraint.EQUAL,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }

    public Result lessThen(BigDecimal a, BigDecimal b) {
        return lessThen(a, b, null, null);
    }
    
    public Result lessThen(BigDecimal a, BigDecimal b, String firstField, String secondField) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.compareTo(b) < 0);

            if((firstField == null || secondField == null) && state == Result.State.FAIL) {
                logMessage = "a must be less than b: a=" + a + ", b=" + b;
            } else if(state == Result.State.FAIL) {
                logMessage = firstField + " must be less than " + secondField + ": " + 
                        firstField + "=" + a + ", " + secondField + "=" + b;
            }
        }

        return createResult(
                Constraint.LESS_THEN,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }
    
    public Result greaterThen(BigDecimal a, BigDecimal b) {
        return greaterThen(a, b, null, null);
    }

    public Result greaterThen(BigDecimal a, BigDecimal b, String firstField, String secondField) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.compareTo(b) > 0);

            if((firstField == null || secondField == null) && state == Result.State.FAIL) {
                logMessage = "a must be greater than b: a=" + a + ", b=" + b;
            } else if(state == Result.State.FAIL) {
                logMessage = firstField + " must be greater than " + secondField + ": " +
                        firstField + "=" + a + ", " + secondField + "=" + b;
            }
        }

        return createResult(
                Constraint.GREATER_THEN,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }

    public Result differentSigns(BigDecimal a, BigDecimal b) {
        return differentSigns(a, b, null, null);
    }

    public Result differentSigns(BigDecimal a, BigDecimal b, String firstField, String secondField) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(a != null && b != null) {
            state = Result.State.of(a.signum() != b.signum());

            if((firstField == null || secondField == null) && state == Result.State.FAIL) {
                logMessage = "a and b must have different signs: a=" + a + ", b=" + b;
            } else if(state == Result.State.FAIL) {
                logMessage = firstField + "and" + secondField + " must have different signs: " +
                        firstField + "=" + a + ", " + secondField + "=" + b;
            }
        }

        return createResult(
                Constraint.DIFFERENT_SIGNS,
                logMessage,
                getRuleName(firstField, secondField),
                state
        );
    }

    public Result isUrl(String checkedValue, Container<URL> container) {
        return isUrl(checkedValue, container, null);
    }

    public Result isUrl(String checkedValue, Container<URL> container, String field) {
        Result.State state = Result.State.UNKNOWN;
        String logMessage = null;

        if(checkedValue != null) {
            try {
                URL url = new URL(checkedValue);
                container.set(url);
                state = Result.State.SUCCESS;
            } catch(Exception e) {
                if(field == null) logMessage = "Incorrect url = " + checkedValue;
                else logMessage = field + " is incorrect ulr = " + checkedValue;
                state = Result.State.FAIL;
            }
        }

        return createResult(
                Constraint.IS_URL,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result isBigDecimal(String checkedValue, Container<BigDecimal> container) {
        return isBigDecimal(checkedValue, container, null);
    }

    public Result isBigDecimal(String checkedValue, Container<BigDecimal> container, String field) {
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
                if(field == null) logMessage = "Incorrect BigDecimal format = " + checkedValue;
                else logMessage = field + " is incorrect BigDecimal format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_BIG_DECIMAL,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result isLong(String checkedValue, Container<Long> container) {
        return isLong(checkedValue, container, null);
    }

    public Result isLong(String checkedValue, Container<Long> container, String field) {
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
                if(field == null) logMessage = "Incorrect Long format = " + checkedValue;
                else logMessage = field + " is incorrect Long format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_LONG,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result isInteger(String checkedValue, Container<Integer> container) {
        return isInteger(checkedValue, container, null);
    }

    public Result isInteger(String checkedValue, Container<Integer> container, String field) {
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
                if(field == null) logMessage = "Incorrect Integer format = " + checkedValue;
                else logMessage = field + " is incorrect Integer format = " + checkedValue;
            }
        }

        return createResult(
                Constraint.IS_INTEGER,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public Result isTrue(boolean checkedValue) {
        return isTrue(checkedValue, null);
    }

    public Result isTrue(boolean checkedValue, String field) {
        Result.State state = Result.State.of(checkedValue);
        String logMessage = null;

        if(state == Result.State.FAIL && field != null) {
            logMessage = field + " must be true. Actual: false";
        } else if(state == Result.State.FAIL) {
            logMessage = "must be true. Actual: false";
        }

        return createResult(
                Constraint.IS_TRUE,
                logMessage,
                getRuleName(field),
                state
        );
    }

    public <S, T> Result doesNotThrows(Collection<S> source,
                                       Function<S, T> factory,
                                       Container<List<T>> container) {
        return doesNotThrows(source, factory, container, null);
    }

    public <S, T> Result doesNotThrows(Collection<S> source,
                                       Function<S, T> factory,
                                       Container<List<T>> container,
                                       String field) {
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
            if(field != null) logMessage = "Unexpected exception for " + field + ": " + logMessage;
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                logMessage,
                getRuleName(field),
                state,
                unexpectedExceptions
        );
    }

    public <S, T> Result doesNotThrow(S source,
                                      Function<S, T> factory,
                                      Container<T> container) {
        return doesNotThrow(source, factory, container, null);
    }

    public <S, T> Result doesNotThrow(S source,
                                      Function<S, T> factory,
                                      Container<T> container,
                                      String field) {
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
                if(field != null) logMessage = "Unexpected exception for " + field + ": " + logMessage;
            }
        }

        return createResult(
                Constraint.DOES_NOT_THROW,
                logMessage,
                getRuleName(field),
                state,
                unexpectedExceptions
        );
    }


    protected String getRuleName(String field) {
        return field == null ? ruleName : ruleName + "." + field;
    }

    protected String getRuleName(String firstField, String secondField) {
        return firstField == null || secondField == null ?
                ruleName :
                ruleName + "." + firstField + "." + secondField;
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

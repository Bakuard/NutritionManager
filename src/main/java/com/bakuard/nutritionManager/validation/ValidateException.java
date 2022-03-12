package com.bakuard.nutritionManager.validation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public class ValidateException extends RuntimeException implements Iterable<Result> {

    private Class<?> checkedClass;
    private final String methodName;
    private List<Result> results;
    private List<ValidateException> validateExceptions;

    public ValidateException(Class<?> checkedClass, String methodName) {
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Class<?> checkedClass, String methodName) {
        super(message);
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause, Class<?> checkedClass, String methodName) {
        super(message, cause);
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause, Class<?> checkedClass, String methodName) {
        super(cause);
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public boolean isOriginate(Class<?> checkedClass, String methodName) {
        return this.checkedClass == checkedClass && this.methodName.equals(methodName);
    }

    public Class<?> getCheckedClass() {
        return checkedClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUserMessageKey() {
        return checkedClass.getSimpleName() + "." + methodName;
    }

    public boolean containsConstraint(Constraint constraint) {
        return results.stream().anyMatch(r -> r.contains(constraint));
    }

    public ValidateException addReason(Exception e) {
        if(e instanceof ValidateException validateException) {
            validateExceptions.add(validateException);
            addSuppressed(validateException);
        } else if(e != null) {
            addSuppressed(e);
        }

        return this;
    }

    public ValidateException addReason(Result e) {
        if(e != null) {
            results.add(e);
        }
        return this;
    }

    public ValidateException addReason(String fieldName, Constraint constraint) {
        Result result = new Result(
                checkedClass,
                methodName,
                fieldName,
                constraint,
                Result.State.FAIL,
                null,
                null,
                null
        );

        results.add(result);
        return this;
    }

    public ValidateException addReason(String fieldName, Constraint constraint, String logMessage) {
        Result result = new Result(
                checkedClass,
                methodName,
                fieldName,
                constraint,
                Result.State.FAIL,
                null,
                logMessage,
                null
        );

        results.add(result);
        return this;
    }

    public List<Result> getConstraints() {
        return results;
    }

    public List<ValidateException> getSuppressedValidateExceptions() {
        return validateExceptions;
    }

    @Override
    public Iterator<Result> iterator() {
        return new Iterator<>() {

            private final Iterator<Result> inner;

            {
                Deque<ValidateException> stack = new ArrayDeque<>();
                List<Result> all = new ArrayList<>();

                stack.addFirst(ValidateException.this);
                while(!stack.isEmpty()) {
                    ValidateException current = stack.removeFirst();
                    all.addAll(current.results);
                    for(int i = current.validateExceptions.size() - 1; i >= 0; --i)
                        stack.addFirst(current.validateExceptions.get(i));
                }

                inner = all.iterator();
            }

            @Override
            public boolean hasNext() {
                return inner.hasNext();
            }

            @Override
            public Result next() {
                return inner.next();
            }

        };
    }

    @Override
    public void forEach(Consumer<? super Result> action) {
        Deque<ValidateException> stack = new ArrayDeque<>();

        stack.addFirst(this);
        while(!stack.isEmpty()) {
            ValidateException current = stack.removeFirst();
            for(Result e : current.results) {
                action.accept(e);
            }
            for(int i = current.validateExceptions.size() - 1; i >= 0; --i)
                stack.addFirst(current.validateExceptions.get(i));
        }
    }

    @Override
    public String getMessage() {
        StringBuilder result = new StringBuilder("Fail to ").
                append(checkedClass.getSimpleName()).
                append('.').
                append(methodName).append("()");

        if(super.getMessage() != null) result.append(": ").append(super.getMessage());

        if(!results.isEmpty()) {
            result.append(". Reasons:");
            results.forEach(c -> result.append("\n -> ").append(c.getLogMessage()));
        }

        return result.toString();
    }

}

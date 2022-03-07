package com.bakuard.nutritionManager.model.exceptions;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public class ValidateException extends RuntimeException implements Iterable<Result> {

    private Class<?> checkedType;
    private final String operationName;
    private List<Result> results;
    private List<ValidateException> validateExceptions;

    public ValidateException(Class<?> checkedType, String operationName) {
        this.checkedType = checkedType;
        this.operationName = operationName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Class<?> checkedType, String operationName) {
        super(message);
        this.checkedType = checkedType;
        this.operationName = operationName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause, Class<?> checkedType, String operationName) {
        super(message, cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause, Class<?> checkedType, String operationName) {
        super(cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        results = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public boolean isOriginate(Class<?> checkedType, String operationName) {
        return this.checkedType == checkedType && this.operationName.equals(operationName);
    }

    public Class<?> getCheckedType() {
        return checkedType;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getMessageKey() {
        return checkedType.getSimpleName() + "." + operationName;
    }

    public boolean containsConstraint(ConstraintType type) {
        return results.stream().map(Result::getType).anyMatch(c -> c == type);
    }

    public ValidateException addExcReason(ValidateException e) {
        if(e != null) {
            validateExceptions.add(e);
            addSuppressed(e);
        }
        return this;
    }

    public ValidateException addExcReasons(List<ValidateException> exceptions) {
        if(exceptions != null) {
            validateExceptions.addAll(exceptions);
            exceptions.forEach(this::addSuppressed);
        }
        return this;
    }

    public ValidateException addReason(Result e) {
        if(e != null) {
            results.add(e);
        }
        return this;
    }

    public ValidateException addReasons(Result... results) {
        if(results != null && results.length > 0) {
            Collections.addAll(this.results, results);
        }
        return this;
    }

    public ValidateException addReasons(List<Result> results) {
        if(results != null && results.size() > 0) {
            this.results.addAll(results);
        }
        return this;
    }

    public ValidateException addReason(String fieldName, ConstraintType type) {
        results.add(new Result(checkedType, fieldName, type));
        return this;
    }

    public List<Result> getConstraints() {
        return results;
    }

    public List<ValidateException> getValidateExceptions() {
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
                append(checkedType.getSimpleName()).
                append('.').
                append(operationName).
                append(" - ").
                append(super.getMessage()).
                append(". Reasons:");

        results.forEach(
                c -> result.append('\n').
                        append(c.getMessageKey()).
                        append(". Detail: ").
                        append(c.getDetail())
        );

        return result.toString();
    }

}

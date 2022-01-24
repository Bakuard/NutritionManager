package com.bakuard.nutritionManager.model.exceptions;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public class ValidateException extends AbstractDomainException implements Iterable<Constraint> {

    private Class<?> checkedType;
    private final String operationName;
    private List<Constraint> constraints;
    private List<ValidateException> validateExceptions;

    public ValidateException(Class<?> checkedType, String operationName) {
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Class<?> checkedType, String operationName) {
        super(message);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause, Class<?> checkedType, String operationName) {
        super(message, cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause, Class<?> checkedType, String operationName) {
        super(cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
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
        return constraints.stream().map(Constraint::getType).anyMatch(c -> c == type);
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

    public ValidateException addReason(Constraint e) {
        if(e != null) {
            constraints.add(e);
        }
        return this;
    }

    public ValidateException addReasons(Constraint... constraints) {
        if(constraints != null && constraints.length > 0) {
            Collections.addAll(this.constraints, constraints);
        }
        return this;
    }

    public ValidateException addReasons(List<Constraint> constraints) {
        if(constraints != null && constraints.size() > 0) {
            this.constraints.addAll(constraints);
        }
        return this;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public List<ValidateException> getValidateExceptions() {
        return validateExceptions;
    }

    @Override
    public Iterator<Constraint> iterator() {
        return new Iterator<>() {

            private final Iterator<Constraint> inner;

            {
                Deque<ValidateException> stack = new ArrayDeque<>();
                List<Constraint> all = new ArrayList<>();

                stack.addFirst(ValidateException.this);
                while(!stack.isEmpty()) {
                    ValidateException current = stack.removeFirst();
                    all.addAll(current.constraints);
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
            public Constraint next() {
                return inner.next();
            }

        };
    }

    @Override
    public void forEach(Consumer<? super Constraint> action) {
        Deque<ValidateException> stack = new ArrayDeque<>();

        stack.addFirst(this);
        while(!stack.isEmpty()) {
            ValidateException current = stack.removeFirst();
            for(Constraint e : current.constraints) {
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

        constraints.forEach(
                c -> result.append('\n').
                        append(c.getMessageKey()).
                        append(". Detail: ").
                        append(c.getDetail())
        );

        return result.toString();
    }

}

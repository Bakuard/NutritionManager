package com.bakuard.nutritionManager.model.exceptions;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public class ValidateException extends AbstractDomainException implements Iterable<Constraint> {

    private Class<?> checkedType;
    private List<Constraint> constraints;
    private List<ValidateException> validateExceptions;

    public ValidateException(Class<?> checkedType) {
        this.checkedType = checkedType;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Class<?> checkedType) {
        super(message);
        this.checkedType = checkedType;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause, Class<?> checkedType) {
        super(message, cause);
        this.checkedType = checkedType;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause, Class<?> checkedType) {
        super(cause);
        this.checkedType = checkedType;
        constraints = new ArrayList<>();
        validateExceptions = new ArrayList<>();
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

}

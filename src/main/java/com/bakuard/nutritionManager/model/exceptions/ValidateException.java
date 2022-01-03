package com.bakuard.nutritionManager.model.exceptions;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public abstract class ValidateException extends AbstractDomainException implements Iterable<IncorrectFiledValueException> {

    protected List<IncorrectFiledValueException> filedValueExceptions;
    protected List<ValidateException> validateExceptions;

    public ValidateException() {
        filedValueExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message) {
        super(message);
        filedValueExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
        filedValueExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause) {
        super(cause);
        filedValueExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public boolean violatedConstraints() {
        return !filedValueExceptions.isEmpty() || !validateExceptions.isEmpty();
    }

    public void addReason(IncorrectFiledValueException e) {
        if(e != null) {
            filedValueExceptions.add(e);
            addSuppressed(e);
        }
    }

    public void addReason(ValidateException e) {
        if(e != null) {
            validateExceptions.add(e);
            addSuppressed(e);
        }
    }

    public List<IncorrectFiledValueException> getFiledValueExceptions() {
        return filedValueExceptions;
    }

    public List<ValidateException> getValidateExceptions() {
        return validateExceptions;
    }

    @Override
    public Iterator<IncorrectFiledValueException> iterator() {
        return new Iterator<>() {

            private final Iterator<IncorrectFiledValueException> inner;

            {
                Deque<ValidateException> stack = new ArrayDeque<>();
                List<IncorrectFiledValueException> all = new ArrayList<>();

                stack.addFirst(ValidateException.this);
                while(!stack.isEmpty()) {
                    ValidateException current = stack.removeFirst();
                    all.addAll(current.filedValueExceptions);
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
            public IncorrectFiledValueException next() {
                return inner.next();
            }

        };
    }

    @Override
    public void forEach(Consumer<? super IncorrectFiledValueException> action) {
        Deque<ValidateException> stack = new ArrayDeque<>();

        stack.addFirst(this);
        while(!stack.isEmpty()) {
            ValidateException current = stack.removeFirst();
            for(IncorrectFiledValueException e : current.filedValueExceptions) {
                action.accept(e);
            }
            for(int i = current.validateExceptions.size() - 1; i >= 0; --i)
                stack.addFirst(current.validateExceptions.get(i));
        }
    }

}

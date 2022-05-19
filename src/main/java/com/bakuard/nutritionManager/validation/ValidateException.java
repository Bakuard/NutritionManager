package com.bakuard.nutritionManager.validation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании или изменении бизнес сущности.
 */
public class ValidateException extends RuntimeException implements Iterable<RuleException> {

    public ValidateException() {}

    public ValidateException(String message) {
        super(message);
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidateException(Throwable cause) {
        super(cause);
    }

    public ValidateException addReason(Exception e) {
        if(e != null) addSuppressed(e);
        return this;
    }

    public ValidateException addReason(Result result) {
        if(result != null) addReason(result.check());
        return this;
    }

    public boolean containsConstraint(Constraint constraint) {
        return Arrays.stream(getSuppressed()).
                filter(e -> e instanceof RuleException).
                anyMatch(e -> ((RuleException)e).contains(constraint));
    }

    @Override
    public Iterator<RuleException> iterator() {
        return new Iterator<>() {

            private final Deque<Throwable> stack;
            private RuleException current;

            {
                stack = new ArrayDeque<>();
                stack.addLast(ValidateException.this);
                current = findNext();
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public RuleException next() {
                if(hasNext()) {
                    RuleException result = current;
                    current = findNext();
                    return result;
                }
                throw new NoSuchElementException();
            }

            private RuleException findNext() {
                RuleException result = null;

                while(result == null && !stack.isEmpty()) {
                    Throwable current = stack.removeFirst();
                    if(current instanceof RuleException ruleException) result = ruleException;
                    Throwable[] suppressed = current.getSuppressed();
                    for(int i = suppressed.length - 1; i >= 0; --i) stack.addFirst(suppressed[i]);
                }

                return result;
            }

        };
    }

    @Override
    public void forEach(Consumer<? super RuleException> action) {
        Deque<Throwable> stack = new ArrayDeque<>();

        stack.addLast(this);
        while(!stack.isEmpty()) {
            Throwable current = stack.removeFirst();
            if(current instanceof RuleException ruleException) action.accept(ruleException);
            Throwable[] suppressed = current.getSuppressed();
            for(int i = suppressed.length - 1; i >= 0; --i) stack.addFirst(suppressed[i]);
        }
    }

}

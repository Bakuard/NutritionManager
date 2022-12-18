package com.bakuard.nutritionManager.validation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушено один или несколько
 * инвариантов при конструировании или изменении бизнес сущности.
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

    public ValidateException addReason(Rule rule) {
        if(rule != null) addReason(rule.check());
        return this;
    }

    public boolean containsConstraint(Constraint constraint) {

        Container<Boolean> container = new Container<>();
        container.set(false);

        forEach(ruleException -> {
            if(ruleException.contains(constraint)) container.set(true);
        });

        return container.get();
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

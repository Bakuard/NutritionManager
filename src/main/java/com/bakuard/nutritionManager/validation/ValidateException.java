package com.bakuard.nutritionManager.validation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании или изменении бизнес сущности.
 */
public class ValidateException extends RuntimeException implements Iterable<RuleException> {

    private static final StackWalker walker = StackWalker.getInstance(
            Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE),
            4
    );


    private String userMessageKey;

    public ValidateException() {
        StackWalker.StackFrame frame = getFrame();
        setUserMessageKey(frame.getDeclaringClass(), frame.getMethodName());
    }

    public ValidateException(String message) {
        super(message);
        StackWalker.StackFrame frame = getFrame();
        setUserMessageKey(frame.getDeclaringClass(), frame.getMethodName());
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
        StackWalker.StackFrame frame = getFrame();
        setUserMessageKey(frame.getDeclaringClass(), frame.getMethodName());
    }

    public ValidateException(Throwable cause) {
        super(cause);
        StackWalker.StackFrame frame = getFrame();
        setUserMessageKey(frame.getDeclaringClass(), frame.getMethodName());
    }

    public ValidateException setUserMessageKey(Class<?> checkedClass, String methodName) {
        userMessageKey = checkedClass.getSimpleName() + "." + methodName;
        return this;
    }

    public ValidateException setUserMessageKey(String key) {
        userMessageKey = key;
        return this;
    }

    public ValidateException addReason(Exception e) {
        if(e != null) addSuppressed(e);
        return this;
    }

    public ValidateException addReason(Result result) {
        if(result != null) addReason(result.check());
        return this;
    }

    public String getUserMessageKey() {
        return userMessageKey;
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

    @Override
    public String getMessage() {
        StringBuilder result = new StringBuilder("Key = ").
                append(userMessageKey);

        if(super.getMessage() != null) result.append(". ").append(super.getMessage());

        return result.toString();
    }


    private StackWalker.StackFrame getFrame() {
        final Class<?> c = walker.walk(stream -> stream.
                skip(2).
                findFirst().
                orElseThrow()).
                getDeclaringClass();

        return walker.walk(stream -> stream.
                skip(2).
                dropWhile(f -> f.getMethodName().startsWith("lambda") || f.getDeclaringClass() != c).
                findFirst().
                orElseThrow()
        );
    }

}

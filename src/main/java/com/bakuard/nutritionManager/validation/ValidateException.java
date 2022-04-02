package com.bakuard.nutritionManager.validation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Обобщенный тип исключений, все наследники которого указывают, что было нарушенно один или несколько
 * инвариантов при констрировании бизнес сущности.
 */
public class ValidateException extends RuntimeException implements Iterable<RuleException> {

    private static final StackWalker walker = StackWalker.getInstance(
            Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE),
            4
    );

    public static void check(String userMessageKey, String logMessage, Result... results) {
        List<RuleException> failedResults = Arrays.stream(results).
                map(Result::check).
                filter(Objects::nonNull).
                toList();

        if(!failedResults.isEmpty()) {
            StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());

            ValidateException exception = new ValidateException(
                    frame.getDeclaringClass(),
                    frame.getMethodName(),
                    logMessage
            );
            failedResults.forEach(exception::addReason);
            exception.setUserMessageKey(userMessageKey);
            throw exception;
        }
    }

    public static void check(String userMessageKey, Result... results) {
        List<RuleException> failedResults = Arrays.stream(results).
                map(Result::check).
                filter(Objects::nonNull).
                toList();

        if(!failedResults.isEmpty()) {
            StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());

            ValidateException exception = new ValidateException(frame.getDeclaringClass(), frame.getMethodName());
            failedResults.forEach(exception::addReason);
            exception.setUserMessageKey(userMessageKey);
            throw exception;
        }
    }

    public static void check(Result... results) {
        List<RuleException> failedResults = Arrays.stream(results).
                map(Result::check).
                filter(Objects::nonNull).
                toList();

        if(!failedResults.isEmpty()) {
            StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());

            ValidateException exception = new ValidateException(frame.getDeclaringClass(), frame.getMethodName());
            failedResults.forEach(exception::addReason);
            throw exception;
        }
    }


    private final Class<?> checkedClass;
    private final String methodName;
    private final List<RuleException> ruleExceptions;
    private final List<ValidateException> validateExceptions;
    private String userMessageKey;

    public ValidateException() {
        StackWalker.StackFrame frame = getFrame();
        this.checkedClass = frame.getDeclaringClass();
        this.methodName = frame.getMethodName();
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message) {
        super(message);
        StackWalker.StackFrame frame = getFrame();
        this.checkedClass = frame.getDeclaringClass();
        this.methodName = frame.getMethodName();
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
        StackWalker.StackFrame frame = getFrame();
        this.checkedClass = frame.getDeclaringClass();
        this.methodName = frame.getMethodName();
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException(Throwable cause) {
        super(cause);
        StackWalker.StackFrame frame = getFrame();
        this.checkedClass = frame.getDeclaringClass();
        this.methodName = frame.getMethodName();
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    private ValidateException(Class<?> checkedClass, String methodName) {
        StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    private ValidateException(Class<?> checkedClass, String methodName, String logMessage) {
        super(logMessage);
        StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(1).findFirst().orElseThrow());
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        ruleExceptions = new ArrayList<>();
        validateExceptions = new ArrayList<>();
    }

    public ValidateException setUserMessageKey(String key) {
        userMessageKey = key;
        return this;
    }

    public ValidateException addReason(Exception e) {
        if(e instanceof ValidateException validateException) {
            validateExceptions.add(validateException);
            addSuppressed(validateException);
        } else if(e instanceof RuleException ruleException) {
            ruleExceptions.add(ruleException);
            addSuppressed(e);
        } else if(e != null) {
            addReason(e);
        }

        return this;
    }

    public ValidateException addReason(Result result) {
        if(result != null) addReason(result.check());
        return this;
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
        return userMessageKey != null ? userMessageKey : checkedClass.getSimpleName() + "." + methodName;
    }

    public boolean containsConstraint(Constraint constraint) {
        return ruleExceptions.stream().anyMatch(e -> e.contains(constraint));
    }

    @Override
    public Iterator<RuleException> iterator() {
        return new Iterator<>() {

            private final Iterator<RuleException> inner;

            {
                Deque<ValidateException> stack = new ArrayDeque<>();
                List<RuleException> all = new ArrayList<>();

                stack.addFirst(ValidateException.this);
                while(!stack.isEmpty()) {
                    ValidateException current = stack.removeFirst();
                    all.addAll(current.ruleExceptions);
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
            public RuleException next() {
                return inner.next();
            }

        };
    }

    @Override
    public void forEach(Consumer<? super RuleException> action) {
        Deque<ValidateException> stack = new ArrayDeque<>();

        stack.addFirst(this);
        while(!stack.isEmpty()) {
            ValidateException current = stack.removeFirst();
            for(RuleException e : current.ruleExceptions) {
                action.accept(e);
            }
            for(int i = current.validateExceptions.size() - 1; i >= 0; --i)
                stack.addFirst(current.validateExceptions.get(i));
        }
    }

    @Override
    public String getMessage() {
        StringBuilder result = new StringBuilder("Fail to ").
                append(checkedClass.getName()).
                append('.').
                append(methodName).append("()");

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

package com.bakuard.nutritionManager.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class Validator {

    private static final StackWalker walker = StackWalker.getInstance(
            Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE),
            4
    );

    public static void check(Supplier<ValidateException> exceptionFabric, Result... results) {
        List<RuleException> failedResults = Arrays.stream(results).
                map(Result::check).
                filter(Objects::nonNull).
                toList();

        if(!failedResults.isEmpty()) {
            ValidateException exception = exceptionFabric.get();
            failedResults.forEach(exception::addReason);
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

            ValidateException exception = new ValidateException().
                    setUserMessageKey(frame.getDeclaringClass(), frame.getMethodName());
            failedResults.forEach(exception::addReason);
            throw exception;
        }
    }


    private Validator() {}

}

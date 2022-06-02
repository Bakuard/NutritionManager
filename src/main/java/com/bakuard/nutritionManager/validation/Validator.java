package com.bakuard.nutritionManager.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Validator {

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
        check(ValidateException::new, results);
    }


    private Validator() {}

}

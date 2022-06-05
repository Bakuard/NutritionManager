package com.bakuard.nutritionManager.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Validator {

    public static void check(Supplier<ValidateException> exceptionFabric, Rule... rules) {
        List<RuleException> failedResults = Arrays.stream(rules).
                map(Rule::check).
                filter(Objects::nonNull).
                toList();

        if(!failedResults.isEmpty()) {
            ValidateException exception = exceptionFabric.get();
            failedResults.forEach(exception::addReason);
            throw exception;
        }
    }

    public static void check(Rule... rules) {
        check(ValidateException::new, rules);
    }

    public static void check(String ruleName, Result result) {
        check(Rule.of(ruleName, result));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5,
                             String ruleName6, Result result6) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5),
                Rule.of(ruleName6, result6));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5,
                             String ruleName6, Result result6,
                             String ruleName7, Result result7) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5),
                Rule.of(ruleName6, result6),
                Rule.of(ruleName7, result7));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5,
                             String ruleName6, Result result6,
                             String ruleName7, Result result7,
                             String ruleName8, Result result8) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5),
                Rule.of(ruleName6, result6),
                Rule.of(ruleName7, result7),
                Rule.of(ruleName8, result8));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5,
                             String ruleName6, Result result6,
                             String ruleName7, Result result7,
                             String ruleName8, Result result8,
                             String ruleName9, Result result9) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5),
                Rule.of(ruleName6, result6),
                Rule.of(ruleName7, result7),
                Rule.of(ruleName8, result8),
                Rule.of(ruleName9, result9));
    }

    public static void check(String ruleName1, Result result1,
                             String ruleName2, Result result2,
                             String ruleName3, Result result3,
                             String ruleName4, Result result4,
                             String ruleName5, Result result5,
                             String ruleName6, Result result6,
                             String ruleName7, Result result7,
                             String ruleName8, Result result8,
                             String ruleName9, Result result9,
                             String ruleName10, Result result10) {
        check(Rule.of(ruleName1, result1),
                Rule.of(ruleName2, result2),
                Rule.of(ruleName3, result3),
                Rule.of(ruleName4, result4),
                Rule.of(ruleName5, result5),
                Rule.of(ruleName6, result6),
                Rule.of(ruleName7, result7),
                Rule.of(ruleName8, result8),
                Rule.of(ruleName9, result9),
                Rule.of(ruleName10, result10));
    }


    private Validator() {}

}

package com.bakuard.nutritionManager;

import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.ValidateException;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Arrays;

public class AssertUtil {

    public static void assertValidateException(Action action,
                                               Class<?> checkedClass,
                                               String methodName,
                                               Constraint... expectedConstraints) {
        try {
            action.act();
            Assertions.fail("Expected exception, but nothing be thrown");
        } catch(Exception e) {
            if(!(e instanceof ValidateException)) {
                Assertions.fail("Unexpected exception type " + e.getClass().getName() + "\n" + e.getMessage());
            }

            ValidateException ex = (ValidateException) e;

            for(Constraint constraint : expectedConstraints) {
                if(ex.getConstraints().stream().noneMatch(r -> r.contains(constraint))) {
                    Assertions.fail("Expected constraint " + constraint + " is missing");
                }
            }

            for(Constraint constraint : Constraint.values()) {
                if(Arrays.stream(expectedConstraints).noneMatch(c -> c == constraint) &&
                        ex.getConstraints().stream().anyMatch(r -> r.contains(constraint))) {
                    Assertions.fail("Unexpected constraint " + constraint);
                }
            }

            if(!ex.isOriginate(checkedClass, methodName)) {
                Assertions.fail("Unexpected checkedClass or methodName. Expected: " +
                        checkedClass.getName() + ", " + methodName +
                        ". Actual: " + ex.getCheckedClass().getName() + ", " + ex.getMethodName()
                );
            }
        }
    }

    public static void assertEquals(BigDecimal a, BigDecimal b) {
        if(a.compareTo(b) != 0) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

}

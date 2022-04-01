package com.bakuard.nutritionManager;

import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.User;
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
                if(!ex.containsConstraint(constraint)) {
                    Assertions.fail("Expected constraint " + constraint + " is missing");
                }
            }

            for(Constraint constraint : Constraint.values()) {
                if(Arrays.stream(expectedConstraints).noneMatch(c -> c == constraint) &&
                        ex.containsConstraint(constraint)) {
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

    public static void assertValidateException(Action action,
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
                if(!ex.containsConstraint(constraint)) {
                    Assertions.fail("Expected constraint " + constraint + " is missing");
                }
            }

            for(Constraint constraint : Constraint.values()) {
                if(Arrays.stream(expectedConstraints).noneMatch(c -> c == constraint) &&
                        ex.containsConstraint(constraint)) {
                    Assertions.fail("Unexpected constraint " + constraint);
                }
            }

        }
    }

    public static void assertEquals(BigDecimal a, BigDecimal b) {
        if(a.compareTo(b) != 0) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

    public static void assertEquals(Product a, Product b) {
        if(!a.equalsFullState(b)) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

    public static void assertEquals(Dish a, Dish b) {
        if(!a.equalsFullState(b)) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

    public static void assertEquals(User a, User b) {
        if(!a.equalsFullState(b)) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

}

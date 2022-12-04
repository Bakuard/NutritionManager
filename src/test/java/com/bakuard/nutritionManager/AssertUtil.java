package com.bakuard.nutritionManager;

import com.bakuard.nutritionManager.model.Entity;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.RuleException;
import com.bakuard.nutritionManager.validation.ValidateException;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.IntStream;

public class AssertUtil {

    @SafeVarargs
    public static void assertValidateException(Runnable action,
                                               Pair<String, Constraint>... expectedConstraintsAndMessageKeys) {
        try {
            action.run();
            Assertions.fail("Expected exception, but nothing be thrown");
        } catch(Exception e) {
            if(!(e instanceof ValidateException)) {
                Assertions.fail("Unexpected exception type " + e.getClass().getName() + "\n" + e.getMessage());
            }

            ValidateException ex = (ValidateException) e;

            for(Pair<String, Constraint> pair : expectedConstraintsAndMessageKeys) {
                if(!ex.containsConstraint(pair.second())) {
                    Assertions.fail("Expected constraint " + pair.second() + " is missing");
                }
            }

            for(Constraint constraint : Constraint.values()) {
                for(RuleException exRule : ex) {
                    boolean noneMatch = Arrays.stream(expectedConstraintsAndMessageKeys).
                            noneMatch(pair -> pair.second() == constraint);
                    if(exRule.contains(constraint) && noneMatch) {
                        Assertions.fail("Unexpected constraint " + constraint +
                                " and message " + exRule.getMessage());
                    }
                }
            }

            for(RuleException exRule : ex) {
                boolean noneMatch = Arrays.stream(expectedConstraintsAndMessageKeys).
                        noneMatch(pair -> pair.first().equals(exRule.getUserMessageKey()));
                if(noneMatch) {
                    Assertions.fail("Unexpected userMessageKey '" + exRule.getUserMessageKey() + '\'');
                }
            }
        }
    }

    public static void assertValidateException(Runnable action,
                                               String expectedMessageKey,
                                               Constraint expectedConstraint) {
        try {
            action.run();
            Assertions.fail("Expected exception, but nothing be thrown");
        } catch(Exception e) {
            if(!(e instanceof ValidateException)) {
                Assertions.fail("Unexpected exception type " + e.getClass().getName() + "\n" + e.getMessage());
            }

            ValidateException ex = (ValidateException) e;

            if(!ex.containsConstraint(expectedConstraint)) {
                Assertions.fail("Expected constraint " + expectedConstraint + " is missing");
            }

            for(Constraint constraint : Constraint.values()) {
                for(RuleException exRule : ex) {
                    if(constraint != expectedConstraint && exRule.contains(constraint)) {
                        Assertions.fail("Unexpected constraint " + constraint +
                                " and message " + exRule.getMessage());
                    }
                }
            }

            for(RuleException exRule : ex) {
                if(!exRule.getUserMessageKey().equals(expectedMessageKey)) {
                    Assertions.fail("Unexpected userMessageKey '" + exRule.getUserMessageKey() + '\'');
                }
            }
        }
    }

    public static void assertValidateException(Runnable action,
                                               Constraint... expectedConstraints) {
        try {
            action.run();
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

    public static <T extends Entity<T>> void assertEquals(T a, T b) {
        if(!a.equalsFullState(b)) Assertions.fail("\nExpected: " + a + "\n  Actual: " + b);
    }

    public static <T extends Entity<T>> void assertEquals(Page<T> a, Page<T> b) {
        boolean isEqual = a.getMetadata().equals(b.getMetadata()) &&
                IntStream.range(0, a.getMetadata().getActualSize()).
                        allMatch(i -> {
                            T entityA = a.getContent().get(i);
                            T entityB = b.getContent().get(i);
                            return entityA.equalsFullState(entityB);
                        });

        if(!isEqual) Assertions.fail("\nExpected: " + a + "\n  Actual: " + b);
    }

}

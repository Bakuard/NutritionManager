package com.bakuard.nutritionManager;

import com.bakuard.nutritionManager.model.exceptions.Result;
import com.bakuard.nutritionManager.model.exceptions.ConstraintType;
import com.bakuard.nutritionManager.model.exceptions.ValidateException;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Arrays;

public class AssertUtil {

    public static void assertValidateException(Action action,
                                               Class<?> checkedType,
                                               String operationName,
                                               ConstraintType... expectedTypes) {
        try {
            action.act();
            Assertions.fail("Expected exception, but nothing be thrown");
        } catch(Exception e) {
            if(!(e instanceof ValidateException)) {
                Assertions.fail("Unexpected exception type " + e.getClass().getName() + "\n" + e.getMessage());
            }

            ValidateException ex = (ValidateException) e;

            for(ConstraintType type : expectedTypes) {
                if(ex.getConstraints().stream().map(Result::getType).noneMatch(t -> t == type)) {
                    Assertions.fail("Expected constraint type " + type + " is missing");
                }
            }

            for(Result result : ex.getConstraints()) {
                if(Arrays.stream(expectedTypes).noneMatch(t -> t == result.getType())) {
                    Assertions.fail("Unexpected constraint type " + result.getType());
                }
            }

            if(!ex.isOriginate(checkedType, operationName)) {
                Assertions.fail("Unexpected checkedType or operationName. Expected: " +
                        checkedType.getName() + ", " + operationName +
                        ". Actual: " + ex.getCheckedType().getName() + ", " + ex.getOperationName()
                );
            }
        }
    }

    public static void assertEquals(BigDecimal a, BigDecimal b) {
        if(a.compareTo(b) != 0) Assertions.fail("Expected: " + a + ", actual: " + b);
    }

}

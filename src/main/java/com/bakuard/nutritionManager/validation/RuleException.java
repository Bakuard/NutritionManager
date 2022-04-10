package com.bakuard.nutritionManager.validation;

import java.util.Arrays;
import java.util.Objects;

public class RuleException extends RuntimeException {

    private final String userMessageKey;
    private final Constraint[] constraints;

    public RuleException(String userMessageKey, String logMessage, Constraint... constraints) {
        super(logMessage, null, true, false);
        this.userMessageKey = Objects.requireNonNull(userMessageKey, "UserMessageKey can't be null");
        this.constraints = constraints;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    public boolean contains(Constraint constraint) {
        boolean find = false;
        for(int i = 0; i < constraints.length && !find; i++) {
            find = constraints[i] == constraint;
        }
        return find;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleException that = (RuleException) o;
        return userMessageKey.equals(that.userMessageKey) &&
                Arrays.equals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(userMessageKey);
        result = 31 * result + Arrays.hashCode(constraints);
        return result;
    }

}

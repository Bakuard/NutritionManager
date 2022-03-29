package com.bakuard.nutritionManager.validation;

public class RuleException extends RuntimeException {

    private final String userMessageKey;
    private final Constraint[] constraints;

    public RuleException(String userMessageKey, String logMessage, Constraint... constraints) {
        super(logMessage, null, true, false);
        this.userMessageKey = userMessageKey;
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

}

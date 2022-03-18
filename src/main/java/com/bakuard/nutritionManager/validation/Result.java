package com.bakuard.nutritionManager.validation;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public final class Result {

    public enum State {
        SUCCESS,
        FAIL
    }

    private final Class<?> checkedClass;
    private final String methodName;
    private final String fieldName;
    private final Constraint[] constraints;
    private final State state;
    private final Result nextResult;
    private final String userMessageKey;
    private final String logMessage;
    private final Validator validator;

    public Result(Class<?> checkedClass,
                  String methodName,
                  String fieldName,
                  Constraint constraint,
                  State state,
                  Result nextResult,
                  String userMessageKey,
                  String logMessage,
                  Validator validator) {
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.constraints = new Constraint[]{constraint};
        this.userMessageKey = userMessageKey != null ?
                        userMessageKey :
                        userMessageKey(checkedClass, methodName, fieldName, constraints);
        this.state = state;
        this.nextResult = nextResult;
        this.logMessage = logMessage;
        this.validator = validator;
    }

    private Result(Class<?> checkedClass,
                   String methodName,
                   String fieldName,
                   Constraint[] constraints,
                   State state,
                   Result nextResult,
                   String userMessageKey,
                   String logMessage,
                   Validator validator) {
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.constraints = constraints;
        this.state = state;
        this.nextResult = nextResult;
        this.userMessageKey = userMessageKey;
        this.logMessage = logMessage;
        this.validator = validator;
    }

    public Class<?> getCheckedClass() {
        return checkedClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Constraint getConstraint(int index) {
        return constraints[index];
    }

    public int getConstraintsNumber() {
        return constraints.length;
    }

    public boolean contains(Constraint constraint) {
        return Arrays.stream(constraints).anyMatch(c -> c == constraint);
    }

    public State getState() {
        return state;
    }

    public Result getNextResult() {
        return nextResult;
    }

    public String getUserMessageKey() {
        return userMessageKey;
    }

    public String getLogMessage() {
        String result = '(' +
                (constraints.length > 1 ?
                        "constraints " + Arrays.toString(constraints) :
                        "constraint " + constraints[0]) +
                " is " + state +
                " for variable '" + fieldName + "'. ";

        if(logMessage != null) result += logMessage;

        result += ')';

        return result;
    }

    public Result and(Function<Validator, Result> other) {
        return state == State.SUCCESS ? other.apply(validator) : this;
    }

    public Result or(Function<Validator, Result> other) {
        if(state == State.SUCCESS) return this;

        Result result = other.apply(validator);

        if(result.state != State.SUCCESS) {
            Constraint[] newConstraints = concat(constraints, result.constraints);

            result = new Result(
                    checkedClass,
                    methodName,
                    fieldName,
                    newConstraints,
                    state,
                    nextResult,
                    userMessageKey(checkedClass, methodName, fieldName, newConstraints),
                    String.join(", ", logMessage, result.logMessage),
                    validator
            );
        }

        return result;
    }

    public Result or(Function<Validator, Result> other, String userMessageKey) {
        if(state == State.SUCCESS) return this;

        Result result = other.apply(validator);

        if(result.state != State.SUCCESS) {
            Constraint[] newConstraints = concat(constraints, result.constraints);

            result = new Result(
                    checkedClass,
                    methodName,
                    fieldName,
                    newConstraints,
                    state,
                    nextResult,
                    userMessageKey,
                    String.join(", ", logMessage, result.logMessage),
                    validator
            );
        }

        return result;
    }

    public Result append(Function<Validator, Result> other) {
        return new Result(
                checkedClass,
                methodName,
                fieldName,
                constraints,
                state,
                other.apply(validator),
                userMessageKey,
                logMessage,
                validator
        );
    }

    public Validator end() {
        validator.flushCurrentField(this);
        return validator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return checkedClass.equals(result.checkedClass) &&
                methodName.equals(result.methodName) &&
                fieldName.equals(result.fieldName) &&
                Arrays.equals(constraints, result.constraints) &&
                state == result.state &&
                nextResult.equals(result.nextResult) &&
                userMessageKey.equals(result.userMessageKey) &&
                logMessage.equals(result.logMessage) &&
                validator.equals(result.validator);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(checkedClass,
                methodName,
                fieldName,
                state,
                nextResult,
                userMessageKey,
                logMessage,
                validator);
        result = 31 * result + Arrays.hashCode(constraints);
        return result;
    }


    private String userMessageKey(Class<?> checkedClass,
                                  String methodName,
                                  String fieldName,
                                  Constraint[] constraints) {
        return checkedClass.getSimpleName() + '.' + methodName + '(' + fieldName + ")[" + convertToString(constraints) + ']';
    }

    private <T> T[] concat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private String convertToString(Constraint[] constraints) {
        return Arrays.stream(constraints).
                map(Constraint::name).
                reduce((a, b) -> a + ',' + b).
                orElseThrow();
    }

}

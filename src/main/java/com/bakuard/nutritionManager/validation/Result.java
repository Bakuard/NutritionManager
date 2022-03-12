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
        this.userMessageKey = userMessageKey;
        this.state = state;
        this.nextResult = nextResult;
        this.logMessage = logMessage;
        this.validator = validator;
    }

    public Result(Class<?> checkedClass,
                  String methodName,
                  String fieldName,
                  Constraint constraint,
                  State state,
                  Result nextResult,
                  String logMessage,
                  Validator validator) {
        this.checkedClass = checkedClass;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.constraints = new Constraint[]{constraint};
        this.userMessageKey = userMessageKey();
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
        String result = checkedClass.getName() + '.' + methodName + "(..., " + fieldName + ",...)" +
                " -> " +
                (constraints.length > 1 ?
                        "constraints " + Arrays.toString(constraints) :
                        "constraint " + constraints[0]) +
                " is " + state +
                " for variable '" + fieldName + "'.";

        if(logMessage != null) result += " Detail: " + logMessage;

        return result;
    }

    public Result and(Function<Validator, Result> other) {
        return state == State.SUCCESS ? other.apply(validator) : this;
    }

    public Result or(Function<Validator, Result> other) {
        if(state == State.SUCCESS) return this;

        Result result = other.apply(validator);

        if(result.state != State.SUCCESS) {
            result = new Result(
                    checkedClass,
                    methodName,
                    fieldName,
                    concat(constraints, result.constraints),
                    state,
                    nextResult,
                    userMessageKey,
                    String.join(", ", logMessage, result.logMessage),
                    validator
            );
        }

        return result;
    }

    public Result append(Result other) {
        return new Result(
                checkedClass,
                methodName,
                fieldName,
                constraints,
                state,
                other,
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
        return state == result.state &&
                checkedClass.equals(result.checkedClass) &&
                methodName.equals(result.methodName) &&
                fieldName.equals(result.fieldName) &&
                Arrays.equals(constraints, result.constraints) &&
                nextResult.equals(result.nextResult) &&
                userMessageKey.equals(result.userMessageKey) &&
                logMessage.equals(result.logMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                checkedClass,
                methodName,
                fieldName,
                constraints,
                state,
                nextResult,
                userMessageKey,
                logMessage
        );
    }

    @Override
    public String toString() {
        return "Result{" +
                "checkedClass=" + checkedClass +
                ", methodName='" + methodName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", constraints=" + constraints +
                ", state=" + state +
                ", nextResult=" + nextResult +
                ", userMessageKey='" + userMessageKey + '\'' +
                ", logMessage='" + logMessage + '\'' +
                '}';
    }


    private String userMessageKey() {
        return checkedClass.getSimpleName() + '.' + methodName + '#' + fieldName;
    }

    private Constraint[] concat(Constraint[] array1, Constraint[] array2) {
        Constraint[] result = new Constraint[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

}

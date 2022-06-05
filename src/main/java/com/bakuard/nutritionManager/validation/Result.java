package com.bakuard.nutritionManager.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Result {

    public enum State {
        SUCCESS(1),
        UNKNOWN(0),
        FAIL(-1);

        public static State of(boolean value) {
            return value ? SUCCESS : FAIL;
        }


        private final int weight;

        private State(int weight) {
            this.weight = weight;
        }

        public State or(State other) {
            return other.weight < weight ? this : other;
        }

        public State and(State other) {
            return other.weight < weight ? other : this;
        }

        public State not() {
            int newWeight = weight * -1;

            State result = null;
            switch(newWeight) {
                case -1 -> result = FAIL;
                case 0 -> result = UNKNOWN;
                case 1 -> result = SUCCESS;
            }

            return result;
        }

    }

    private final Constraint[] constraints;
    private final State state;
    private final String logMessage;
    private final List<Exception> suppressedExceptions;

    public Result(Constraint constraint,
                  State state,
                  String logMessage,
                  List<Exception> suppressedExceptions) {
        this.constraints = new Constraint[]{Objects.requireNonNull(constraint, "constraint can't be null")};
        this.state = Objects.requireNonNull(state, "state can't be null");
        this.logMessage = logMessage;
        this.suppressedExceptions = suppressedExceptions;
    }

    private Result(Constraint[] constraints,
                   State state,
                   String logMessage,
                   List<Exception> suppressedExceptions) {
        this.constraints = constraints;
        this.state = state;
        this.logMessage = logMessage;
        this.suppressedExceptions = suppressedExceptions;
    }

    public State getState() {
        return state;
    }

    public boolean isFail() {
        return state == State.FAIL;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isUnknown() {
        return state == State.UNKNOWN;
    }

    public Result and(Supplier<Result> other) {
        Result result = null;

        switch(state) {
            case SUCCESS, UNKNOWN -> result = other.get();
            case FAIL -> result = this;
        }

        return result;
    }

    public Result or(Supplier<Result> other) {
        Result result = null;

        switch(state) {
            case SUCCESS -> result = this;
            case UNKNOWN, FAIL -> result = other.get();
        }

        switch(result.state) {
            case UNKNOWN, FAIL -> {
                ArrayList<Exception> suppressed = new ArrayList<>(suppressedExceptions);
                suppressed.addAll(result.suppressedExceptions);

                result = new Result(
                        concat(constraints, result.constraints),
                        state.or(result.state),
                        joinLogMessages(logMessage, result.logMessage),
                        suppressed
                );
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Arrays.equals(constraints, result.constraints) &&
                state == result.state &&
                Objects.equals(logMessage, result.logMessage);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(state, logMessage);
        result = 31 * result + Arrays.hashCode(constraints);
        return result;
    }

    @Override
    public String toString() {
        return "Result{" +
                ", constraints=" + Arrays.toString(constraints) +
                ", state=" + state +
                ", logMessage='" + logMessage + '\'' +
                '}';
    }


    RuleException check(String ruleName) {
        if(!isSuccess()) {
            String userMessageKey = ruleName + Arrays.toString(constraints).replaceAll(" ", "");

            StringBuilder log = new StringBuilder(ruleName).
                    append(Arrays.toString(constraints)).
                    append(" - is ").
                    append(state).
                    append(". ");
            if(logMessage != null) log.append(logMessage);

            RuleException exception = new RuleException(
                    userMessageKey,
                    log.toString(),
                    constraints
            );
            suppressedExceptions.forEach(exception::addSuppressed);
            return exception;
        }

        return null;
    }

    private <T> T[] concat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private String joinLogMessages(String message1, String message2) {
        String result = "";

        if(message1 != null && message2 != null) {
            result = String.join(", ", message1, message2);
        } else if(message1 != null) {
            result = message1;
        } else if(message2 != null) {
            result = message2;
        }

        return result;
    }

}

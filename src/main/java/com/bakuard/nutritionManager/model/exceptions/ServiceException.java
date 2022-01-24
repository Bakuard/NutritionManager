package com.bakuard.nutritionManager.model.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ServiceException extends AbstractDomainException implements Iterable<Constraint> {

    private final Class<?> checkedType;
    private final String operationName;
    private final List<Constraint> constraints;

    public ServiceException(Class<?> checkedType, String operationName) {
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
    }

    public ServiceException(String message, Class<?> checkedType, String operationName) {
        super(message);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
    }

    public ServiceException(String message, Throwable cause, Class<?> checkedType, String operationName) {
        super(message, cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
    }

    public ServiceException(Throwable cause, Class<?> checkedType, String operationName) {
        super(cause);
        this.checkedType = checkedType;
        this.operationName = operationName;
        constraints = new ArrayList<>();
    }

    public boolean isOriginate(Class<?> checkedType, String operationName) {
        return this.checkedType == checkedType && this.operationName.equals(operationName);
    }

    public Class<?> getCheckedType() {
        return checkedType;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getMessageKey() {
        return checkedType.getSimpleName() + "." + operationName;
    }

    public boolean containsConstraint(ConstraintType type) {
        return constraints.stream().map(Constraint::getType).anyMatch(c -> c == type);
    }

    public ServiceException addReason(Constraint constraint) {
        if(constraint != null) constraints.add(constraint);
        return this;
    }

    public ServiceException addReasons(Constraint... constraints) {
        if(constraints != null && constraints.length > 0) {
            Collections.addAll(this.constraints, constraints);
        }
        return this;
    }

    public ServiceException addReasons(List<Constraint> constraints) {
        if(constraints != null && constraints.size() > 0) {
            this.constraints.addAll(constraints);
        }
        return this;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public Iterator<Constraint> iterator() {
        return constraints.iterator();
    }

    @Override
    public void forEach(Consumer<? super Constraint> action) {
        constraints.forEach(action);
    }

    @Override
    public String getMessage() {
        StringBuilder result = new StringBuilder("Fail to ").
                append(checkedType.getSimpleName()).
                append('.').
                append(operationName).
                append(" - ").
                append(super.getMessage()).
                append(". Reasons:");

        constraints.forEach(
                c -> result.append('\n').
                        append(c.getMessageKey()).
                        append(". Detail: ").
                        append(c.getDetail())
        );

        return result.toString();
    }

}

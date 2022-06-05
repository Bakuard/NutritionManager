package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.validation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class Sort {

    public static Sort productDefaultSort() {
        return products().asc("category");
    }

    public static Sort dishDefaultSort() {
        return dishes().asc("name");
    }

    public static Sort menuDefaultSort() {
        return dishes().asc("name");
    }

    public static Sort products() {
        return new Sort(List.of("category", "price"));
    }

    public static Sort products(List<String> sortRules) {
        if(sortRules == null) {
            return productDefaultSort();
        } else {
            Sort sort = products();
            sortRules.stream().
                    filter(Objects::nonNull).
                    map(sortRule -> sortRule.split("_")).
                    forEach(parameters -> sort.put(parameters[0], sort.toDirection(parameters[1])));
            return sort.getParametersNumber() > 0 ? sort : productDefaultSort();
        }
    }

    public static Sort dishes() {
        return new Sort(List.of("name"));
    }

    public static Sort dishes(List<String> sortRules) {
        if(sortRules == null) {
            return dishDefaultSort();
        } else {
            Sort sort = dishes();
            sortRules.stream().
                    filter(Objects::nonNull).
                    map(sortRule -> sortRule.split("_")).
                    forEach(parameters -> sort.put(parameters[0], sort.toDirection(parameters[1])));
            return sort.getParametersNumber() > 0 ? sort : dishDefaultSort();
        }
    }

    public static Sort menus() {
        return new Sort(List.of("name"));
    }

    public static Sort menus(List<String> sortRules) {
        if(sortRules == null || sortRules.isEmpty()) {
            return menuDefaultSort();
        } else {
            Sort sort = menus();
            sortRules.stream().
                    filter(Objects::nonNull).
                    map(sortRule -> sortRule.split("_")).
                    forEach(parameters -> sort.put(parameters[0], sort.toDirection(parameters[1])));
            return sort.getParametersNumber() > 0 ? sort : menuDefaultSort();
        }
    }


    private final List<String> parameters;
    private final List<Boolean> directions;
    private final List<String> validParameters;

    private Sort(List<String> validParameters) {
        this.validParameters = validParameters;
        parameters = new ArrayList<>();
        directions = new ArrayList<>();
    }

    public Sort asc(String parameter) {
        return put(parameter, true);
    }

    public Sort desc(String parameter) {
        return put(parameter, false);
    }

    public int getParametersNumber() {
        return parameters.size();
    }

    public String getParameter(int parameterIndex) {
        return parameters.get(parameterIndex);
    }

    public boolean isAscending(int parameterIndex) {
        return directions.get(parameterIndex);
    }

    public boolean isDescending(int parameterIndex) {
        return !directions.get(parameterIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort sort = (Sort) o;
        return parameters.equals(sort.parameters) &&
                directions.equals(sort.directions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, directions);
    }

    @Override
    public String toString() {
        return "Sort{" +
                "parameters=" + parameters +
                ", directions=" + directions +
                ", validParameters=" + validParameters +
                '}';
    }


    private boolean toDirection(String direction) {
        Container<Boolean> d = new Container<>();

        Validator.check(
                () -> new ValidateException("Unknown sort direction"),
                Rule.of("Sort.direction", notNull(direction).
                        and(() -> {
                            if("asc".equalsIgnoreCase(direction)) d.set(true);
                            else if("desc".equalsIgnoreCase(direction)) d.set(false);

                            if(d.isEmpty()) return failure(Constraint.ANY_MATCH);
                            else return success(Constraint.ANY_MATCH);
                        })
                )
        );

        return d.get();
    }

    private Sort put(String parameter, boolean isAscending) {
        Validator.check(
                "Sort.parameter", notNull(parameter).and(() -> anyMatch(validParameters, parameter))
        );

        parameters.add(parameter);
        directions.add(isAscending);

        return this;
    }

}

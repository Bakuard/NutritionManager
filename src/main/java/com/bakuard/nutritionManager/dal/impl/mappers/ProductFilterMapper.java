package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;

public class ProductFilterMapper {

    public ProductFilterMapper() {

    }

    public String toCondition(Filter filter) {
        filter = filter.toDnf();

        if(hasFilterCorrectStructure(filter)) {
            return parseFilterWithExtendedStructure(filter).toString();
        } else {
            throw new ValidateException("Incorrect filter structure:\n" + filter.toPrettyString()).
                    addReason(Rule.of("", Rule.failure(Constraint.CORRECT_STRUCTURE)));
        }
    }


    private Condition parseFilterWithExtendedStructure(Filter filter) {
        switch(filter.getType()) {
            case AND -> {
                return andFilter((AndFilter) filter);
            }
            case MIN_TAGS -> {
                return minTagsFilter((MinTagsFilter) filter);
            }
            case CATEGORY -> {
                return categoryFilter((AnyFilter) filter);
            }
            case SHOPS -> {
                return shopFilter((AnyFilter) filter);
            }
            case GRADES -> {
                return gradeFilter((AnyFilter) filter);
            }
            case MANUFACTURER -> {
                return manufacturerFilter((AnyFilter) filter);
            }
            case OR -> {
                return orFilter((OrFilter) filter);
            }
            case USER -> {
                return userFilter((UserFilter) filter);
            }
            case MIN_QUANTITY -> {
                return quantityFilter((QuantityFilter) filter);
            }
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }
    }

    private Condition orFilter(OrFilter filter) {
        Condition condition = parseFilterWithExtendedStructure(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.or(parseFilterWithExtendedStructure(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = parseFilterWithExtendedStructure(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(parseFilterWithExtendedStructure(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("Products.productId").in(
                select(field("ProductTags.productId")).
                        from("ProductTags").
                        where(field("ProductTags.tagValue").in(
                                filter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("ProductTags.productId")).
                        having(count(field("ProductTags.productId")).eq(inline(filter.getTags().size())))
        );
    }

    private Condition categoryFilter(AnyFilter filter) {
        return field("category").in(
                filter.getValues().stream().map(DSL::inline).toList()
        );
    }

    private Condition shopFilter(AnyFilter filter) {
        return field("shop").in(
                filter.getValues().stream().map(DSL::inline).toList()
        );
    }

    private Condition gradeFilter(AnyFilter filter) {
        return field("grade").in(
                filter.getValues().stream().map(DSL::inline).toList()
        );
    }

    private Condition manufacturerFilter(AnyFilter filter) {
        return field("manufacturer").in(
                filter.getValues().stream().map(DSL::inline).toList()
        );
    }

    private Condition userFilter(UserFilter filter) {
        return field("userId").eq(inline(filter.getUserId()));
    }

    private Condition quantityFilter(QuantityFilter filter) {
        switch(filter.getRelative()) {
            case LESS -> {
                return field("quantity").lessThan(inline(filter.getQuantity()));
            }
            case LESS_OR_EQUAL -> {
                return field("quantity").lessOrEqual(inline(filter.getQuantity()));
            }
            case GREATER -> {
                return field("quantity").greaterThan(inline(filter.getQuantity()));
            }
            case GREATER_OR_EQUAL -> {
                return field("quantity").greaterOrEqual(inline(filter.getQuantity()));
            }
            case EQUAL -> {
                return field("quantity").eq(inline(filter.getQuantity()));
            }
            default -> throw new UnsupportedOperationException("Unknown relative = " + filter.getRelative());
        }
    }


    private boolean hasFilterCorrectStructure(Filter filter) {
        return filter.typeIs(Filter.Type.USER) ||
                filter.matchingTypesNumber(Filter.Type.USER, Filter.Type.AND) == 2 &&
                filter.bfs().
                        map(IterableFilter::filter).
                        filter(f -> f.typeIs(Filter.Type.AND)).
                        allMatch(f -> f.matchingTypesNumber(Filter.Type.USER) == 1);
    }

}

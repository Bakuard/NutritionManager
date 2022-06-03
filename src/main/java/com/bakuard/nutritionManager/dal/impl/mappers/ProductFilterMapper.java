package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.filters.*;

import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.List;

import static org.jooq.impl.DSL.*;

public class ProductFilterMapper {

    public ProductFilterMapper() {

    }

    public Condition toCondition(Filter filter) {
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
            case OR_ELSE -> {
                return orElseFilter((OrElseFilter) filter);
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

    public List<Condition> toConditions(Filter filter) {
        switch(filter.getType()) {
            case AND -> {
                return List.of(andFilter((AndFilter) filter));
            }
            case MIN_TAGS -> {
                return List.of(minTagsFilter((MinTagsFilter) filter));
            }
            case CATEGORY -> {
                return List.of(categoryFilter((AnyFilter) filter));
            }
            case SHOPS -> {
                return List.of(shopFilter((AnyFilter) filter));
            }
            case GRADES -> {
                return List.of(gradeFilter((AnyFilter) filter));
            }
            case MANUFACTURER -> {
                return List.of(manufacturerFilter((AnyFilter) filter));
            }
            case OR_ELSE -> {
                OrElseFilter orElse = (OrElseFilter) filter;
                return orElse.getOperands().stream().
                        map(this::toCondition).
                        toList();
            }
            case USER -> {
                return List.of(userFilter((UserFilter) filter));
            }
            case MIN_QUANTITY -> {
                return List.of(quantityFilter((QuantityFilter) filter));
            }
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }
    }


    private Condition orElseFilter(OrElseFilter filter) {
        Condition condition = toCondition(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.or(toCondition(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = toCondition(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(toCondition(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("productId").in(
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

}

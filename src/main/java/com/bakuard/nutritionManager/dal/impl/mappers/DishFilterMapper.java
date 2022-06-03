package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.filters.*;
import org.jooq.Condition;
import org.jooq.Param;
import org.jooq.impl.DSL;

import java.util.List;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.array;

public class DishFilterMapper {

    public DishFilterMapper() {

    }

    public Condition toFilter(Filter filter) {
        switch(filter.getType()) {
            case AND -> {
                return andFilter((AndFilter) filter);
            }
            case MIN_TAGS -> {
                return minTagsFilter((MinTagsFilter) filter);
            }
            case INGREDIENTS -> {
                return ingredientsFilter((AnyFilter) filter);
            }
            case USER -> {
                return userFilter((UserFilter) filter);
            }
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = toFilter(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(toFilter(filter.getOperands().get(i)));
        }
        return condition;
    }

    private Condition userFilter(UserFilter filter) {
        return field("userId").eq(inline(filter.getUserId()));
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("dishId").in(
                select(field("DishTags.dishId")).
                        from("DishTags").
                        where(field("DishTags.tagValue").in(
                                filter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("DishTags.dishId")).
                        having(count(field("DishTags.dishId")).eq(inline(filter.getTags().size())))
        );
    }

    private Condition ingredientsFilter(AnyFilter filter) {
        List<Param<String>> arrayData = filter.getValues().stream().
                map(DSL::inline).
                toList();

        return field("dishId").in(
                select(field("DishIngredients.dishId")).
                        from(table("DishIngredients")).
                        where(
                                "existProductsForFilter(?, DishIngredients.filterQuery)",
                                array(arrayData)
                        )
        );
    }

}

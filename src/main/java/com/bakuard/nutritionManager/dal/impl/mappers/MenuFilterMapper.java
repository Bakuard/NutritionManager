package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.filters.*;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;

public class MenuFilterMapper {

    public MenuFilterMapper() {

    }

    public Condition toCondition(Filter filter) {
        Condition result = null;

        switch(filter.getType()) {
            case USER -> result = userFilter((UserFilter) filter);
            case DISHES -> result = dishesFilter((AnyFilter) filter);
            case MIN_TAGS -> result = minTagsFilter((MinTagsFilter) filter);
            case AND -> result = andFilter((AndFilter) filter);
            default -> throw new UnsupportedOperationException(
                    "Unsupported operation for " + filter.getType() + " constraint");
        }

        return result;
    }


    private Condition userFilter(UserFilter filter) {
        return field("userId").eq(inline(filter.getUserId()));
    }

    private Condition dishesFilter(AnyFilter filter) {
        return field("menuId").in(
                select(field("MenuItems.menuId")).
                        from(table("MenuItems")).
                        innerJoin(table("Dishes")).
                        on(field("MenuItems.dishId").eq(field("Dishes.dishId"))).
                        where(field("Dishes.name").in(
                                filter.getValues().stream().map(DSL::inline).toList()
                        ))
        );
    }

    private Condition minTagsFilter(MinTagsFilter filter) {
        return field("menuId").in(
                select(field("MenuTags.menuId")).
                        from("MenuTags").
                        where(field("MenuTags.tagValue").in(
                                filter.getTags().stream().map(t -> inline(t.getValue())).toList()
                        )).
                        groupBy(field("MenuTags.menuId")).
                        having(count(field("MenuTags.menuId")).eq(inline(filter.getTags().size())))
        );
    }

    private Condition andFilter(AndFilter filter) {
        Condition condition = toCondition(filter.getOperands().get(0));
        for(int i = 1; i < filter.getOperands().size(); i++) {
            condition = condition.and(toCondition(filter.getOperands().get(i)));
        }
        return condition;
    }

}

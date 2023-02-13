package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.filters.*;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.UUID;
import java.util.stream.Stream;

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

    public String toCondition2(Filter filter) {
        if(!filter.isDnf()) filter = filter.toDnf();

        String result = null;

        if(isFilterFormPure(filter)) {
            String sqlCondition = parsePureFilterToSql(filter).indent(4);
            result = """
                get_bit(
                %s,
                    Products.integerIndex
                ) = 1
                AND
                Product.userId = %s
                """.
                    formatted(
                            sqlCondition.substring(0, sqlCondition.length() - 1),
                            inline(filter.<UserFilter>findAny(Filter.Type.USER).orElseThrow().getUserId())
                    );
        } else if(filter.typeIs(Filter.Type.USER)) {
            return userFilter2(filter);
        }

        return result;
    }


    private String parsePureFilterToSql(Filter filter) {
        String result = null;

        switch(filter.getType()) {
            case OR -> {
                String operands = filter.getOperands().stream().
                        filter(f -> !f.typeIs(Filter.Type.USER)).
                        map(this::parsePureFilterToSql).
                        map(sql -> '(' + sql + ')').
                        reduce((a, b) -> a + "\nUNION ALL\n" + b).
                        orElseThrow().
                        indent(4);
                result = """
                        select bit_or(
                        %s
                        )""".
                        formatted(operands.substring(0, operands.length() - 1));
            }
            case AND -> {
                String operands = filter.getOperands().stream().
                        filter(f -> !f.typeIs(Filter.Type.USER)).
                        map(this::parsePureFilterToSql).
                        map(sql -> '(' + sql + ')').
                        reduce((a, b) -> a + "\nUNION ALL\n" + b).
                        orElseThrow().
                        indent(4);
                result = """
                        select bit_and(
                        %s
                        )""".
                        formatted(operands.substring(0, operands.length() - 1));
            }
            case MIN_TAGS -> {
                result = fieldFilter(
                        filter.<UserFilter>findFirstSibling(Filter.Type.USER).orElseThrow().getUserId(),
                        ((MinTagsFilter)filter).getTags().stream().map(Tag::getValue),
                        "tag"
                );
            }
            case CATEGORY -> {
                result = fieldFilter(
                        filter.<UserFilter>findFirstSibling(Filter.Type.USER).orElseThrow().getUserId(),
                        ((AnyFilter)filter).getValues().stream(),
                        "category"
                );
            }
            case SHOPS -> {
                result = fieldFilter(
                        filter.<UserFilter>findFirstSibling(Filter.Type.USER).orElseThrow().getUserId(),
                        ((AnyFilter)filter).getValues().stream(),
                        "shop"
                );
            }
            case GRADES -> {
                result = fieldFilter(
                        filter.<UserFilter>findFirstSibling(Filter.Type.USER).orElseThrow().getUserId(),
                        ((AnyFilter)filter).getValues().stream(),
                        "grade"
                );
            }
            case MANUFACTURER -> {
                result = fieldFilter(
                        filter.<UserFilter>findFirstSibling(Filter.Type.USER).orElseThrow().getUserId(),
                        ((AnyFilter)filter).getValues().stream(),
                        "manufacturer"
                );
            }
        }

        return result;
    }

    private Condition orFilter(OrFilter filter) {
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

    private String fieldFilter(UUID userId, Stream<String> arrayValues, String filterType) {
        return query("""
                select bit_or(ProductFiltering.productIndexes)
                    from ProductFiltering
                    where ProductFiltering.userId = {0}
                          and ProductFiltering.filterValue in ({1})
                          and ProductFiltering.filterType = {2}""",
                inline(userId),
                list(arrayValues.map(DSL::field).map(DSL::inline).toList()),
                inline(filterType)
        ).getSQL();
    }

    private String userFilter2(Filter filter) {
        UserFilter userFilter = (UserFilter) filter;
        return query("Products.userId = {0}", inline(userFilter.getUserId())).getSQL();
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


    private boolean isFilterFormPure(Filter filter) {
        return filter.containsMin(Filter.Type.USER, Filter.Type.AND) &&
                filter.bfs().
                        map(IterableFilter::filter).
                        filter(f -> f.typeIs(Filter.Type.AND)).
                        allMatch(f -> f.containsMin(Filter.Type.USER)) &&
                filter.containsMax(Filter.Type.USER,
                        Filter.Type.MANUFACTURER,
                        Filter.Type.CATEGORY,
                        Filter.Type.SHOPS,
                        Filter.Type.GRADES,
                        Filter.Type.MIN_TAGS);
    }

}

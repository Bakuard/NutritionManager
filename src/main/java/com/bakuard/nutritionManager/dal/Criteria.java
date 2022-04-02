package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

import java.util.Objects;
import java.util.Optional;

public class Criteria {

    private Pageable pageable;
    private Sort sort;
    private Filter filter;

    public Criteria() {

    }

    public Criteria setPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }

    public Criteria setSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public Criteria setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public Sort getSort() {
        return sort;
    }

    public Filter getFilter() {
        return filter;
    }

    public Pageable tryGetPageable() {
        ValidateException.check(
                Rule.of("Criteria.pageable").notNull(pageable)
        );

        return pageable;
    }

    public Sort tryGetSort() {
        ValidateException.check(
                Rule.of("Criteria.sort").notNull(sort)
        );

        return sort;
    }

    public Filter tryGetFilter() {
        ValidateException.check(
                Rule.of("Criteria.filter").notNull(filter)
        );

        return filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criteria criteria = (Criteria) o;
        return Objects.equals(pageable, criteria.pageable) &&
                Objects.equals(sort, criteria.sort) &&
                Objects.equals(filter, criteria.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageable, sort, filter);
    }

    @Override
    public String toString() {
        return "Criteria{" +
                "pageable=" + pageable +
                ", sort=" + sort +
                ", filter=" + filter +
                '}';
    }

}

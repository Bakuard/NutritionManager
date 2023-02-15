package com.bakuard.nutritionManager.model.filters;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.stream.Collectors;

public abstract class AbstractFilter implements Filter {

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    @Override
    public int matchingTypesNumber(Type... types) {
        Type[] allTypes = Type.values();
        matchAll(allTypes);

        int count = 0;
        for(int i = 0; i < types.length; i++) {
            if(allTypes[types[i].ordinal()] == null) ++count;
        }

        return count;
    }

    @Override
    public int typesNumber() {
        Type[] allTypes = Type.values();
        matchAll(allTypes);

        int count = 0;
        for(int i = 0; i < allTypes.length; i++) {
            if(allTypes[i] == null) ++count;
        }

        return count;
    }

    @Override
    public int getDepth() {
        return calculateDepth(0);
    }

    @Override
    public Filter toDnf() {
        return ((AbstractFilter)openBrackets()).toDnfRecursive().openBrackets();
    }


    protected void matchAll(Type[] types) {
        int index = 0;
        while(index < types.length && types[index] != getType()) ++index;
        if(index < types.length) types[index] = null;

        for(int i = 0; i < getOperands().size(); i++) {
            ((AbstractFilter)getOperands().get(i)).matchAll(types);
        }
    }

    protected int calculateDepth(final int currentDepth) {
        int result = currentDepth;

        for(int i = 0; i < getOperands().size(); i++) {
            AbstractFilter filter = (AbstractFilter) getOperands().get(i);
            int childDepth = filter.calculateDepth(currentDepth + 1);
            result = Math.max(result, childDepth);
        }

        return result;
    }

    protected Filter toDnfRecursive() {
        Filter result = this;

        if(typeIs(Type.OR)) {
            result = Filter.or(getOperands().stream().map(Filter::toDnf).toList());
        } else if(typeIs(Type.AND)) {
            ArrayList<Filter> operands = getOperands().stream().
                    map(Filter::toDnf).
                    collect(Collectors.toCollection(ArrayList::new));

            int orFilterIndex = 0;
            while(orFilterIndex < operands.size() && !operands.get(orFilterIndex).typeIs(Type.OR)) ++orFilterIndex;

            if(orFilterIndex < operands.size()) {
                Filter orFilter = operands.remove(orFilterIndex);
                result = Filter.or(
                        orFilter.getOperands().stream().
                                map(f -> {
                                    ArrayList<Filter> listF = new ArrayList<>(operands);
                                    listF.add(f);
                                    return listF;
                                }).
                                map(Filter::and).
                                map(Filter::toDnf).
                                toList()
                );
            } else {
                result = Filter.and(operands);
            }
        }

        return result;
    }

}

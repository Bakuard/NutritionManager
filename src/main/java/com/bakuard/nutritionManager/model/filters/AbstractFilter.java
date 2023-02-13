package com.bakuard.nutritionManager.model.filters;

import com.google.common.collect.ImmutableList;

public abstract class AbstractFilter implements Filter {

    protected Filter parent;

    @Override
    public ImmutableList<Filter> getOperands() {
        return ImmutableList.of();
    }

    @Override
    public Filter getParent() {
        return parent;
    }

    @Override
    public boolean containsMax(Type... types) {
        return containsMax(types.length, types);
    }

    @Override
    public boolean containsMax(int maxMatch, Type... types) {
        return containsMax(maxMatch, 1000, types); //вместо 1000 можно использовать любое другое большое число.
    }

    @Override
    public boolean containsMax(int maxMatch, int maxDepth, Type... types) {
        Type[] allTypes = Type.values();
        matchAll(maxDepth, allTypes);

        int count = 0;
        for(int i = 0; i < types.length; i++) {
            if(allTypes[types[i].ordinal()] == null) ++count;
        }

        return count <= maxMatch;
    }

    @Override
    public boolean containsExactly(Type... types) {
        return containsExactly(types.length, types);
    }

    @Override
    public boolean containsExactly(int matchNumber, Type... types) {
        return containsExactly(matchNumber, 1000, types); //вместо 1000 можно использовать любое другое большое число.
    }

    @Override
    public boolean containsExactly(int matchNumber, int maxDepth, Type... types) {
        Type[] allTypes = Type.values();
        matchAll(maxDepth, allTypes);

        int count = 0;
        for(int i = 0; i < types.length; i++) {
            if(allTypes[types[i].ordinal()] == null) ++count;
        }

        return count == matchNumber;
    }

    @Override
    public boolean containsMin(Type... types) {
        return containsMin(types.length, types);
    }

    @Override
    public boolean containsMin(int minMatch, Type... types) {
        return containsMin(minMatch, 1000, types); //вместо 1000 можно использовать любое другое большое число.
    }

    @Override
    public boolean containsMin(int minMatch, int maxDepth, Type... types) {
        matchAll(maxDepth, types);

        int count = 0;
        for(int i = 0; i < types.length; i++) {
            if(types[i] == null) ++count;
        }

        return count >= minMatch;
    }

    @Override
    public int getDepth() {
        return calculateDepth(0);
    }


    protected void matchAll(int depth, Type[] types) {
        if(depth >= 0) {
            int index = 0;
            while(index < types.length && types[index] != getType()) ++index;
            if(index < types.length) types[index] = null;

            for(int i = 0; i < getOperands().size(); i++) {
                ((AbstractFilter)getOperands().get(i)).matchAll(depth - 1, types);
            }
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

}

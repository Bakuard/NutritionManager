package com.bakuard.nutritionManager.model.filters;

public abstract class AbstractFilter implements Filter {

    @Override
    public boolean containsOnly(Type... types) {
        boolean match = findMatch(types) < types.length;

        for(int i = 0; i < getOperands().size() && match; i++) {
            match = getOperands().get(i).containsOnly(types);
        }

        return match;
    }

    @Override
    public boolean containsAtLeast(Type... types) {
        boolean[] matchTypes = new boolean[types.length];
        matchAll(matchTypes, types);

        int i = 0;
        while(i < matchTypes.length && matchTypes[i]) ++i;
        return i == matchTypes.length;
    }

    protected int findMatch(Type... types) {
        int i = 0;
        while(i < types.length && getType() != types[i]) ++i;
        return i;
    }

    protected void matchAll(boolean[] matchTypes, Type[] types) {
        int index = findMatch(types);
        if(index < types.length) matchTypes[index] = true;

        for(int i = 0; i < getOperands().size(); i++) {
            ((AbstractFilter)getOperands().get(i)).matchAll(matchTypes, types);
        }
    }

    @Override
    public <T extends Filter> T findAny(Type type) {
        Filter result = type == getType() ? this : null;

        for(int i = 0; i < getOperands().size() && result == null; i++) {
            result = getOperands().get(i).findAny(type);
        }

        return (T)result;
    }

}

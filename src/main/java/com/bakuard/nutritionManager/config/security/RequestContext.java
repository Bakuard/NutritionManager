package com.bakuard.nutritionManager.config.security;

public interface RequestContext {

    public <T> T getCurrentJwsBodyAs(Class<T> jwsBodyType);

    public String getJws();

}

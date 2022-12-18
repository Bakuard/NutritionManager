package com.bakuard.nutritionManager.config.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class RequestContextImpl implements RequestContext {

    @Override
    public <T> T getCurrentJwsBodyAs(Class<T> jwsBodyType) {
        JwsAuthentication jwsAuthentication = (JwsAuthentication) SecurityContextHolder.
                getContext().
                getAuthentication();
        return jwsBodyType.cast(jwsAuthentication.getJwsBody());
    }

    @Override
    public String getJws() {
        JwsAuthentication jwsAuthentication = (JwsAuthentication) SecurityContextHolder.
                getContext().
                getAuthentication();
        return jwsAuthentication.getJws();
    }

}

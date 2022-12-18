package com.bakuard.nutritionManager.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwsAuthentication extends AbstractAuthenticationToken {

    private final String jws;
    private Object jwsBody;
    private String path;

    public JwsAuthentication(String jws, String path) {
        super(null);
        this.jws = jws;
        this.path = path;
    }

    public JwsAuthentication(String jws, String path, Object jwsBody) {
        super(null);
        this.jws = jws;
        this.path = path;
        this.jwsBody = jwsBody;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getJws() {
        return jws;
    }

    public Object getJwsBody() {
        return jwsBody;
    }

    public String getPath() {
        return path;
    }

}

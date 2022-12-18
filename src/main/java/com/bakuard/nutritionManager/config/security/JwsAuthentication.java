package com.bakuard.nutritionManager.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwsAuthentication extends AbstractAuthenticationToken {

    private final String jws;
    private Object jwsBody;

    public JwsAuthentication(Collection<? extends GrantedAuthority> authorities, String jws) {
        super(authorities);
        this.jws = jws;
    }

    public JwsAuthentication(String jws) {
        super(null);
        this.jws = jws;
    }

    public JwsAuthentication(String jws, Object jwsBody) {
        super(null);
        this.jws = jws;
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


}

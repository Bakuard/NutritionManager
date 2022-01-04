package com.bakuard.nutritionManager.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwsAuthentication extends AbstractAuthenticationToken {

    private final String jws;

    public JwsAuthentication(Collection<? extends GrantedAuthority> authorities, String jws) {
        super(authorities);
        this.jws = jws;
    }

    public JwsAuthentication(String jws) {
        super(null);
        this.jws = jws;
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

}

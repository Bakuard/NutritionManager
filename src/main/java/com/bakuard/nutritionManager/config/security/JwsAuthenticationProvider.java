package com.bakuard.nutritionManager.config.security;

import com.bakuard.nutritionManager.service.JwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JwsAuthenticationProvider implements AuthenticationProvider {

    private final JwsService jwsService;

    @Autowired
    public JwsAuthenticationProvider(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwsAuthentication request = (JwsAuthentication) authentication;

        String jws = request.getJws();

        try {
            Object jwsBody = null;
            switch(request.getPath()) {
                case "/auth/registration" -> jwsBody = jwsService.parseJws(jws, "registration");
                case "/auth/changeCredential" -> jwsBody = jwsService.parseJws(jws, "restorePassword");
                default -> jwsBody = jwsService.parseJws(jws, "commonToken");
            }

            JwsAuthentication response = new JwsAuthentication(jws, request.getPath(), jwsBody);
            response.setAuthenticated(true);
            return response;
        } catch(Exception e) {
            throw new BadCredentialsException("Incorrect JWS -> " + jws, e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwsAuthentication.class.isAssignableFrom(authentication);
    }

}

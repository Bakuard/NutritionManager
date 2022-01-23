package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.model.exceptions.ServiceException;
import com.bakuard.nutritionManager.services.JwsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class JwsAuthenticationProvider implements AuthenticationProvider {

    private JwsService jwsService;

    @Autowired
    public JwsAuthenticationProvider(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwsAuthentication request = (JwsAuthentication) authentication;

        String jws = request.getJws();

        try {
            jwsService.parseAccessJws(jws);

            JwsAuthentication response = new JwsAuthentication(jws);
            response.setAuthenticated(true);
            return response;
        } catch(ServiceException e) {
            throw new BadCredentialsException("Incorrect jws", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwsAuthentication.class.isAssignableFrom(authentication);
    }

}

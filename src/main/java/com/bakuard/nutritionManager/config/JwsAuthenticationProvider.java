package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.services.JwsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.UUID;

public class JwsAuthenticationProvider implements AuthenticationProvider {

    private static final ThreadLocal<UUID> usersId = new ThreadLocal<>();

    public static UUID getAndClearUserId() {
        UUID userId = usersId.get();
        usersId.remove();
        return userId;
    }


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
            UUID userId = jwsService.parseAccessJws(jws);
            usersId.set(userId);

            JwsAuthentication response = new JwsAuthentication(jws);
            response.setAuthenticated(true);
            return response;
        } catch(ValidateException e) {
            throw new BadCredentialsException("Incorrect jws", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwsAuthentication.class.isAssignableFrom(authentication);
    }

}

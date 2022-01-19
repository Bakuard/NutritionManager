package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.services.JwsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private JwsService jwsService;

    @Autowired
    public SecurityConfig(JwsService jwsService) {
        this.jwsService = jwsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new JwsAuthenticationProvider(jwsService));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                    exceptionHandling().authenticationEntryPoint((request, response, ex) ->
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                ex.getMessage()
                        )
                    ).
                and().
                    authorizeRequests().
                        antMatchers(
                                "/products/**",
                                "/dishes/**",
                                "/menus/**",
                                "/auth/getUserByJws"
                        ).authenticated().
                and().
                    addFilterBefore(new JwsFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}

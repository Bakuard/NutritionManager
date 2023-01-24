package com.bakuard.nutritionManager.config;

import com.bakuard.nutritionManager.config.security.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.config.security.JwsFilter;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.service.JwsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private DtoMapper mapper;
    private ObjectMapper jsonWriter;

    @Autowired
    public SecurityConfig(JwsService jwsService, DtoMapper mapper) {
        this.jwsService = jwsService;
        this.mapper = mapper;
        jsonWriter = new ObjectMapper();
        jsonWriter.registerModule(new JavaTimeModule());
        jsonWriter.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
                    exceptionHandling().authenticationEntryPoint((request, response, ex) -> {
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                        jsonWriter.writeValue(
                                response.getOutputStream(),
                                mapper.toExceptionResponse(HttpStatus.UNAUTHORIZED, "unauthorized")
                        );
                    }).
                and().
                    authorizeRequests().
                        antMatchers(
                                "/auth/enter",
                                "/auth/verifyEmailForRegistration",
                                "/auth/verifyEmailForChangeCredentials",
                                "/api",
                                "/apiStandardFormat/**",
                                "/swagger-ui/**",
                                "/actuator/health"
                        ).permitAll().
                        anyRequest().authenticated().
                and().
                    addFilterBefore(new JwsFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}

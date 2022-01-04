package com.bakuard.nutritionManager.services;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.IncorrectJwsException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class JwsService {

    private enum Aim {
        ACCESS,
        REGISTRATION,
        CHANGE_CREDENTIALS
    }

    
    private final KeyPair accessKeyPair;
    private final KeyPair registrationKeyPair;
    private final KeyPair changeCredentialsKeyPair;
    private final long lifeTimeInHours;
    private final long lifeTimeInMinutes;

    public JwsService() {
        accessKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        registrationKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        changeCredentialsKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        lifeTimeInHours = 5;
        lifeTimeInMinutes = 3;
    }

    public String generateAccessJws(User user) {
        LocalDateTime expiration = LocalDateTime.now().plusHours(lifeTimeInHours);

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                setSubject(user.getId().toString()).
                claim("aim", Aim.ACCESS.toString()).
                signWith(accessKeyPair.getPrivate()).
                compact();
    }

    public UUID parseAccessJws(String jws) throws IncorrectJwsException {
        jws = jws.replaceFirst("Bearer ", "");

        try {
            Claims claims = Jwts.parserBuilder().
                    setSigningKey(accessKeyPair.getPublic()).
                    build().
                    parseClaimsJws(jws).
                    getBody();

            return UUID.fromString(claims.getSubject());
        } catch(JwtException e) {
            throw new IncorrectJwsException("Fail to parse access JWS", e);
        }
    }

    public String generateRegistrationJws(String email) {
        LocalDateTime expiration = LocalDateTime.now().plusHours(lifeTimeInMinutes);

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                claim("email", email).
                claim("aim", Aim.REGISTRATION.toString()).
                signWith(registrationKeyPair.getPrivate()).
                compact();
    }

    public String parseRegistrationJws(String jws) throws IncorrectJwsException {
        jws = jws.replaceFirst("Bearer ", "");

        try {
            Claims claims = Jwts.parserBuilder().
                    setSigningKey(registrationKeyPair.getPublic()).
                    build().
                    parseClaimsJws(jws).
                    getBody();

            return claims.get("email", String.class);
        } catch(JwtException e) {
            throw new IncorrectJwsException("Fail to parse registration JWS", e);
        }
    }

    public String generateChangeCredentialsJws(String email) {
        LocalDateTime expiration = LocalDateTime.now().plusHours(lifeTimeInMinutes);

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                claim("email", email).
                claim("aim", Aim.CHANGE_CREDENTIALS.toString()).
                signWith(changeCredentialsKeyPair.getPrivate()).
                compact();
    }

    public String parseChangeCredentialsJws(String jws) throws IncorrectJwsException {
        jws = jws.replaceFirst("Bearer ", "");

        try {
            Claims claims = Jwts.parserBuilder().
                    setSigningKey(changeCredentialsKeyPair.getPublic()).
                    build().
                    parseClaimsJws(jws).
                    getBody();

            return claims.get("email", String.class);
        } catch(JwtException e) {
            throw new IncorrectJwsException("Fail to parse change credentials JWS", e);
        }
    }

}

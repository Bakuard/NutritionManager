package com.bakuard.nutritionManager.services;

import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class JwsService {

    private static Logger logger = LoggerFactory.getLogger(JwsService.class);


    private enum Aim {
        ACCESS,
        REGISTRATION,
        CHANGE_CREDENTIALS
    }


    private final JwsBlackListRepository blackList;
    private final KeyPair accessKeyPair;
    private final KeyPair registrationKeyPair;
    private final KeyPair changeCredentialsKeyPair;
    private final long lifeTimeInDays;
    private final long lifeTimeInMinutes;

    public JwsService(JwsBlackListRepository blackList) {
        this.blackList = blackList;
        accessKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        registrationKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        changeCredentialsKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        lifeTimeInDays = 7;
        lifeTimeInMinutes = 3;
    }

    public String generateAccessJws(User user) {
        LocalDateTime expiration = LocalDateTime.now().plusDays(lifeTimeInDays);

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant())).
                setSubject(user.getId().toString()).
                setId(UUID.randomUUID().toString()).
                claim("aim", Aim.ACCESS.toString()).
                signWith(accessKeyPair.getPrivate()).
                compact();
    }

    public UUID parseAccessJws(String jws) throws ValidateException {
        Claims claims = null;
        try {
            claims = parseJws(jws, accessKeyPair);
        } catch(JwtException e) {
            throw new ValidateException(
                    "Incorrect access jws",
                    e,
                    getClass(),
                    "parseAccessJws"
            ).addReason("jws", ConstraintType.INCORRECT_JWS);
        }

        UUID accessJwsId = UUID.fromString(claims.getId());
        if(blackList.inBlackList(accessJwsId)) {
            throw new ValidateException(
                    "Incorrect access jws",
                    getClass(),
                    "parseAccessJws"
            ).addReason("jws", ConstraintType.INCORRECT_JWS);
        }

        return UUID.fromString(claims.getSubject());
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

    public String parseRegistrationJws(String jws) throws ValidateException {
        try {
            Claims claims = parseJws(jws, registrationKeyPair);

            return claims.get("email", String.class);
        } catch(JwtException e) {
            throw new ValidateException(
                    "Incorrect registration jws",
                    getClass(),
                    "parseRegistrationJws"
            ).addReason("jws", ConstraintType.INCORRECT_JWS);
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

    public String parseChangeCredentialsJws(String jws) throws ValidateException {
        try {
            Claims claims = parseJws(jws, changeCredentialsKeyPair);

            return claims.get("email", String.class);
        } catch(JwtException e) {
            throw new ValidateException(
                    "Incorrect change credential jws",
                    getClass(),
                    "parseChangeCredentialsJws"
            ).addReason("jws", ConstraintType.INCORRECT_JWS);
        }
    }

    public void invalidateJws(String accessJws) {
        Claims claims = parseJws(accessJws, accessKeyPair);

        UUID accessJwsId = UUID.fromString(claims.getId());
        LocalDateTime expiration = Instant.
                ofEpochMilli(claims.getExpiration().getTime()).
                atZone(ZoneId.systemDefault()).
                toLocalDateTime();

        blackList.addToBlackList(accessJwsId, expiration);
    }

    @Transactional
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void clearJwsBlackList() {
        logger.info("Clear jws black list");

        blackList.removeAllExpired(LocalDateTime.now());
    }


    private Claims parseJws(String jws, KeyPair keyPair) {
        jws = jws.replaceFirst("Bearer ", "");

        return Jwts.parserBuilder().
                setSigningKey(keyPair.getPublic()).
                build().
                parseClaimsJws(jws).
                getBody();
    }

}

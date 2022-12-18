package com.bakuard.nutritionManager.service;

import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.bakuard.nutritionManager.validation.Rule.failure;

public class JwsService {

    private static final Logger logger = LoggerFactory.getLogger(JwsService.class);


    private final JwsBlackListRepository blackList;
    private ObjectMapper objectMapper;
    private Map<String, KeyPair> keyPairs;
    private Clock clock;

    /**
     * Создает новый сервис генерации и парсинг JWS токенов.
     * @param blackList хранилище отозванных токенов, срок действия которых ещё не истек.
     * @param clock часы используемые для получения текущей даты (параметр добавлен для удобства тестирования)
     * @param objectMapper отвечает за сериализацию и десериализацию тела JWS токена
     */
    public JwsService(JwsBlackListRepository blackList,
                      Clock clock,
                      ObjectMapper objectMapper) {
        this.blackList = blackList;
        this.keyPairs = new ConcurrentHashMap<>();
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    /**
     * Генерирует и возвращает JWS токен для которого в качестве body будет взят объект jwsBody
     * сериализованный в JSON формат. Токен подписывается с применением ассиметричного шифрования, где
     * используется пара ключей с именем keyName. Если пары ключей с таким именем нет - она будет автоматически
     * сгенерирована.
     * @param jwsBody тело JWS токена
     * @param keyName имя пары ключей используемых для подписи токена
     * @return JWS токен
     * @throws NullPointerException если jwsBody или keyName имеют значение null.
     */
    public String generateJws(Object jwsBody, String keyName, Duration jwsLifeTime) {
        Objects.requireNonNull(jwsBody, "jwsBody can't be null");
        Objects.requireNonNull(keyName, "keyName can't be null");

        LocalDateTime expiration = LocalDateTime.now(clock).plus(jwsLifeTime);
        String json = tryCatch(() -> objectMapper.writeValueAsString(jwsBody), RuntimeException::new);
        KeyPair keyPair = keyPairs.computeIfAbsent(keyName, key -> Keys.keyPairFor(SignatureAlgorithm.RS512));

        return Jwts.builder().
                setExpiration(Date.from(expiration.atZone(clock.getZone()).toInstant())).
                setId(UUID.randomUUID().toString()).
                claim("body", json).
                claim("bodyType", jwsBody.getClass().getName()).
                claim("keyName", keyName).
                signWith(keyPair.getPrivate()).
                compact();
    }

    /**
     * Парсит переданный JWS токен и десериализует его тело в виде отдельного объекта с типом T.
     * @param jws токен
     * @param <T> тип объекта представляющего десериализованное тело токена
     * @return тело токена в виде отдельного объекта с типом T.
     * @throws NullPointerException если jws равен null.
     * @throws JwtException если выполняется хотя бы одна из следующих причин: <br/>
     *                      1. если указанный токен не соответствует формату JWT. <br/>
     *                      2. если срок действия токена истек. <br/>
     *                      3. если токен был изменен после его подписания. <br/>
     */
    public <T> T parseJws(String jws) {
        Objects.requireNonNull(jws, "jws can't be null");

        String keyPairName = parseKeyPairName(jws);
        KeyPair keyPair = keyPairs.get(keyPairName);

        Claims claims = parseJws(jws, keyPair);
        assertNotInBlackList(claims);
        return parseJwsBody(claims);
    }

    public void invalidateJws(String accessJws, String keyName) {
        Claims claims = parseJws(accessJws, keyPairs.get(keyName));

        UUID accessJwsId = UUID.fromString(claims.getId());
        LocalDateTime expiration = parseExpiration(claims);

        blackList.addToBlackList(accessJwsId, expiration);
    }

    @Transactional
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void clearJwsBlackList() {
        int removedNumber = blackList.removeAllExpired(LocalDateTime.now(clock));
        logger.info("Clear jws black list. Removed jws number = " + removedNumber);
    }


    private Claims parseJws(String jws, KeyPair keyPair) {
        if(jws.startsWith("Bearer ")) jws = jws.substring(7);

        return Jwts.parserBuilder().
                setSigningKey(keyPair.getPublic()).
                build().
                parseClaimsJws(jws).
                getBody();
    }

    private String parseKeyPairName(String jws) {
        final String preparedJws = jws.startsWith("Bearer ") ? jws.substring(7) : jws;

        String keyPairName =  tryCatch(() -> objectMapper.readTree(decodeJwsBody(preparedJws)),
                e -> new MalformedJwtException("Jws has incorrect format '" + jws + '\'')).
                findPath("keyName").
                textValue();
        if(keyPairName == null) throw new MalformedJwtException("Missing key name");
        if(!keyPairs.containsKey(keyPairName)) {
            throw new MalformedJwtException("Unknown key-pair with name '" + keyPairName + '\'');
        }
        return keyPairName;
    }

    private <T> T parseJwsBody(Claims claims) {
        String json = claims.get("body", String.class);
        Class<?> jwsBodyType = tryCatch(() -> Class.forName(claims.get("bodyType", String.class)), RuntimeException::new);
        return tryCatch(() -> objectMapper.readValue(json, (Class<T>) jwsBodyType), RuntimeException::new);
    }

    private LocalDateTime parseExpiration(Claims claims) {
        return Instant.
                ofEpochMilli(claims.getExpiration().getTime()).
                atZone(clock.getZone()).
                toLocalDateTime();
    }

    private String decodeJwsBody(String jws) {
        String[] data = jws.split("\\.");
        return new String(Base64.getUrlDecoder().decode(data[1]));
    }

    private void assertNotInBlackList(Claims claims) {
        UUID accessJwsId = UUID.fromString(claims.getId());
        if(blackList.inBlackList(accessJwsId)) {
            throw new ValidateException("Incorrect access jws").
                    addReason(Rule.of("JwsService.parseAccessJws", failure(Constraint.CORRECT_JWS)));
        }
    }

    private <T> T tryCatch(Callable<T> callable,
                           Function<Exception, ? extends RuntimeException> exceptionFabric) {
        try {
            return callable.call();
        } catch(Exception e) {
            throw exceptionFabric.apply(e);
        }
    }

}

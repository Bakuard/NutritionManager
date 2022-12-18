package com.bakuard.nutritionManager.service;

import com.bakuard.nutritionManager.TestConfig;
import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.MalformedJwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class JwsServiceTest {

    @Autowired
    private ConfigData conf;
    @Autowired
    private ObjectMapper objectMapper;
    private Clock clock = Clock.fixed(Instant.parse("2100-01-01T00:00:00Z"), ZoneId.of("Asia/Kolkata"));

    @Test
    @DisplayName("""
            generateJws(jwsBody, keyName):
             jwsBody is null
             => throw exception
            """)
    public void generateJws1() {
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.generateJws(null, "keyName", conf.jws().commonTokenLifeTime()));
    }

    @Test
    @DisplayName("""
            generateJws(jwsBody, keyName):
             keyName is null
             => throw exception
            """)
    public void generateJws2() {
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.generateJws("Some data", null, conf.jws().commonTokenLifeTime()));
    }

    @Test
    @DisplayName("""
            parseJws(jws):
             jws is null
             => throw exception
            """)
    public void parseJws1() {
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);

        Assertions.assertThatNullPointerException().
                isThrownBy(() -> jwsService.parseJws(null));
    }

    @Test
    @DisplayName("""
            generate and parse token:
             JWS body is correct
             => parser must return the same JWS body
            """)
    public void generateAndParse1() {
        UUID expected = toUUID(1);
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);

        String jws = jwsService.generateJws(expected, "keyName", conf.jws().commonTokenLifeTime());
        UUID actual = jwsService.parseJws(jws);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            generate and parse token:
             generate several JWS for different jws bodies,
             use unique key-pair for each JWS
             => parser must return correct JWS body for each JWS
            """)
    public void generateAndParse2() {
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);
        String jws1 = jwsService.generateJws("Some data A", "keyName1", conf.jws().commonTokenLifeTime());
        String jws2 = jwsService.generateJws("Some data B", "keyName2", conf.jws().commonTokenLifeTime());

        String actual1 = jwsService.parseJws(jws1);
        String actual2 = jwsService.parseJws(jws2);

        Assertions.assertThat(actual1).isEqualTo("Some data A");
        Assertions.assertThat(actual2).isEqualTo("Some data B");
    }

    @Test
    @DisplayName("""
            generate and parse token:
             keyName in JWS was changed after signing
             => throw exception
            """)
    public void generateAndParse3() throws JacksonException {
        JwsService jwsService = new JwsService(Mockito.mock(JwsBlackListRepository.class), clock, objectMapper);
        String jws = jwsService.generateJws(toUUID(1), "keyName1", conf.jws().commonTokenLifeTime());

        ObjectNode node = (ObjectNode) objectMapper.readTree(getRawJwsBody(jws));
        node.put("keyName", "otherKeyName");
        String changedJws = replaceJwsBody(jws, node.toPrettyString());

        Assertions.assertThatExceptionOfType(MalformedJwtException.class).
                isThrownBy(() -> jwsService.parseJws(changedJws));
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

    private String getRawJwsBody(String jws) {
        String jwsBodyInBase64 = jws.split("\\.")[1];
        byte[] jwsBodyAsByteArray = Base64.getUrlDecoder().decode(jwsBodyInBase64);
        return new String(jwsBodyAsByteArray);
    }

    private String replaceJwsBody(String jws, String newJwsBody) {
        String[] jwsParts = jws.split("\\.");
        byte[] jwsBodyAsByteArray = newJwsBody.getBytes(StandardCharsets.UTF_8);
        String jwsBodyInBase64 = new String(Base64.getUrlEncoder().encode(jwsBodyAsByteArray));
        return jwsParts[0] + '.' + jwsBodyInBase64 + '.' + jwsParts[2];
    }

}
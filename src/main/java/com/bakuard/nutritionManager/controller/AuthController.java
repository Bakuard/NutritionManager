package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.services.AuthService;
import com.bakuard.nutritionManager.dto.auth.CredentialsRequest;
import com.bakuard.nutritionManager.dto.auth.EmailRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Контроллер аутентификации",
        description = """
                Используется для регистрации новых пользователей,
                 аутентификации существующих и смены учетных данных
                """
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class.getName());


    private DtoMapper mapper;
    private AuthService authService;
    private ExceptionResolver exceptionResolver;

    @Autowired
    public AuthController(DtoMapper mapper, AuthService authService, ExceptionResolver exceptionResolver) {
        this.mapper = mapper;
        this.authService = authService;
        this.exceptionResolver = exceptionResolver;
    }

    @Operation(
            summary = "Аутентификация существующего пользователя",
            description = "Используется для входа существующего пользователя в систему"
    )
    @Transactional
    @PostMapping("/enter")
    public ResponseEntity<?> enter(@RequestBody CredentialsRequest dto) {
        logger.info("User " + dto.getUserName() + " try enter");
        try {
            String jws = authService.enter(dto.getUserName(), dto.getUserPassword());
            return ResponseEntity.ok(mapper.toJwsResponse(jws));
        } catch(RuntimeException e) {
            return exceptionResolver.commonHandle(e);
        }
    }

    @Operation(
            summary = "Верификация почты пользователя для регистрации",
            description = """
                    Первый из двух шагов для регистрации нового пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу регистрации -
                     добавление своих учетных данных.
                    """
    )
    @Transactional
    @PostMapping("/verifyEmailForRegistration")
    public ResponseEntity<?> verifyEmailForRegistration(@RequestBody EmailRequest dto) {
        logger.info("Verify email for registration");
        try {
            authService.verifyEmailForRegistration(dto.getEmail());
            return ResponseEntity.ok("the letter was sent to the mail for registration");
        } catch(RuntimeException e) {
            return exceptionResolver.commonHandle(e);
        }
    }

    @Operation(
            summary = "Верификация почты пользователя для смены учетных данных",
            description = """
                    Первый из двух шагов для смены учетных данных существующего пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу смены учетных данных -
                     указание своих новых учетных данных.
                    """
    )
    @Transactional
    @PostMapping("/verifyEmailForChangeCredentials")
    public ResponseEntity<?> verifyEmailForChangeCredentials(@RequestBody EmailRequest dto) {
        logger.info("Verify email for change credentials");
        try {
            authService.verifyEmailForChangeCredentials(dto.getEmail());
            return ResponseEntity.ok("the letter was sent to the mail for change credentials");
        } catch(RuntimeException e) {
            return exceptionResolver.commonHandle(e);
        }
    }

    @Operation(
            summary = "Добавление учетных данных пользователя при регистрации",
            description = """
                    Второй и завершающий шаг регистрации нового пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """
    )
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody CredentialsRequest dto,
                                          @RequestHeader("Authorization") String registrationJws) {
        logger.info("registration new user = " + dto.getUserName() +
                ". registrationJws=" + registrationJws);
        try {
            String accessJws = authService.registration(registrationJws, dto.getUserName(), dto.getUserPassword());
            return ResponseEntity.ok(mapper.toJwsResponse(accessJws));
        } catch(RuntimeException e) {
            return exceptionResolver.commonHandle(e);
        }
    }

    @Operation(
            summary = "Указание новых учетных данных пользователя",
            description = """
                    Второй и завершающий шаг смены учетных данных пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """
    )
    @Transactional
    @PostMapping("/changeCredential")
    public ResponseEntity<?> changeCredential(@RequestBody CredentialsRequest dto,
                                              @RequestHeader("Authorization") String changeCredentialsJws) {
        logger.info("change credentials for user with new name = " + dto.getUserName() +
                ". changeCredentialsJws=" + changeCredentialsJws);
        try {
            String accessJws = authService.changeCredential(changeCredentialsJws, dto.getUserName(), dto.getUserPassword());
            return ResponseEntity.ok(mapper.toJwsResponse(accessJws));
        } catch(RuntimeException e) {
            return exceptionResolver.commonHandle(e);
        }
    }

}

package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.auth.*;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Autowired
    public AuthController(DtoMapper mapper, AuthService authService) {
        this.mapper = mapper;
        this.authService = authService;
    }

    @Operation(
            summary = "Аутентификация существующего пользователя",
            description = "Используется для входа существующего пользователя в систему",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если пользователь не найден или указан неверный пароль",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/enter")
    public ResponseEntity<SuccessResponse<JwsResponse>> enter(@RequestBody CredentialsForEnterRequest dto) {
        logger.info("User " + dto.getUserName() + " try enter");

        Pair<String, User> jws = authService.enter(dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(jws.getFirst(), jws.getSecond());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.enter", response));
    }

    @Operation(
            summary = "Верификация почты пользователя для регистрации",
            description = """
                    Первый из двух шагов для регистрации нового пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу регистрации -
                     добавление своих учетных данных.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если не удалось отправить письмо на почту",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/verifyEmailForRegistration")
    public ResponseEntity<SuccessResponse<?>> verifyEmailForRegistration(@RequestBody EmailRequest dto) {
        logger.info("Verify email for registration");

        authService.verifyEmailForRegistration(dto.getEmail());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.verifyEmailForRegistration", null));
    }

    @Operation(
            summary = "Верификация почты пользователя для смены учетных данных",
            description = """
                    Первый из двух шагов для смены учетных данных существующего пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу смены учетных данных -
                     указание своих новых учетных данных.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если не удалось отправить письмо на почту",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/verifyEmailForChangeCredentials")
    public ResponseEntity<SuccessResponse<?>> verifyEmailForChangeCredentials(@RequestBody EmailRequest dto) {
        logger.info("Verify email for change credentials");

        authService.verifyEmailForChangeCredentials(dto.getEmail());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.verifyEmailForChangeCredentials", null));
    }

    @Operation(
            summary = "Добавление учетных данных пользователя при регистрации",
            description = """
                    Второй и завершающий шаг регистрации нового пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<SuccessResponse<JwsResponse>> registration(@RequestBody CredentialsForEnterRequest dto,
                                                                     @RequestHeader("Authorization") String registrationJws) {
        logger.info("registration new user = " + dto.getUserName() + ". registrationJws=" + registrationJws);

        Pair<String, User> accessJws = authService.registration(registrationJws, dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(accessJws.getFirst(), accessJws.getSecond());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.registration", response));
    }

    @Operation(
            summary = "Указание новых учетных данных пользователя",
            description = """
                    Второй и завершающий шаг смены учетных данных пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/changeCredential")
    public ResponseEntity<SuccessResponse<JwsResponse>> changeCredential(@RequestBody CredentialsForEnterRequest dto,
                                                                         @RequestHeader("Authorization") String changeCredentialsJws) {
        logger.info("change credentials for user with new name = " + dto.getUserName() +
                ". changeCredentialsJws=" + changeCredentialsJws);

        Pair<String, User> accessJws = authService.changeCredential(changeCredentialsJws, dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(accessJws.getFirst(), accessJws.getSecond());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changeCredential", response));
    }

    @Operation(
            summary = "Смена логина и почты пользователя из его личного кабинета.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/changeLoginAndEmail")
    public ResponseEntity<SuccessResponse<UserResponse>> changeLoginAndEmail(
            @RequestBody ChangeLoginAndEmailRequest dto,
            @RequestHeader("Authorization") String accessJws) {
        logger.info("Change login and email from personal area. Dto = " + dto);

        User user = authService.changeLoginAndEmail(
                accessJws,
                dto.getUserName(),
                dto.getUserEmail(),
                dto.getUserPassword()
        );

        UserResponse response = mapper.toUserResponse(user);
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changeLoginAndEmail", response));
    }

    @Operation(
            summary = "Смена пароля пользователя из его личного кабинета.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "Если передан неверный токен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/changePassword")
    public ResponseEntity<SuccessResponse<UserResponse>> changePassword(@RequestBody ChangePasswordRequest dto,
                                                                        @RequestHeader("Authorization") String accessJws) {
        logger.info("Change password from personal area. Dto = " + dto);

        User user = authService.changePassword(
                accessJws,
                dto.getUserCurrentPassword(),
                dto.getUserNewPassword()
        );

        UserResponse response = mapper.toUserResponse(user);
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changePassword", response));
    }

    @Operation(
            summary = "Возвращает пользователя в соответствии с токеном доступа",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getUserByJws")
    public ResponseEntity<UserResponse> getUserByJws(@RequestHeader("Authorization") String accessJws) {
        logger.info("Get user by accessJws");

        User user = authService.getUserByJws(accessJws);
        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @Operation(
            summary = "Выполняет logout для пользователя определяемого по токену доступа",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<?>> logout(@RequestHeader("Authorization") String accessJws) {
        logger.info("Logout user by accessJws");

        authService.logout(accessJws);
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.logout", null));
    }

}

package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.security.RequestContext;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.auth.*;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    private RequestContext requestContext;
    private UserRepository userRepository;

    @Autowired
    public AuthController(DtoMapper mapper,
                          AuthService authService,
                          RequestContext requestContext,
                          UserRepository userRepository) {
        this.mapper = mapper;
        this.authService = authService;
        this.requestContext = requestContext;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Аутентификация существующего пользователя",
            description = "Используется для входа существующего пользователя в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если пользователь не найден или указан неверный пароль",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/enter")
    @Transactional
    public ResponseEntity<SuccessResponse<JwsResponse>> enter(@RequestBody CredentialsForEnterRequest dto) {
        logger.info("User '{}' try enter", dto.getUserName());

        Pair<String, User> jws = authService.enter(dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(jws.first(), jws.second());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.enter", response));
    }

    @Operation(summary = "Верификация почты пользователя для регистрации",
            description = """
                    Первый из двух шагов для регистрации нового пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу регистрации -
                     добавление своих учетных данных.
                    """)
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если не удалось отправить письмо на почту",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/verifyEmailForRegistration")
    @Transactional
    public ResponseEntity<SuccessResponse<?>> verifyEmailForRegistration(@RequestBody EmailRequest dto) {
        logger.info("Verify email '{}' for registration", dto.getEmail());

        authService.verifyEmailForRegistration(dto.getEmail());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.verifyEmailForRegistration", null));
    }

    @Operation(summary = "Верификация почты пользователя для смены учетных данных",
            description = """
                    Первый из двух шагов для смены учетных данных существующего пользователя.
                     Отправляет на указанную почту письмо с ссылкой для подтверждения почты.
                     При переходе по ссылке в письме пользователь переходит ко второму шагу смены учетных данных -
                     указание своих новых учетных данных.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если не удалось отправить письмо на почту",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @PostMapping("/verifyEmailForChangeCredentials")
    @Transactional
    public ResponseEntity<SuccessResponse<?>> verifyEmailForChangeCredentials(@RequestBody EmailRequest dto) {
        logger.info("Verify email '{}' for change credentials", dto.getEmail());

        authService.verifyEmailForChangeCredentials(dto.getEmail());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.verifyEmailForChangeCredentials", null));
    }

    @Operation(summary = "Добавление учетных данных пользователя при регистрации",
            description = """
                    Второй и завершающий шаг регистрации нового пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "registrationToken")
    @PostMapping("/registration")
    @Transactional
    public ResponseEntity<SuccessResponse<JwsResponse>> registration(@RequestBody CredentialsForEnterRequest dto) {
        String email = requestContext.getCurrentJwsBodyAs(String.class);
        logger.info("registration new user: name='{}', email='{}'", dto.getUserName(), email);

        Pair<String, User> accessJws = authService.registration(email, dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(accessJws.first(), accessJws.second());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.registration", response));
    }

    @Operation(summary = "Указание новых учетных данных пользователя",
            description = """
                    Второй и завершающий шаг смены учетных данных пользователя.
                     На этом шаге пользователь указывает свои учетные данные.
                     Необходимо указать в http заголовке Authorization токен полученный на предыдущем шаге.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "restorePassToken")
    @PostMapping("/changeCredential")
    @Transactional
    public ResponseEntity<SuccessResponse<JwsResponse>> changeCredential(@RequestBody CredentialsForEnterRequest dto) {
        String email = requestContext.getCurrentJwsBodyAs(String.class);
        logger.info("change credentials for user with email='{}'", email);

        Pair<String, User> accessJws = authService.changeCredential(email, dto.getUserName(), dto.getUserPassword());

        JwsResponse response = mapper.toJwsResponse(accessJws.first(), accessJws.second());
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changeCredential", response));
    }

    @Operation(summary = "Смена логина и почты пользователя из его личного кабинета.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если уже есть пользователь с такими учетными данными или передан неверный токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/changeLoginAndEmail")
    @Transactional
    public ResponseEntity<SuccessResponse<UserResponse>> changeLoginAndEmail(
            @RequestBody ChangeLoginAndEmailRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Change login and email from personal area for user = {}", userId);

        User user = authService.changeLoginAndEmail(
                userId,
                dto.getUserName(),
                dto.getUserEmail(),
                dto.getUserPassword()
        );

        UserResponse response = mapper.toUserResponse(user);
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changeLoginAndEmail", response));
    }

    @Operation(summary = "Смена пароля пользователя из его личного кабинета.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403",
                    description = "Если передан неверный токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/changePassword")
    @Transactional
    public ResponseEntity<SuccessResponse<UserResponse>> changePassword(@RequestBody ChangePasswordRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);

        logger.info("Change password from personal area for user = {}", userId);

        User user = authService.changePassword(
                userId,
                dto.getUserCurrentPassword(),
                dto.getUserNewPassword()
        );

        UserResponse response = mapper.toUserResponse(user);
        return ResponseEntity.ok(mapper.toSuccessResponse("auth.changePassword", response));
    }

    @Operation(summary = "Возвращает пользователя в соответствии с токеном доступа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getUserByJws")
    @Transactional
    public ResponseEntity<UserResponse> getUserByJws() {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("User with id={} get own data", userId);

        User user = userRepository.tryGetById(userId);

        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @Operation(summary = "Выполняет logout для пользователя определяемого по токену доступа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<SuccessResponse<?>> logout() {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        String jws = requestContext.getJws();
        logger.info("User with id={} logout", userId);

        authService.logout(jws);

        return ResponseEntity.ok(mapper.toSuccessResponse("auth.logout", null));
    }

}

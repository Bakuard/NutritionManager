package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Контроллер для функций связанных с обслуживанием приложения",
        description = """
                Используется для получения данных об общем состоянии приложения, например,
                 для проверки - жив ли сервер.
                """
)
@RestController
@RequestMapping("/actuator")
public class ActuatorController {

    public ActuatorController() {

    }

    @Operation(summary = "Именно этот метод следует использовать для проверки - жив ли сервер.",
            description = "Если сервер жив - данный метод всегда будет возвращать строку \"I am alive!\" с кодом 200.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200")})
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("I am alive!");
    }

}

package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер блюд")
@RestController
@RequestMapping("/dishes")
public class DishController {

    private static final Logger logger = LoggerFactory.getLogger(DishController.class.getName());


    private DtoMapper mapper;
    private DishRepository repository;

    @Autowired
    public DishController(DtoMapper mapper, DishRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Operation(summary = "Добавление нового блюда",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/add")
    public ResponseEntity<DishResponse> add(@RequestBody DishRequest dto) {
        logger.info("Add new dish. dto={}", dto);

        return ResponseEntity.ok(new DishResponse());
    }

    @Operation(summary = "Обновление блюда",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PutMapping("/update")
    public ResponseEntity<DishResponse> update(@RequestBody DishRequest dto) {
        logger.info("Update dish. dto={}", dto);

        return ResponseEntity.ok(new DishResponse());
    }

    @Operation(summary = "Удаление блюда",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти блюдо с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<DishResponse> delete(
                @RequestParam("id")
                @Parameter(description = "Уникальный идентификатор блюда в формате UUID", required = true)
                UUID id) {
        logger.info("Delete dish by id={}", id);

        return ResponseEntity.ok(new DishResponse());
    }

    @Operation(summary = "Получение блюда по его ID",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти блюдо с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<DishResponse> getById(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор блюда в формате UUID", required = true)
            UUID id) {
        logger.info("Get dish by id = {}", id);

        return ResponseEntity.ok(new DishResponse());
    }

    @Operation(summary = "Получение блюда по его наименованию",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти блюдо с таким наименованием",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getByName")
    public ResponseEntity<DishResponse> getByName(
            @RequestParam("name")
            @Parameter(description = "Наименование блюда", required = true)
            String name,
            @RequestParam("userId")
            @Parameter(description = "Уникальный идентификатор пользователя в формате UUID", required = true)
            UUID userId) {
        logger.info("Get dish by name={} and userId={}", name, userId);

        return ResponseEntity.ok(new DishResponse());
    }

    @Operation(summary = "Получение выборки блюд указанного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getByFilter")
    public ResponseEntity<Page<DishForListResponse>> getByFilter(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 200]", required = true)
            int size,
            @RequestParam("sort")
            @Parameter(description = "Указывает порядок сортировки выборки продуктов.",
                schema = @Schema(
                    defaultValue = "category_asc (Сортировка по категориям в порядке возрастания).",
                    allowableValues = {
                            "name_asc",
                            "unit_asc",
                            "name_desc",
                            "unit_desc"
                    }
            ))
            String sortRule,
            @RequestParam("userId")
            @Parameter(description = """
                    Уникальный идентификатор пользователя в формате UUID.
                     Пользователь с таким ID должен существовать в БД.
                    """, required = true)
            UUID userId,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов блюд. В выборку попадут только те блюда,
                      которые имеют как минимум все указанные теги. Если параметр
                      имеет значение null - в выборку попадут блюда имющие любые теги
                      или не имющие их вовсе.
                      Если массив задается - он должен содержать как минимум один элемент,
                      все эелементы должны содержать как минимум один отображаемый символ.
                     """,
                    schema = @Schema(defaultValue = "null"))
            List<String> tags) {
        logger.info("Get dishes for list by filter: page={}, size={}, sortRule={}, userId={}, tags={}",
                page, size, sortRule, userId, tags);

        return ResponseEntity.ok(Pageable.firstEmptyPage());
    }

    @Operation(summary = "Получение выборки из всех единиц измерения блюд указанного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getUnits")
    public ResponseEntity<List<DishUnitResponse>> getUnits(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 200]", required = true)
            int size,
            @RequestParam("userId")
            @Parameter(description = """
                    Уникальный идентификатор пользователя в формате UUID.
                     Пользователь с таким ID должен существовать в БД.
                    """, required = true)
            UUID userId) {
        logger.info("Get dish units by userId={}", userId);
        List<DishUnitResponse> response = List.of();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех тегов используемых для блюд укзанного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти пользователя с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getTags")
    public ResponseEntity<Page<String>> getTags(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 200]", required = true)
            int size,
            @RequestParam("userId")
            @Parameter(description = """
                    Уникальный идентификатор пользователя в формате UUID.
                     Пользователь с таким ID должен существовать в БД.
                    """, required = true)
            UUID userId) {
        logger.info("Get dishes tags by userId. page={}, size={}, userId={}", page, size, userId);

        return ResponseEntity.ok(Pageable.firstEmptyPage());
    }

    @Operation(summary = "Подбирает и возвращает список продуктов для приготовления указанного блюда.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с параметрами запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти блюдо с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/pickProductsList")
    public ResponseEntity<DishProductsListResponse> pickProductsList(@RequestBody DishProductListRequest dto) {
        logger.info("Pick products list for dish. dto={}", dto);

        return ResponseEntity.ok(new DishProductsListResponse());
    }

    @Transactional
    @GetMapping("/pickProductsListAsMenuItem")
    public ResponseEntity<DishProductsListResponse> pickProductsList(
            @RequestParam("ingredients") List<Integer> ingredients,
            @RequestParam("dishId") UUID dishId,
            @RequestParam("menuId") UUID menuId) {
        logger.info("Pick products list for dish. ingredients={}, dishId={}, menuId={}", ingredients, dishId, menuId);

        return ResponseEntity.ok(new DishProductsListResponse());
    }

}

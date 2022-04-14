package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.menus.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер меню")
@RestController
@RequestMapping("/menus")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class.getName());


    private DtoMapper mapper;
    private MenuRepository repository;

    @Autowired
    public MenuController(DtoMapper mapper, MenuRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Operation(summary = "Загружает изображение меню и возвращает его URL",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = """
                                    Если размер файла превышает 250 Кб.
                                    """,
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/uploadImage")
    public ResponseEntity<SuccessResponse<URL>> uploadImage(@RequestParam("image") MultipartFile image) {
        logger.info("Upload menu image.");

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Добавление нового меню",
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
    public ResponseEntity<SuccessResponse<MenuResponse>> add(@RequestBody MenuAddRequest dto) {
        logger.info("Add new menu. dto={}", dto);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Обновление меню",
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
    public ResponseEntity<SuccessResponse<MenuResponse>> update(@RequestBody MenuUpdateRequest dto) {
        logger.info("Update menu. dto={}", dto);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Удаление меню",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти меню с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<MenuResponse>> delete(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор меню в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Delete menu by id={}", id);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Получение меню по его ID",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти меню с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<DishResponse> getById(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор меню в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Get menu by id = {}", id);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Получение меню по его наименованию",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти меню с таким наименованием",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getByName")
    public ResponseEntity<DishResponse> getByName(
            @RequestParam("name")
            @Parameter(description = "Наименование меню. Не может быть null.", required = true)
            String name) {
        logger.info("Get menu by name={}", name);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Получение выборки меню указанного пользователя",
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
    public ResponseEntity<Page<MenuForListResponse>> getByFilter(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля. Не может быть null.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 30]. Не может быть null.", required = true)
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Указывает порядок сортировки выборки меню.",
                    schema = @Schema(
                            defaultValue = "name_asc (Сортировка по наименованию в порядке возрастания).",
                            allowableValues = {
                                    "name_asc",
                                    "name_desc"
                            }
                    ))
            String sortRule,
            @RequestParam(value = "productCategories", required = false)
            @Parameter(description = """
                     Массив наименований блюд. В выборку попадут только те меню, которые имеют в своем
                      составе хотя бы одно из указанных блюд. Если параметр имеет значение null или является
                      пустым массивом - в выборку попадут меню имеющие с своем составе любые блюда.
                      Если массив задается - все элементы должны содержать как минимум один отображаемый символ.
                     """,
                    schema = @Schema(defaultValue = "null"))
            List<String> dishNames,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов меню. В выборку попадут только те меню,
                      которые имеют как минимум все указанные теги. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут меню
                      имеющие любые теги или не имющие их вовсе.
                      Если массив задается - все элементы должны содержать как минимум один отображаемый символ.
                     """,
                    schema = @Schema(defaultValue = "null"))
            List<String> tags) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();

        logger.info("Get menus for list by filter: page={}, size={}, userId={}, " +
                        "sortRule={}, dishNames={}, tags={}",
                page, size, userId, sortRule, dishNames, tags);

        return ResponseEntity.ok(null);
    }

    @Operation(summary = """
            Возвращает теги и наименования всех меню указанного пользователя.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getAllMenusFields")
    public ResponseEntity<MenuFieldsResponse> getAllMenusFields() {
        logger.info("Get all menus fields");

        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Подбирает и возвращает список продуктов для приготовления указанного меню.",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти меню с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/pickProductsList")
    public ResponseEntity<MenuProductsListResponse> pickProductsList(
            @RequestBody MenuProductsListRequest dto) {
        logger.info("Pick products list for menu. dto={}", dto);

        return ResponseEntity.ok(null);
    }

}

package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.util.Page;

import com.bakuard.nutritionManager.services.ImageUploaderService;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер блюд")
@RestController
@RequestMapping("/dishes")
public class DishController {

    private static final Logger logger = LoggerFactory.getLogger(DishController.class.getName());

    private DtoMapper mapper;
    private DishRepository dishRepository;
    private ImageUploaderService imageUploaderService;

    @Autowired
    public DishController(DtoMapper mapper,
                          DishRepository dishRepository,
                          ImageUploaderService imageUploaderService) {
        this.mapper = mapper;
        this.dishRepository = dishRepository;
        this.imageUploaderService = imageUploaderService;
    }

    @Operation(summary = "Загружает изображение блюда и возвращает его URL",
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
        logger.info("Upload dish image.");

        URL imageUrl = imageUploaderService.uploadDishImage(JwsAuthenticationProvider.getAndClearUserId(), image);

        return ResponseEntity.ok(mapper.toSuccessResponse("dish.uploadImage", imageUrl));
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
    public ResponseEntity<SuccessResponse<DishResponse>> add(@RequestBody DishAddRequest dto) {
        logger.info("Add new dish. dto={}", dto);

        Dish dish = mapper.toDish(JwsAuthenticationProvider.getAndClearUserId(), dto);
        dishRepository.save(dish);

        DishResponse response = mapper.toDishResponse(dish);
        return ResponseEntity.ok(mapper.toSuccessResponse("dish.add", response));
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
    public ResponseEntity<SuccessResponse<DishResponse>> update(@RequestBody DishUpdateRequest dto) {
        logger.info("Update dish. dto={}", dto);

        Dish dish = mapper.toDish(JwsAuthenticationProvider.getAndClearUserId(), dto);
        dishRepository.save(dish);

        DishResponse response = mapper.toDishResponse(dish);
        return ResponseEntity.ok(mapper.toSuccessResponse("dish.update", response));
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
    public ResponseEntity<SuccessResponse<DishResponse>> delete(
                @RequestParam("id")
                @Parameter(description = "Уникальный идентификатор блюда в формате UUID. Не может быть null.", required = true)
                UUID id) {
        logger.info("Delete dish by id={}", id);

        Dish dish = dishRepository.tryRemove(JwsAuthenticationProvider.getAndClearUserId(), id);

        DishResponse response = mapper.toDishResponse(dish);
        return ResponseEntity.ok(mapper.toSuccessResponse("dish.delete", response));
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
            @Parameter(description = "Уникальный идентификатор блюда в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Get dish by id = {}", id);

        Dish dish = dishRepository.tryGetById(JwsAuthenticationProvider.getAndClearUserId(), id);

        DishResponse response = mapper.toDishResponse(dish);
        return ResponseEntity.ok(response);
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
            @Parameter(description = "Наименование блюда. Не может быть null.", required = true)
            String name) {
        logger.info("Get dish by name={}", name);

        Dish dish = dishRepository.tryGetByName(JwsAuthenticationProvider.getAndClearUserId(), name);

        DishResponse response = mapper.toDishResponse(dish);
        return ResponseEntity.ok(response);
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
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля. Не может быть null.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 30]. Не может быть null.", required = true)
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Указывает порядок сортировки выборки продуктов.",
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
                     Массив категорий продуктов. В выборку попадут только те блюда, которые имеют в своем
                      составе продукты соответствующие хотя бы одной из перечисленных категорий. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут блюда имеющие с своем
                      составе продукты любых категорий.
                      Если массив задается - все элементы должны содержать как минимум один отображаемый символ.
                     """,
                    schema = @Schema(defaultValue = "null"))
            List<String> productCategories,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов блюд. В выборку попадут только те блюда,
                      которые имеют как минимум все указанные теги. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут блюда имеющие любые
                      теги или не имющие их вовсе.
                      Если массив задается - все элементы должны содержать как минимум один отображаемый символ.
                     """,
                    schema = @Schema(defaultValue = "null"))
            List<String> tags) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();

        logger.info("Get dishes for list by filter: page={}, size={}, userId={}, " +
                        "sortRule={}, productCategories={}, tags={}",
                page, size, userId, sortRule, productCategories, tags);

        Criteria criteria = mapper.toDishCriteria(
                page,
                size,
                userId,
                sortRule,
                productCategories,
                tags
        );

        Page<DishForListResponse> response = mapper.toDishesResponse(dishRepository.getDishes(criteria));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает теги, единицы измерения кол-ва и наименования всех блюд указанного пользователя.
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
    @GetMapping("/getAllDishesFields")
    public ResponseEntity<DishFieldsResponse> getAllDishesFields() {
        logger.info("Get all dishes fields");

        DishFieldsResponse response = mapper.toDishFieldsResponse(
                JwsAuthenticationProvider.getAndClearUserId()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Возвращает список продуктов для каждого ингредиента указанного блюда.",
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
                            description = "Если не удалось найти блюдо с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getAllIngredientProducts")
    public ResponseEntity<DishProductsListResponse> getAllIngredientProducts(
            @RequestParam("dishId")
            @Parameter(description = "Уникальный идентификатор блюда. Не может быть null.", required = true)
            UUID dishId,
            @RequestParam("servingNumber")
            @Parameter(description = "Кол-во порций блюда. Не может быть null. Должно быть больше 0.", required = false)
            BigDecimal servingNumber) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Get all ingredient products for dishId={} and ServingNumber={}", dishId, servingNumber);

        DishProductsListResponse response = mapper.toDishProductsListResponse(userId, dishId, servingNumber);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Рассчитывает и возвращает стоимость блюда, которая представляет собой суммарную стоимость недостающего
             кол-ва продукта выбранного для каждого ингредиента.
            """,
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
                            description = "Если не удалось найти блюдо с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/getDishPrice")
    public ResponseEntity<BigDecimal> getDishPrice(@RequestBody DishPriceRequest dto) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Pick products list for dish: userId={}, dto={}", userId, dto);

        BigDecimal response = mapper.toDishPrice(userId, dto).orElse(null);

        return ResponseEntity.ok(response);
    }

}

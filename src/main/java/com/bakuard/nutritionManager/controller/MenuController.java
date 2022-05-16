package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.DishProductsListResponse;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.menus.*;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.service.ImageUploaderService;
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

@Tag(name = "Контроллер меню")
@RestController
@RequestMapping("/menus")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class.getName());


    private DtoMapper mapper;
    private MenuRepository repository;

    private ImageUploaderService imageUploaderService;

    @Autowired
    public MenuController(DtoMapper mapper,
                          MenuRepository repository,
                          ImageUploaderService imageUploaderService) {
        this.mapper = mapper;
        this.repository = repository;
        this.imageUploaderService = imageUploaderService;
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

        URL imageUrl = imageUploaderService.uploadMenuImage(JwsAuthenticationProvider.getAndClearUserId(), image);

        return ResponseEntity.ok(mapper.toSuccessResponse("menu.uploadImage", imageUrl));
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

        Menu menu = mapper.toMenu(JwsAuthenticationProvider.getAndClearUserId(), dto);
        repository.save(menu);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.add", response));
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

        Menu menu = mapper.toMenu(JwsAuthenticationProvider.getAndClearUserId(), dto);
        repository.save(menu);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.update", response));
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

        Menu menu = repository.tryRemove(JwsAuthenticationProvider.getAndClearUserId(), id);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.delete", response));
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
    public ResponseEntity<MenuResponse> getById(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор меню в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Get menu by id = {}", id);

        Menu menu = repository.tryGetById(JwsAuthenticationProvider.getAndClearUserId(), id);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<MenuResponse> getByName(
            @RequestParam("name")
            @Parameter(description = "Наименование меню. Не может быть null.", required = true)
            String name) {
        logger.info("Get menu by name={}", name);

        Menu menu = repository.tryGetByName(JwsAuthenticationProvider.getAndClearUserId(), name);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(response);
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

        Criteria criteria = mapper.toMenuCriteria(
                page,
                size,
                userId,
                sortRule,
                dishNames,
                tags
        );

        Page<MenuForListResponse> response = mapper.toMenusResponse(repository.getMenus(criteria));
        return ResponseEntity.ok(response);
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

        MenuFieldsResponse response = mapper.toMenuFieldsResponse(
                JwsAuthenticationProvider.getAndClearUserId()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает список продуктов для каждого ингредиента указанного блюда. Необходимое кол-во
             каждого продукта рассчитывается с учетом кол-ва блюда в указанном меню и кол-ва этого меню.
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
    @GetMapping("/getAllDishIngredientProducts")
    public ResponseEntity<DishProductsListResponse> getAllDishIngredientProducts(
            @RequestParam("dishName")
            @Parameter(description = "Уникальное наименование блюда. Не может быть null.", required = true)
            String dishName,
            @RequestParam("menuId")
            @Parameter(description = "Уникальный идентификатор меню. Не может быть null.", required = true)
            UUID menuId,
            @RequestParam(value = "menuQuantity", required = false)
            @Parameter(description = "Кол-во меню. Должно быть больше 0. Значение по умолчанию равно 1.")
            BigDecimal menuQuantity) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Get all ingredient products for dishName={}, menuId={}, menuQuantity={}",
                dishName, menuId, menuQuantity);

        DishProductsListResponse response = mapper.toDishProductsListResponse(userId, menuId, dishName, menuQuantity);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Рассчитывает и возвращает стоимость меню, которая представляет собой суммарную стоимость недостающего
             кол-ва продукта выбранного для каждого ингредиента каждого блюда этого меню.
            """,
            description = """
            Рассчитывает и возвращает стоимость меню, которая представляет собой суммарную стоимость недостающего
             кол-ва продукта выбранного для каждого ингредиента каждого блюда этого меню. Особые случаи: <br/>
            1. Если ни одно блюдо не содержит ни одного ингредиента - возвращает null. <br/>
            2. Если ни одному ингредиенту ни одного блюда не соответствует ни один продукт - возвращает null. <br/>
            3. Если какому-либо ингредиенту какого-либо блюда не соответствует ни одного продукта - то он не принимает
             участия в расчете стоимости меню. <br/>
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
                            description = "Если не удалось найти меню с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @PostMapping("/getLackProductPrice")
    public ResponseEntity<BigDecimal> getLackProductPrice(@RequestBody MenuPriceRequest dto) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Pick products list for menu dishes: userId={}, dto={}", userId, dto);

        BigDecimal response = mapper.toMenuPrice(userId, dto).orElse(null);

        return ResponseEntity.ok(response);
    }

}

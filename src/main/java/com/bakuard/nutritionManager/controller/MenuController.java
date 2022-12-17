package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.DishReportRequest;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.menus.*;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.service.ImageUploaderService;
import com.bakuard.nutritionManager.service.menuGenerator.Input;
import com.bakuard.nutritionManager.service.menuGenerator.MenuGeneratorService;
import com.bakuard.nutritionManager.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private ReportService reportService;

    private MenuGeneratorService menuGeneratorService;

    @Autowired
    public MenuController(DtoMapper mapper,
                          MenuRepository repository,
                          ImageUploaderService imageUploaderService,
                          ReportService reportService,
                          MenuGeneratorService menuGeneratorService) {
        this.mapper = mapper;
        this.repository = repository;
        this.imageUploaderService = imageUploaderService;
        this.reportService = reportService;
        this.menuGeneratorService = menuGeneratorService;
    }

    @Operation(summary = "Загружает изображение меню и возвращает его URL")
    @ApiResponses(value = {
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
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/uploadImage")
    @Transactional
    public ResponseEntity<SuccessResponse<URL>> uploadImage(@RequestParam("image") MultipartFile image) {
        logger.info("Upload menu image.");

        URL imageUrl = imageUploaderService.uploadMenuImage(JwsAuthenticationProvider.getAndClearUserId(), image);

        return ResponseEntity.ok(mapper.toSuccessResponse("menu.uploadImage", imageUrl));
    }

    @Operation(summary = "Добавление нового меню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<SuccessResponse<MenuResponse>> add(@RequestBody MenuAddRequest dto) {
        logger.info("Add new menu. dto={}", dto);

        Menu menu = mapper.toMenu(JwsAuthenticationProvider.getAndClearUserId(), dto);
        repository.save(menu);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.add", response));
    }

    @Operation(summary = "Обновление меню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связаный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PutMapping("/update")
    @Transactional
    public ResponseEntity<SuccessResponse<MenuResponse>> update(@RequestBody MenuUpdateRequest dto) {
        logger.info("Update menu. dto={}", dto);

        Menu menu = mapper.toMenu(JwsAuthenticationProvider.getAndClearUserId(), dto);
        repository.save(menu);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.update", response));
    }

    @Operation(summary = "Генерирует новое меню на основе заданных ограничений")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = """
                                    Если нарушен хотя бы один из инвариантов связаный с телом запроса или
                                     невозможно создать меню удовлетворяющее заданным ограничениям.
                                    """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<SuccessResponse<MenuResponse>> generate(@RequestBody GenerateMenuRequest dto) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Generate menu for user={}. dto={}", userId, dto);

        Input input = mapper.toInput(userId, dto);
        Menu menu = menuGeneratorService.generate(input);
        MenuResponse response = mapper.toMenuResponse(menu);

        return ResponseEntity.ok(mapper.toSuccessResponse("menu.generate", response));
    }

    @Operation(summary = "Удаление меню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти меню с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<SuccessResponse<MenuResponse>> delete(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор меню в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Delete menu by id={}", id);

        Menu menu = repository.tryRemove(JwsAuthenticationProvider.getAndClearUserId(), id);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(mapper.toSuccessResponse("menu.delete", response));
    }

    @Operation(summary = "Получение меню по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти меню с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getById")
    @Transactional
    public ResponseEntity<MenuResponse> getById(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор меню в формате UUID. Не может быть null.", required = true)
            UUID id) {
        logger.info("Get menu by id = {}", id);

        Menu menu = repository.tryGetById(JwsAuthenticationProvider.getAndClearUserId(), id);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение меню по его наименованию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти меню с таким наименованием",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getByName")
    @Transactional
    public ResponseEntity<MenuResponse> getByName(
            @RequestParam("name")
            @Parameter(description = "Наименование меню. Не может быть null.", required = true)
            String name) {
        logger.info("Get menu by name={}", name);

        Menu menu = repository.tryGetByName(JwsAuthenticationProvider.getAndClearUserId(), name);

        MenuResponse response = mapper.toMenuResponse(menu);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки меню указанного пользователя")
    @ApiResponses(value = {
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
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getByFilter")
    @Transactional
    public ResponseEntity<Page<MenuForListResponse>> getByFilter(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля. Не может быть null.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 30]. Не может быть null.", required = true)
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = """
                    Задает порядок сортировки
                    <br/><br/>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>name - сортировка по наименованию меню </li>
                    </ol>
                    Параметры сортировки можно комбинировать через запятую.
                    </br></br>
                    Направление сортировки для параметра задается в виде <i>параметр_направление</i>, где направление
                     задается одной из следующих констант (без учета регистра символов):
                    <ol>
                        <li>asc (по умолчанию)</li>
                        <li>ascending</li>
                        <li>desc</li>
                        <li>descending</li>
                    </ol>
                    """,
                    schema = @Schema(defaultValue = "name_asc"))
            String sortRule,
            @RequestParam(value = "dishNames", required = false)
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
                      имеющие любые теги или не имеющие их вовсе.
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
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getAllMenusFields")
    @Transactional
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
            """)
    @ApiResponses(value = {
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
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getAllDishIngredientProducts")
    @Transactional
    public ResponseEntity<MenuDishProductsListResponse> getAllDishIngredientProducts(
            @RequestParam("menuId")
            @Parameter(description = "Уникальный идентификатор меню. Не может быть null.", required = true)
            UUID menuId,
            @RequestParam(value = "dishName", required = false)
            @Parameter(description = """
                    Наименование одного из блюд указанного меню. Особые случаи: <br/>
                    1. Если данный параметр не указан (для него задано значение null) и в составе меню
                     ЕСТЬ блюда - в качестве значения по умолчанию будет взято первое блюдо из списка блюд
                     этого меню. <br/>
                    2. Если данный параметр не указан (для него задано значение null) и в составе меню
                     НЕТ блюд - данный параметр будет проигнорирован. <br/>
                    2. Если данный параметр ЗАДАН и в составе меню нет блюда с таким именем - кидает исключение.
                    """)
            String dishName,
            @RequestParam(value = "menuQuantity", required = false)
            @Parameter(description = "Кол-во меню. Должно быть больше 0. Значение по умолчанию равно 1.")
            BigDecimal menuQuantity) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Get all ingredient products for menuId={}, dishName={}, menuQuantity={}",
                menuId, dishName, menuQuantity);

        MenuDishProductsListResponse response = mapper.toMenuDishProductsListResponse(
                userId, menuId, dishName, menuQuantity);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает список продуктов для каждого ингредиента каждого блюда. Необходимое кол-во
             каждого продукта рассчитывается с учетом кол-ва блюда в указанном меню и кол-ва этого меню.
            """)
    @ApiResponses(value = {
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
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getIngredientProductsOfAllDishes")
    @Transactional
    public ResponseEntity<MenuDishesProductsListResponse> getIngredientProductsOfAllDishes(
            @RequestParam("menuId")
            @Parameter(description = "Уникальный идентификатор меню. Не может быть null.", required = true)
            UUID menuId,
            @RequestParam(value = "menuQuantity", required = false)
            @Parameter(description = "Кол-во меню. Должно быть больше 0. Значение по умолчанию равно 1.")
            BigDecimal menuQuantity) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("Get all ingredient products of all dishes for menuId={}, menuQuantity={}",
                menuId, menuQuantity);

        MenuDishesProductsListResponse response = mapper.toMenuDishesProductsListResponse(
                userId, menuId, menuQuantity
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Составляет и возвращает отчет содержащий перечень всех недостающих продуктов и их кол-во, а также
             суммарную их стоимость. Этот список создается на основе выбранных пользователем продуктов для каждого
             ингредиента каждого блюда.
            """)
    @ApiResponses(value = {
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
    })
    @SecurityRequirement(name = "commonToken")
    @PostMapping("/createReport")
    @Transactional
    public ResponseEntity<Resource> createReport(@RequestBody MenuReportRequest dto) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();
        logger.info("create menu report: userId={}, dto={}", userId, dto);

        ReportService.MenuProductsReportData reportInputData = mapper.toMenuProductsReportData(userId, dto);
        byte[] reportOutputData = reportService.createMenuProductsReport(reportInputData);

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "inline;attachment; filename=report.pdf");
        header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

        return ResponseEntity.ok().
                headers(header).
                body(new ByteArrayResource(reportOutputData));
    }

}

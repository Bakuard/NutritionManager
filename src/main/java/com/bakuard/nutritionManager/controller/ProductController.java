package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.security.RequestContext;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.products.fields.ProductFieldsByCategoryResponse;
import com.bakuard.nutritionManager.dto.products.fields.ProductFieldsResponse;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.service.ImageUploaderService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Tag(name = "Контроллер продуктов")
@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class.getName());


    private DtoMapper mapper;
    private ProductRepository productRepository;
    private ImageUploaderService imageUploaderService;
    private RequestContext requestContext;

    @Autowired
    public ProductController(DtoMapper mapper,
                             ProductRepository productRepository,
                             ImageUploaderService imageUploaderService,
                             RequestContext requestContext) {
        this.mapper = mapper;
        this.productRepository = productRepository;
        this.imageUploaderService = imageUploaderService;
        this.requestContext = requestContext;
    }

    @Operation(summary = "Загружает изображение продукта и возвращает его URL")
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
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Upload product image of user {}", userId);

        URL imageUrl = imageUploaderService.uploadProductImage(userId, image);

        return ResponseEntity.ok(mapper.toSuccessResponse("product.uploadImage", imageUrl));
    }

    @Operation(summary = "Добавление нового продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связанный с телом запроса",
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
    public ResponseEntity<SuccessResponse<ProductResponse>> add(@RequestBody ProductAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Add new product for user {}. dto={}", userId, dto);

        Product product = mapper.toProduct(userId, dto);
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.add", response));
    }

    @Operation(summary = "Обновление продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связанный с телом запроса",
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
    public ResponseEntity<SuccessResponse<ProductResponse>> update(@RequestBody ProductUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Update product of user {}. dto={}", userId, dto);

        Product product = mapper.toProduct(userId, dto);
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.update", response));
    }

    @Operation(summary = "Удаление продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти продукт с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<SuccessResponse<ProductResponse>> delete(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор продукта в формате UUID. Не может быть null.", required = true)
            UUID id) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Delete product with id={} of user {}", id, userId);

        Product product = productRepository.tryRemove(userId, id);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.delete", response));
    }

    @Operation(summary = "Увеличение кол-ва продукта имеющегося в наличии у пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связанный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти продукт с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PatchMapping("/addQuantity")
    @Transactional
    public ResponseEntity<SuccessResponse<ProductResponse>> addQuantity(@RequestBody ProductAddedQuantityRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Add quantity to product of user {}. dto={}", userId, dto);

        Product product = productRepository.tryGetById(userId, dto.getProductId());
        product.addQuantity(dto.getAddedQuantity());
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.addQuantity", response));
    }

    @Operation(summary = "Уменьшение кол-ва продукта имеющегося в наличии у пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связанный с телом запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти продукт с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @PatchMapping("/takeQuantity")
    @Transactional
    public ResponseEntity<SuccessResponse<ProductResponse>> takeQuantity(@RequestBody ProductTakeQuantityRequest dto) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Take quantity from product of user {}. dto={}", userId, dto);

        Product product = productRepository.tryGetById(userId, dto.getProductId());
        product.take(dto.getTakeQuantity());
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.takeQuantity", response));
    }

    @Operation(summary = "Получение продукта по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "404",
                    description = "Если не удалось найти продукт с таким ID",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getById")
    @Transactional
    public ResponseEntity<ProductResponse> getById(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор продукта в формате UUID. Не может быть null.", required = true)
            UUID id) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Get product with id={} of user {}", id, userId);

        Product product = productRepository.tryGetById(userId, id);

        return ResponseEntity.ok(mapper.toProductResponse(product));
    }

    @Operation(summary = "Получение выборки продуктов указанного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    description = "Если нарушен хотя бы один из инвариантов связанный с параметрами запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getByFilter")
    @Transactional
    public ResponseEntity<Page<ProductResponse>> getByFilter(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля. Не может быть null.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапазон значений - [1, 30]. Не может быть null.", required = true)
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = """
                    Задает порядок сортировки
                    <br/><br/>
                    Допустимые параметры (без учета регистра символов):
                    <ol>
                        <li>category - сортировка по категориям продуктов </li>
                        <li>price - сортировка по цене продуктов </li>
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
                     schema = @Schema(defaultValue = "category_asc"))
            String sortRule,
            @RequestParam(value = "onlyFridge", required = false)
            @Parameter(description = """
                     Если true - выборка будет проводится только по тем продуктам,
                      которые есть в наличии у пользователя (параметр quantity у таких
                      продуктов больше нуля). Иначе выборка будет проводится
                      по всем продуктам пользователя.
                     """,
                     schema = @Schema(defaultValue = "false"))
            boolean onlyFridge,
            @RequestParam(value = "category", required = false)
            @Parameter(description = """
                     Массив категорий продуктов. В выборку попадут только те продукты,
                      которые связаны с любой из указанных категорий. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут
                      продукты связанные с любыми категориями.
                      Если массив задается - все элементы должны содержать как минимум
                      один отображаемый символ.
                    """)
            List<String> categories,
            @RequestParam(value = "shops", required = false)
            @Parameter(description = """
                     Массив магазинов продуктов. В выборку попадут только те продукты,
                      которые связаны с любым из указанных магазинов. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут
                      продукты связанные с любыми магазинами.
                      Если массив задается - все элементы должны содержать как минимум
                      один отображаемый символ.
                     """)
            List<String> shops,
            @RequestParam(value = "grades", required = false)
            @Parameter(description = """
                     Массив сортов продуктов. В выборку попадут только те продукты,
                      которые имеют с любой из указанных сортов. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут
                      продукты связанные с любыми сортами.
                      Если массив задается - все элементы должны содержать как минимум один
                      отображаемый символ.
                     """)
            List<String> grades,
            @RequestParam(value = "manufacturers", required = false)
            @Parameter(description = """
                     Массив производителей продуктов. В выборку попадут только те продукты,
                      которые связаны с любым из указанных производителей. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут
                      продукты связанные с любыми производителями.
                      Если массив задается - все элементы должны содержать как минимум один
                      отображаемый символ.
                     """)
            List<String> manufacturers,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов продуктов. В выборку попадут только те продукты,
                      которые имеют как минимум все указанные теги. Если параметр
                      имеет значение null или является пустым массивом - в выборку попадут
                      продукты связанные с любыми тегами.
                      Если массив задается -  все элементы должны содержать как минимум
                      один отображаемый символ.
                     """)
            List<String> tags) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Get products by filter: user={}, page={}, size={}, userId={}, sortRule={}, onlyFridge={}, " +
                        "categories={}, shops={}, grades={}, manufacturers={}, tags={}",
                userId, page, size, userId, sortRule, onlyFridge,
                categories, shops, grades, manufacturers, tags);

        Criteria criteria = mapper.toProductCriteria(
                page,
                size,
                userId,
                sortRule,
                onlyFridge,
                categories,
                shops,
                grades,
                manufacturers,
                tags
        );

        Page<ProductResponse> response = mapper.toProductsResponse(productRepository.getProducts(criteria));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает производителей, торговые точки, сорта, категории и теги всех продуктов пользователя
             сделавшего запрос. При этом, если пользователь указывает в аргументах некоторые из перечисленных
             выше данных, то возвращаемый результат будет ограничен этими данными, например: если пользователь
             задал некоторого производителя, то будут возвращены указанный производитель, а также все категории,
             теги, сорта и торговые точки имеющие связь с указанным производителем.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getAllProductsFields")
    @Transactional
    public ResponseEntity<ProductFieldsResponse> getAllProductsFields(
            @RequestParam(value = "category", required = false)
            @Parameter(description = """
                    Массив категорий продуктов уже выбранных пользователем.
                     Список может быть пустым или иметь значение null.
                    """)
            List<String> categories,
            @RequestParam(value = "shops", required = false)
            @Parameter(description = """
                     Массив магазинов продуктов уже выбранных пользователем.
                      Список может быть пустым или иметь значение null.
                     """)
            List<String> shops,
            @RequestParam(value = "grades", required = false)
            @Parameter(description = """
                     Массив сортов продуктов уже выбранных пользователем.
                      Список может быть пустым или иметь значение null.
                     """)
            List<String> grades,
            @RequestParam(value = "manufacturers", required = false)
            @Parameter(description = """
                     Массив производителей продуктов уже выбранных пользователем.
                      Список может быть пустым или иметь значение null.
                     """)
            List<String> manufacturers,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов продуктов уже выбранных пользователем.
                      Список может быть пустым или иметь значение null.
                     """)
            List<String> tags) {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("Get products all products fields for categories={}, shops={}, grades={}, manufacturers={}, tags={}, user={}",
                categories, shops, grades, manufacturers, tags, userId);

        Criteria criteria = mapper.toProductCriteria(
                userId,
                categories,
                shops,
                grades,
                manufacturers,
                tags
        );

        ProductFieldsResponse response = mapper.toProductFieldsResponse(criteria);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает производителей, торговые точки, сорта и теги всех продуктов пользователя,
              сделавшего запрос, сгруппированные по категориям.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401",
                    description = "Если передан некорректный токен или токен не указан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)))
    })
    @SecurityRequirement(name = "commonToken")
    @GetMapping("/getProductsFieldsByCategories")
    @Transactional
    public ResponseEntity<List<ProductFieldsByCategoryResponse>> getProductsFieldsByCategories() {
        UUID userId = requestContext.getCurrentJwsBodyAs(UUID.class);
        logger.info("get products fields by categories for user={}", userId);

        List<ProductFieldsByCategoryResponse> response = mapper.toProductFieldsByCategoryResponse(userId);

        return ResponseEntity.ok(response);
    }

}

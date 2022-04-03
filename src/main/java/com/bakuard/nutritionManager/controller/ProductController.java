package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.config.JwsAuthenticationProvider;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.dto.DtoMapper;

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

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Tag(
        name = "Котроллер продуктов",
        description = "Предоставляет CRUD операции для продуктов."
)
@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class.getName());


    private DtoMapper mapper;
    private ProductRepository productRepository;
    private ImageUploaderService imageUploaderService;

    @Autowired
    public ProductController(DtoMapper mapper,
                             ProductRepository productRepository,
                             ImageUploaderService imageUploaderService) {
        this.mapper = mapper;
        this.productRepository = productRepository;
        this.imageUploaderService = imageUploaderService;
    }

    @Operation(summary = "Загружает изображение продукта и возвращает его URL",
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
        logger.info("Upload product image.");

        URL imageUrl = imageUploaderService.uploadProductImage(JwsAuthenticationProvider.getAndClearUserId(), image);

        return ResponseEntity.ok(mapper.toSuccessResponse("product.uploadImage", imageUrl));
    }

    @Operation(summary = "Добавление нового продукта",
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
    public ResponseEntity<SuccessResponse<ProductResponse>> add(@RequestBody ProductAddRequest dto) {
        logger.info("Add new product. dto={}", dto);

        Product product = mapper.toProductForAdd(JwsAuthenticationProvider.getAndClearUserId(), dto);
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.add", response));
    }

    @Operation(summary = "Обновление продукта",
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
    public ResponseEntity<SuccessResponse<ProductResponse>> update(@RequestBody ProductUpdateRequest dto) {
        logger.info("Update product. dto={}", dto);

        Product product = mapper.toProductForUpdate(JwsAuthenticationProvider.getAndClearUserId(), dto);
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.update", response));
    }

    @Operation(summary = "Удаление продукта",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти продукт с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<SuccessResponse<ProductResponse>> delete(
            @RequestParam("id")
            @Parameter(description = "Уникальный идентификатор продукта в формате UUID", required = true)
            UUID id) {
        logger.info("Delete product with id={}", id);

        Product product = productRepository.remove(id);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.delete", response));
    }

    @Operation(summary = "Увеличение кол-ва продукта имеющегося в наличии у пользователя",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если указано не корректное значение для добавляемого кол-ва кол-ва продукта",
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
            }
    )
    @Transactional
    @PatchMapping("/addQuantity")
    public ResponseEntity<SuccessResponse<ProductResponse>> addQuantity(@RequestBody ProductAddedQuantityRequest dto) {
        logger.info("Add quantity to product. dto={}", dto);

        Product product = productRepository.getById(dto.getProductId());
        product.addQuantity(dto.getAddedQuantity());
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.addQuantity", response));
    }

    @Operation(summary = "Уменьшение кол-ва продукта имеющегося в наличии у пользователя",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Если указано не корректное значение для добавляемого кол-ва кол-ва продукта",
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
            }
    )
    @Transactional
    @PatchMapping("/takeQuantity")
    public ResponseEntity<SuccessResponse<ProductResponse>> takeQuantity(@RequestBody ProductTakeQuantityRequest dto) {
        logger.info("Take quantity from product. dto={}", dto);

        Product product = productRepository.getById(dto.getProductId());
        product.take(dto.getTakeQuantity());
        productRepository.save(product);

        ProductResponse response = mapper.toProductResponse(product);
        return ResponseEntity.ok(mapper.toSuccessResponse("product.takeQuantity", response));
    }

    @Operation(summary = "Получение продукта по его ID",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401",
                            description = "Если передан некорректный токен или токен не указан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404",
                            description = "Если не удалось найти продукт с таким ID",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<ProductResponse> getById(@RequestParam("id")
                                                   @Parameter(description = "Уникальный идентификатор продукта в формате UUID", required = true)
                                                    UUID id) {
        logger.info("Get product with id={}", id);

        Product product = productRepository.getById(id);
        return ResponseEntity.ok(mapper.toProductResponse(product));
    }

    @Operation(summary = "Получение выборки продуктов указанного пользователя",
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
    public ResponseEntity<Page<ProductResponse>> getByFilter(
            @RequestParam("page")
            @Parameter(description = "Номер страницы выборки. Нумерация начинается с нуля.", required = true)
            int page,
            @RequestParam("size")
            @Parameter(description = "Размер страницы выборки. Диапозон значений - [1, 200]", required = true)
            int size,
            @RequestParam(value = "sort", required = false)
            @Parameter(description = "Указывает порядок сортировки выборки продуктов.",
                     schema = @Schema(
                             defaultValue = "category_asc (Сортировка по категориям в порядке возрастания).",
                             allowableValues = {
                                     "category_asc",
                                     "price_asc",
                                     "variety_asc",
                                     "shop_asc",
                                     "manufacturer_asc",
                                     "category_desc",
                                     "price_desc",
                                     "variety_desc",
                                     "shop_desc",
                                     "manufacturer_desc"
                             }
                     ))
            String sortRule,
            @RequestParam(value = "onlyFridge", required = false)
            @Parameter(description = """
                     Если true - выборка будет проводится только по тем продуктам,
                      которые есть в наличии у пользователя (параметр quantity у таких
                      продуктов больше нуля). Иначе выборка будет проводится
                      по всем продуктам пользователя.
                     """,
                     schema = @Schema(defaultValue = "false"))
            Boolean onlyFridge,
            @RequestParam(value = "category", required = false)
            @Parameter(description = "Категория продуктов",
                    schema = @Schema(defaultValue = "null"))
            String category,
            @RequestParam(value = "shops", required = false)
            @Parameter(description = """
                     Массив магазинов продуктов. В выборку попадут только те продукты,
                      которые связаны с любым из указанных магазинов. Если параметр
                      имеет значение null - в выборку попадут продукты связанные с любыми
                      магазинами.
                      Если массив задается - он должен содержать как минимум один элемент,
                      все эелементы должны содержать как минимум один отображаемый символ.
                     """,
                     schema = @Schema(defaultValue = "null"))
            List<String> shops,
            @RequestParam(value = "varieties", required = false)
            @Parameter(description = """
                     Массив сортов продуктов. В выборку попадут только те продукты,
                      которые имеют с любой из указанных сортов. Если параметр
                      имеет значение null - в выборку попадут продукты имеющие любой
                      сорт.
                      Если массив задается - он должен содержать как минимум один элемент,
                      все эелементы должны содержать как минимум один отображаемый символ.
                     """,
                     schema = @Schema(defaultValue = "null"))
            List<String> varieties,
            @RequestParam(value = "manufacturers", required = false)
            @Parameter(description = """
                     Массив производителей продуктов. В выборку попадут только те продукты,
                      которые связаны с любым из указанных производителей. Если параметр
                      имеет значение null - в выборку попадут продукты связанные с любыми
                      производителями.
                      Если массив задается - он должен содержать как минимум один элемент,
                      все эелементы должны содержать как минимум один отображаемый символ.
                     """,
                     schema = @Schema(defaultValue = "null"))
            List<String> manufacturers,
            @RequestParam(value = "tags", required = false)
            @Parameter(description = """
                     Массив тегов продуктов. В выборку попадут только те продукты,
                      которые имеют как минимум все указанные теги. Если параметр
                      имеет значение null - в выборку попадут продукты имющие любые теги
                      или не имющие их вовсе.
                      Если массив задается - он должен содержать как минимум один элемент,
                      все эелементы должны содержать как минимум один отображаемый символ.
                     """,
                     schema = @Schema(defaultValue = "null"))
            List<String> tags) {
        UUID userId = JwsAuthenticationProvider.getAndClearUserId();

        logger.info("Get products by filter: page={}, size={}, userId={}, sortRule={}, onlyFridge={}, " +
                        "category={}, shops={}, varieties={}, manufacturers={}, tags={}",
                page, size, userId, sortRule, onlyFridge,
                category, shops, varieties, manufacturers, tags);

        Criteria criteria = mapper.toProductCriteria(
                page,
                size,
                userId,
                sortRule,
                onlyFridge,
                category,
                shops,
                varieties,
                manufacturers,
                tags
        );

        Page<ProductResponse> response = mapper.toProductsResponse(productRepository.getProducts(criteria));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = """
            Возвращает производителей, торговые точки, сорта, категории и теги всех продуктов указанного
            пользователя без дубликатов.
            """,
            responses = {
                    @ApiResponse(responseCode = "200"),
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
    @GetMapping("/getAllProductsFields")
    public ResponseEntity<ProductFieldsResponse> getAllProductsFields() {
        logger.info("Get products categories, sorts, tags, manufacturers, shops for user");

        ProductFieldsResponse response = mapper.toProductFieldsResponse(
                JwsAuthenticationProvider.getAndClearUserId()
        );

        return ResponseEntity.ok(response);
    }

}

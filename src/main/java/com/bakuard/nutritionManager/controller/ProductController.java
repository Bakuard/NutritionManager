package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.criteria.ProductCategoryCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductFieldCriteria;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.dto.DtoMapper;

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

    @Autowired
    public ProductController(DtoMapper mapper,
                             ProductRepository productRepository) {
        this.mapper = mapper;
        this.productRepository = productRepository;
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
    public ResponseEntity<SuccessResponse<ProductResponse>> add(@RequestBody ProductRequest dto) {
        logger.info("Add new product. dto={}", dto);

        Product product = mapper.toProductForAdd(dto);
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
    public ResponseEntity<SuccessResponse<ProductResponse>> update(@RequestBody ProductRequest dto) {
        logger.info("Update product. dto={}", dto);

        Product product = mapper.toProductForUpdate(dto);
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
            @RequestParam("userId")
            @Parameter(description = """
                    Уникальный идентификатор пользователя в формате UUID.
                     Пользователь с таким ID должен существовать в БД.
                    """, required = true)
            UUID userId,
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
        logger.info("Get products by filter: page={}, size={}, userId={}, sortRule={}, onlyFridge={}, " +
                        "category={}, shops={}, varieties={}, manufacturers={}, tags={}",
                page, size, userId, sortRule, onlyFridge, category, shops, varieties, manufacturers, tags);

        ProductCriteria criteria = mapper.toProductCriteria(page, size, userId, sortRule, onlyFridge,
                category, shops, varieties, manufacturers, tags);
        Page<ProductResponse> response = mapper.toProductsResponse(productRepository.getProducts(criteria));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех тегов использующихся для продуктов указанного пользователя",
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
    public ResponseEntity<Page<TagRequestAndResponse>> getTags(
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
            UUID userId,
            @RequestParam(value = "productCategory", required = false)
            @Parameter(description = """
                     Указывает, что выборка должна формироваться из тегов связанных
                      только с продуктами указанных категорий указанного пользователя. Если
                      задано значение null - выборка формируется из тегов связанных с любыми
                      продуктами указанного пользователя.
                     """, schema = @Schema(defaultValue = "null"))
            String productCategory) {
        logger.info("Get products tags by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

        Page<TagRequestAndResponse> response = mapper.toTagsResponse(
                productRepository.getTags(criteria)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех магазинов продуктов указанного пользователя",
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
    @GetMapping("/getShops")
    public ResponseEntity<Page<ShopResponse>> getShops(
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
            UUID userId,
            @RequestParam(value = "productCategory", required = false)
            @Parameter(description = """
                   Указывает, что выборка должна формироваться из магазинов связанных
                    только с продуктами указанных категорий указанного пользователя. Если
                    задано значение null - выборка формируется из магазинов связанных с любыми
                    продуктами указанного пользователя.
                   """, schema = @Schema(defaultValue = "null"))
            String productCategory) {
        logger.info("Get products shops by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

        Page<ShopResponse> response = mapper.toShopsResponse(
                productRepository.getShops(criteria)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех сортов продуктов указанного пользователя",
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
    @GetMapping("/getVarieties")
    public ResponseEntity<Page<VarietyResponse>> getVarieties(
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
          UUID userId,
          @RequestParam(value = "productCategory", required = false)
          @Parameter(description = """
             Указывает, что выборка должна формироваться из сортов связанных
              только с продуктами указанных категорий указанного пользователя. Если
              задано значение null - выборка формируется из сортов связанных с любыми
              продуктами указанного пользователя.
             """, schema = @Schema(defaultValue = "null"))
          String productCategory) {
        logger.info("Get products varieties by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

        Page<VarietyResponse> response = mapper.toVarietiesResponse(
                productRepository.getVarieties(criteria)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех категорий продуктов указанного пользователя",
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
    @GetMapping("/getCategories")
    public ResponseEntity<Page<CategoryResponse>> getCategories(
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
        logger.info("Get products categories by userId. page={}, size={}, userId={}", page, size, userId);

        ProductCategoryCriteria criteria = mapper.toProductCategoryCriteria(page, size, userId);

        Page<CategoryResponse> response = mapper.toCategoryResponse(
                productRepository.getCategories(criteria)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение выборки из всех производителей продуктов указанного пользователя",
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
    @GetMapping("/getManufacturers")
    public ResponseEntity<Page<ManufacturerResponse>> getManufacturers(
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
          UUID userId,
          @RequestParam(value = "productCategory", required = false)
          @Parameter(description = """
             Указывает, что выборка должна формироваться из производителей связанных
              только с продуктами указанных категорий указанного пользователя. Если
              задано значение null - выборка формируется из производителей связанных с любыми
              продуктами указанного пользователя.
             """, schema = @Schema(defaultValue = "null"))
          String productCategory) {
        logger.info("Get products categories by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

        Page<ManufacturerResponse> response = mapper.toManufacturerResponse(
                productRepository.getManufacturers(criteria)
        );

        return ResponseEntity.ok(response);
    }

}

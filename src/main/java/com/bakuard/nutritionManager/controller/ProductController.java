package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dal.criteria.ProductCategoryCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductFieldCriteria;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.dto.DtoMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class.getName());


    private DtoMapper mapper;
    private ExceptionResolver exceptionResolver;
    private ProductRepository productRepository;
    private UserRepository userRepository;

    @Autowired
    public ProductController(DtoMapper mapper,
                             ExceptionResolver exceptionResolver,
                             ProductRepository productRepository,
                             UserRepository userRepository) {
        this.mapper = mapper;
        this.exceptionResolver = exceptionResolver;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody ProductRequest dto) {
        logger.info("Add new product. dto={}", dto);
        try {
            Product product = mapper.toProductForAdd(dto);
            productRepository.save(product);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody ProductRequest dto) {
        logger.info("Update product. dto={}", dto);
        try {
            Product product = mapper.toProductForUpdate(dto);
            productRepository.save(product);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam("id") UUID id) {
        logger.info("Delete product with id={}", id);
        try {
            Product product = productRepository.remove(id);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @PatchMapping("/addQuantity")
    public ResponseEntity<?> addQuantity(@RequestBody ProductAddedQuantityRequest dto) {
        logger.info("Add quantity to product. dto={}", dto);
        try {
            Product product = productRepository.getById(dto.getProductId());
            product.addQuantity(dto.getAddedQuantity());
            productRepository.save(product);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @PatchMapping("/takeQuantity")
    public ResponseEntity<?> takeQuantity(@RequestBody ProductTakeQuantityRequest dto) {
        logger.info("Take quantity from product. dto={}", dto);
        try {
            Product product = productRepository.getById(dto.getProductId());
            product.take(dto.getTakeQuantity());
            productRepository.save(product);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<?> getById(@RequestParam("id") UUID id) {
        logger.info("Get product with id={}", id);
        try {
            Product product = productRepository.getById(id);
            return ResponseEntity.status(HttpStatus.OK).body(mapper.toProductResponse(product));
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getByFilter")
    public ResponseEntity<?> getByFilter(@RequestParam("page") int page,
                                         @RequestParam("size") int size,
                                         @RequestParam("userId") UUID userId,
                                         @RequestParam(value = "sort", required = false) String sortRule,
                                         @RequestParam(value = "onlyFridge", required = false) Boolean onlyFridge,
                                         @RequestParam(value = "category", required = false) String category,
                                         @RequestParam(value = "shops", required = false) List<String> shops,
                                         @RequestParam(value = "varieties", required = false) List<String> varieties,
                                         @RequestParam(value = "manufacturers", required = false) List<String> manufacturers,
                                         @RequestParam(value = "tags", required = false) List<String> tags) {
        logger.info("Get products by filter: page={}, size={}, userId={}, sortRule={}, onlyFridge={}, " +
                        "category={}, shops={}, varieties={}, manufacturers={}, tags={}",
                page, size, userId, sortRule, onlyFridge, category, shops, varieties, manufacturers, tags);

        try {
            ProductCriteria criteria = mapper.toProductCriteria(page, size, userId, sortRule, onlyFridge,
                    category, shops, varieties, manufacturers, tags);

            Page<ProductResponse> response = mapper.toProductsResponse(
                    productRepository.getProducts(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getTags")
    public ResponseEntity<?> getTags(@RequestParam("page") int page,
                                     @RequestParam("size") int size,
                                     @RequestParam("userId") UUID userId,
                                     @RequestParam(value = "productCategory", required = false) String productCategory) {
        logger.info("Get products tags by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        try {
            ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

            Page<TagRequestAndResponse> response = mapper.toTagsResponse(
                    productRepository.getTags(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getShops")
    public ResponseEntity<?> getShops(@RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam("userId") UUID userId,
                                      @RequestParam(value = "productCategory", required = false) String productCategory) {
        logger.info("Get products shops by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        try {
            ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

            Page<ShopResponse> response = mapper.toShopsResponse(
                    productRepository.getShops(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getVarieties")
    public ResponseEntity<?> getVarieties(@RequestParam("page") int page,
                                          @RequestParam("size") int size,
                                          @RequestParam("userId") UUID userId,
                                          @RequestParam(value = "productCategory", required = false) String productCategory) {
        logger.info("Get products varieties by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        try {
            ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

            Page<VarietyResponse> response = mapper.toVarietiesResponse(
                    productRepository.getVarieties(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getCategories")
    public ResponseEntity<?> getCategories(@RequestParam("page") int page,
                                           @RequestParam("size") int size,
                                           @RequestParam("userId") UUID userId) {
        logger.info("Get products categories by userId. page={}, size={}, userId={}", page, size, userId);

        try {
            ProductCategoryCriteria criteria = mapper.toProductCategoryCriteria(page, size, userId);

            Page<CategoryResponse> response = mapper.toCategoryResponse(
                    productRepository.getCategories(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

    @Transactional
    @GetMapping("/getManufacturers")
    public ResponseEntity<?> getManufacturers(@RequestParam("page") int page,
                                              @RequestParam("size") int size,
                                              @RequestParam("userId") UUID userId,
                                              @RequestParam(value = "productCategory", required = false) String productCategory) {
        logger.info("Get products categories by userId. page={}, size={}, userId={}, productCategory={}",
                page, size, userId, productCategory);

        try {
            ProductFieldCriteria criteria = mapper.toProductFieldCriteria(page, size, userId, productCategory);

            Page<ManufacturerResponse> response = mapper.toManufacturerResponse(
                    productRepository.getManufacturers(criteria)
            );

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(RuntimeException e) {
            return exceptionResolver.handle(e);
        }
    }

}

package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<DishResponse> add(@RequestBody DishRequest dto) {
        logger.info("Add new dish. dto={}", dto);

        return ResponseEntity.ok(new DishResponse());
    }

    @Transactional
    @PutMapping("/update")
    public ResponseEntity<DishResponse> update(@RequestBody DishRequest dto) {
        logger.info("Update dish. dto={}", dto);

        return ResponseEntity.ok(new DishResponse());
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<DishResponse> delete(@RequestParam("id") UUID id) {
        logger.info("Delete dish by id={}", id);

        return ResponseEntity.ok(new DishResponse());
    }

    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<DishResponse> getById(@RequestParam("id") UUID id) {
        logger.info("Get dish by id = {}", id);

        return ResponseEntity.ok(new DishResponse());
    }

    @Transactional
    @GetMapping("/getByName")
    public ResponseEntity<DishResponse> getByName(@RequestParam("name") String name,
                                                  @RequestParam("userId") UUID userId) {
        logger.info("Get dish by name={} and userId={}", name, userId);

        return ResponseEntity.ok(new DishResponse());
    }

    @Transactional
    @GetMapping("/getByFilter")
    public ResponseEntity<Page<DishForListResponse>> getByFilter(@RequestParam("page") int page,
                                                                 @RequestParam("size") int size,
                                                                 @RequestParam("sort") String sortRule,
                                                                 @RequestParam("userId") UUID userId,
                                                                 @RequestParam(value = "tags", required = false) List<String> tags) {
        logger.info("Get dishes for list by filter: page={}, size={}, sortRule={}, userId={}, tags={}",
                page, size, sortRule, userId, tags);

        return ResponseEntity.ok(Pageable.firstEmptyPage());
    }

    @Transactional
    @GetMapping("/getUnits")
    public ResponseEntity<List<DishUnitResponse>> getUnits(@RequestParam("userId") UUID userId) {
        logger.info("Get dish units by userId={}", userId);
        List<DishUnitResponse> response = List.of();
        return ResponseEntity.ok(response);
    }

    @Transactional
    @GetMapping("/getTags")
    public ResponseEntity<Page<String>> getTags(@RequestParam("page") int page,
                                             @RequestParam("size") int size,
                                             @RequestParam("userId") UUID userId) {
        logger.info("Get dishes tags by userId. page={}, size={}, userId={}", page, size, userId);

        return ResponseEntity.ok(Pageable.firstEmptyPage());
    }

    @Transactional
    @GetMapping("/pickProductsList")
    public ResponseEntity<DishProductsListResponse> pickProductsList(
            @RequestParam(value = "servingNumber", required = false) BigDecimal servingNumber,
            @RequestParam("ingredients") List<Integer> ingredients,
            @RequestParam("dishId") UUID dishId) {
        logger.info("Pick products list for dish. servingNumber={}, ingredients={}, dishId={}",
                servingNumber, ingredients, dishId);

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

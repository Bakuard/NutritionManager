package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.menus.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Hidden
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

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<MenuResponse> add(@RequestBody MenuRequest dto) {
        logger.info("Add new menu. dto={}", dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MenuResponse());
    }

    @Transactional
    @PutMapping("/update")
    public ResponseEntity<MenuResponse> update(@RequestBody MenuRequest dto) {
        logger.info("Update menu. dto={}", dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MenuResponse());
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<MenuResponse> delete(@RequestParam("id") UUID id) {
        logger.info("Delete menu with id={}", id);

        return ResponseEntity.status(HttpStatus.OK).body(new MenuResponse());
    }

    @Transactional
    @GetMapping("/getById")
    public ResponseEntity<MenuResponse> getById(@RequestParam("id") UUID id) {
        logger.info("Get menu by id={}", id);

        return ResponseEntity.status(HttpStatus.OK).body(new MenuResponse());
    }

    @Transactional
    @GetMapping("/getByName")
    public ResponseEntity<Page<MenuResponse>> getByName(@RequestParam("page") int page,
                                                        @RequestParam("size") int size,
                                                        @RequestParam("name") String name,
                                                        @RequestParam("userId") UUID userId) {
        logger.info("Get menu by name={} and userId={}", name, userId);

        return ResponseEntity.status(HttpStatus.OK).body(Pageable.firstEmptyPage());
    }

    @Transactional
    @GetMapping("/getByFilter")
    public ResponseEntity<Page<MenuForListResponse>> getByFilter(@RequestParam("page") int page,
                                                                 @RequestParam("size") int size,
                                                                 @RequestParam("sort") String sortRule,
                                                                 @RequestParam("userId") UUID userId) {
        logger.info("Get menus for list by filter: page={}, size={}, sortRule={}, userId={}",
                page, size, sortRule, userId);

        return ResponseEntity.status(HttpStatus.OK).body(Pageable.firstEmptyPage());
    }

    @Transactional
    @PutMapping("/pickProductsList")
    public ResponseEntity<MenuProductsListResponse> pickProductsList(@RequestBody MenuProductsListRequest dto) {
        logger.info("Pick products list for menu. dto={}", dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MenuProductsListResponse());
    }

}

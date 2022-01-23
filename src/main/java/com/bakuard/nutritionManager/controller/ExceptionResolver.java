package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.model.exceptions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class.getName());


    private DtoMapper mapper;

    @Autowired
    public ExceptionResolver(DtoMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handle(RuntimeException e) {
        logger.error("Unexpected exception", e);
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(mapper.toExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(value = ValidateException.class)
    public ResponseEntity<ExceptionResponse> handle(ValidateException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(mapper.toExceptionResponse(e, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<ExceptionResponse> handle(ServiceException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.FORBIDDEN).
                body(mapper.toExceptionResponse(e, HttpStatus.FORBIDDEN));
    }



}

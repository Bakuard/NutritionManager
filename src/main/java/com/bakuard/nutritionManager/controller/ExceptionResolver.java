package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.model.exceptions.*;

import com.bakuard.nutritionManager.model.exceptions.AuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class.getName());


    private DtoMapper mapper;

    @Autowired
    public ExceptionResolver(DtoMapper mapper) {
        this.mapper = mapper;
    }

    public ResponseEntity<ExceptionResponse> handle(RuntimeException e) {
        if(e instanceof UnknownSourceException ex) return handle(ex);
        else if(e instanceof ValidateException ex) return handle(ex);
        else if(e instanceof AuthException ex) return handle(ex);
        else if(e instanceof AbstractDomainException ex)  return handle(ex);
        else return defaultHandle(e);
    }

    private ResponseEntity<ExceptionResponse> defaultHandle(RuntimeException e) {
        logger.error("Unexpected exception", e);
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(mapper.toExceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<ExceptionResponse> handle(AbstractDomainException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(mapper.toExceptionResponse(e, HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<ExceptionResponse> handle(UnknownSourceException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND).
                body(mapper.toExceptionResponse(e, HttpStatus.NOT_FOUND));
    }

    private ResponseEntity<ExceptionResponse> handle(ValidateException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(mapper.toExceptionResponse(e, HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<ExceptionResponse> handle(AuthException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.
                status(HttpStatus.FORBIDDEN).
                body(mapper.toExceptionResponse(e, HttpStatus.FORBIDDEN));
    }

}

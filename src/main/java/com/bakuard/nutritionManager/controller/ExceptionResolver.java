package com.bakuard.nutritionManager.controller;

import com.bakuard.nutritionManager.dto.DtoMapper;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.validation.ValidateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionResolver.class.getName());


    private final DtoMapper mapper;

    @Autowired
    public ExceptionResolver(DtoMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handle(RuntimeException e) {
        logger.error("Unexpected exception", e);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internalServerError");

        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(response);
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponse> handle(MaxUploadSizeExceededException e) {
        logger.error("Fail to upload image. Incorrect size.", e);

        ExceptionResponse response = mapper.toExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "MaxUploadSizeExceededException");

        return ResponseEntity.
                status(HttpStatus.BAD_REQUEST).
                body(response);
    }

    @ExceptionHandler(value = ValidateException.class)
    public ResponseEntity<ExceptionResponse> handle(ValidateException e) {
        logger.error(e.getMessage(), e);

        ExceptionResponse response = mapper.toExceptionResponse(e);

        return ResponseEntity.
                status(HttpStatus.resolve(response.getHttpErrorCode())).
                body(response);
    }

}

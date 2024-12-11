package com.learning.individuals.auth.controller.errorhandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.learning.individuals.auth.exception.PasswordMissmatchException;
import com.learning.individuals.auth.exception.UserAlreadyExistsException;
import com.learning.individuals.auth.exception.UserNotAuthorisedException;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ApiError>> handleRuntimeException(RuntimeException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.baseBuilder().error(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).build()));
    }

    @ExceptionHandler(PasswordMissmatchException.class)
    public Mono<ResponseEntity<ApiError>> handlePasswordMissmatchException(PasswordMissmatchException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.baseBuilder().error(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).build()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ApiError>> handleUserUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.baseBuilder().error(ex.getMessage()).status(HttpStatus.BAD_REQUEST.value()).build()));
    }

    @ExceptionHandler(UserNotAuthorisedException.class)
    public Mono<ResponseEntity<ApiError>> handleUserNoAuthorisedException(UserNotAuthorisedException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.baseBuilder().error(ex.getMessage()).status(HttpStatus.UNAUTHORIZED.value()).build()));
    }





}
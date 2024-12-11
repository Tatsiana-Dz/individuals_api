package com.learning.individuals.auth.exception;

import org.springframework.http.HttpStatus;

public class PasswordMissmatchException extends RuntimeException {

    private final HttpStatus httpStatus;

    public PasswordMissmatchException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
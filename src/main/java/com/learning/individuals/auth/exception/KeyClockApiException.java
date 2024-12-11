package com.learning.individuals.auth.exception;

import org.springframework.http.HttpStatus;

public class KeyClockApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    public KeyClockApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
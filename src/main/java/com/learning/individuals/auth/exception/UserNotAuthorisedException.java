package com.learning.individuals.auth.exception;

import org.springframework.http.HttpStatus;

public class UserNotAuthorisedException extends RuntimeException {

    private final HttpStatus httpStatus;

    public UserNotAuthorisedException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}

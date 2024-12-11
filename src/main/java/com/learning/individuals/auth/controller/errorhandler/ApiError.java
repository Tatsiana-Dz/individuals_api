package com.learning.individuals.auth.controller.errorhandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true, builderMethodName = "baseBuilder")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError {

    private int status;
    private String error;
}

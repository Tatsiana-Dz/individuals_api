package com.learning.individuals.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 5, message = "Too short password")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = " Please, confirm your password ")
    @Size(min = 5, message = "Too short password confirmation")
    @JsonProperty("confirm_password")
    private String confirmPassword;
}

//tests
//emulate keyclock instance, BDD , 2 tests for controllers
//services - unit tests

package com.learning.individuals.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.service.AuthService;
import com.learning.individuals.auth.dto.LoginRequest;
import com.learning.individuals.auth.dto.RefreshTokenRequest;
import com.learning.individuals.auth.dto.UserRegistrationRequest;
import com.learning.individuals.auth.dto.TokenResponse;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationControllerV1 {

    private final AuthService authService;

    @PostMapping("/registration")
    public Mono<TokenResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public Mono<TokenResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }

    @PostMapping("/refresh-token")
    public Mono<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @GetMapping("/me")
    public Mono<UserInfoResponse> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        return authService.getUserInfo(jwt.getSubject(), jwt.getTokenValue());
    }

}
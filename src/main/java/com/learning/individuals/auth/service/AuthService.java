package com.learning.individuals.auth.service;

import com.learning.individuals.auth.dto.LoginRequest;
import com.learning.individuals.auth.dto.RefreshTokenRequest;
import com.learning.individuals.auth.dto.TokenResponse;
import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.dto.UserRegistrationRequest;

import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<TokenResponse> registerUser(UserRegistrationRequest request);

    Mono<TokenResponse> loginUser(LoginRequest request);

    Mono<TokenResponse> refreshToken(RefreshTokenRequest request);

    Mono<UserInfoResponse> getUserInfo(String userId, String token);

}

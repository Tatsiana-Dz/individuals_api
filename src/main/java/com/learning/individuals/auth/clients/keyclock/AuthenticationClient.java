package com.learning.individuals.auth.clients.keyclock;

import com.learning.individuals.auth.dto.TokenResponse;
import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.dto.UserRepresentation;

import reactor.core.publisher.Mono;

public interface AuthenticationClient {

    Mono<TokenResponse> createUser(UserRepresentation user);

    Mono<TokenResponse> authenticateUser(String username, String password);

    Mono<TokenResponse> refreshToken(String refreshToken);

    Mono<UserInfoResponse> getUserInfo(String userId, String token);
}

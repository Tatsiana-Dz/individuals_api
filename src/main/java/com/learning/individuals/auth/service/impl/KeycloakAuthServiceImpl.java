package com.learning.individuals.auth.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.learning.individuals.auth.dto.CredentialRepresentation;
import com.learning.individuals.auth.clients.keyclock.AuthenticationClient;
import com.learning.individuals.auth.dto.UserRepresentation;
import com.learning.individuals.auth.dto.LoginRequest;
import com.learning.individuals.auth.dto.RefreshTokenRequest;
import com.learning.individuals.auth.dto.UserRegistrationRequest;
import com.learning.individuals.auth.dto.TokenResponse;
import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.exception.PasswordMissmatchException;
import com.learning.individuals.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KeycloakAuthServiceImpl implements AuthService {

    private final AuthenticationClient authenticationClient;

    @Override
    public Mono<TokenResponse> registerUser(UserRegistrationRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMissmatchException(HttpStatus.BAD_REQUEST, "Password does not match confirmation password");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return authenticationClient
                .createUser(user).then(authenticationClient.authenticateUser(request.getEmail(), request.getPassword()));
    }

    @Override
    public Mono<TokenResponse> loginUser(LoginRequest request) {
        return authenticationClient.authenticateUser(request.getEmail(), request.getPassword());
    }

    @Override
    public Mono<TokenResponse> refreshToken(RefreshTokenRequest request) {
        return authenticationClient.refreshToken(request.getRefresh_token());
    }

    @Override
    public Mono<UserInfoResponse> getUserInfo(String userId, String token) {
        return authenticationClient.getUserInfo(userId, token);
    }
}
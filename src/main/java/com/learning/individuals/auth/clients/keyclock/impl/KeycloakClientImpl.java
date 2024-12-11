package com.learning.individuals.auth.clients.keyclock.impl;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.learning.individuals.auth.clients.keyclock.AuthenticationClient;
import com.learning.individuals.auth.clients.keyclock.config.KeyClockClientTokenProvider;
import com.learning.individuals.auth.dto.TokenResponse;
import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.dto.UserRepresentation;
import com.learning.individuals.auth.exception.KeyClockApiException;
import com.learning.individuals.auth.exception.UserAlreadyExistsException;
import com.learning.individuals.auth.exception.UserNotAuthorisedException;
import com.learning.individuals.auth.exception.UserNotFoundException;
import com.learning.individuals.auth.mapper.KeycloakResponseMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientImpl implements AuthenticationClient {

    private static final String API_REQUEST_FAILED_ERROR_MESSAGE = "Request not sent";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_TYPE = "grant_type";
    private final WebClient webClient;

    private final KeyClockClientTokenProvider keyClockClientTokenProvider;
    private final KeycloakResponseMapper mapper;


    @Value("${spring.security.oauth2.client.registration.keyCloakApi}")
    private String clientRegistration;

    @Value("${spring.security.oauth2.client.registration.keyCloakApi.client-id}")
    private String clientPrincipal;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.credentials.client}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Getter
    enum ScopedClients {

        KEYCLOAK_API("keyCloakApi");

        private final String registrationId;

        ScopedClients(String registrationId) {
            this.registrationId = registrationId;
        }
    }

    @Override
    public Mono<TokenResponse> createUser(UserRepresentation user) {
        return keyClockClientTokenProvider
                .getAccessToken(ScopedClients.KEYCLOAK_API.getRegistrationId())
                .flatMap(accessToken -> webClient
                        .post()
                        .uri("/admin/realms/" + realm + "/users/")
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(user)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, this::handleCreateUserClientError)
                        .onStatus(HttpStatusCode::isError, handleStatusError())
                        .bodyToMono(TokenResponse.class));
    }

    private Mono<? extends Throwable> handleCreateUserClientError(ClientResponse clientResponse) {
        HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
        if (status == HttpStatus.UNAUTHORIZED) {
            return Mono.error(new KeyClockApiException(HttpStatus.UNAUTHORIZED, "Can't authenticate to keycloak."));
        } else if (HttpStatus.CONFLICT.equals(status)) {
            return Mono.error(
                    new UserAlreadyExistsException(HttpStatus.CONFLICT, "User with this email already exists"));
        } else {
            return clientResponse
                    .bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new KeyClockApiException(status, errorBody)));
        }
    }

    @Override
    public Mono<TokenResponse> authenticateUser(String username, String password) {
        return webClient
                .post()
                .uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData(GRANT_TYPE, AuthorizationGrantType.PASSWORD.getValue())
                        .with(CLIENT_ID, clientId)
                        .with(CLIENT_SECRET, clientSecret)
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleLoginUserClientError)
                .onStatus(HttpStatusCode::isError, handleStatusError())
                .bodyToMono(TokenResponse.class);
    }

    private Mono<? extends Throwable> handleLoginUserClientError(ClientResponse clientResponse) {
        HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
        if (status == HttpStatus.UNAUTHORIZED) {
            return Mono.error(new UserNotAuthorisedException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        } else {
            return clientResponse
                    .bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new KeyClockApiException(status, errorBody)));
        }
    }

    @Override
    public Mono<TokenResponse> refreshToken(String refreshToken) {
        return webClient
                .post()
                .uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData(GRANT_TYPE, AuthorizationGrantType.REFRESH_TOKEN.getValue())
                        .with(CLIENT_ID, clientId)
                        .with(CLIENT_SECRET, clientSecret)
                        .with(REFRESH_TOKEN, refreshToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleRefreshTokenClientError)
                .onStatus(HttpStatusCode::isError, handleStatusError())
                .bodyToMono(TokenResponse.class);
    }

    private Mono<? extends Throwable> handleRefreshTokenClientError(ClientResponse clientResponse) {
        HttpStatus status = HttpStatus.valueOf(clientResponse.statusCode().value());
        if (status == HttpStatus.UNAUTHORIZED) {
            return Mono.error(
                    new UserNotAuthorisedException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));
        } else {
            return clientResponse
                    .bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new KeyClockApiException(status, errorBody)));
        }
    }

    @Override
    public Mono<UserInfoResponse> getUserInfo(String userId, String token) {
        return webClient
                .get()
                .uri("/admin/realms/" + realm + "/users/" + userId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleGetUserClientError)
                .onStatus(HttpStatusCode::isError, handleStatusError())
                .bodyToMono(UserRepresentation.class)
                .map(mapper::toUserInfoResponse);
    }

    private Mono<? extends Throwable> handleGetUserClientError(ClientResponse response) {
        HttpStatus status = HttpStatus.valueOf(response.statusCode().value());

        if (status == HttpStatus.UNAUTHORIZED) {
            return Mono.error(
                    new UserNotAuthorisedException(HttpStatus.UNAUTHORIZED, "Invalid or expired access token"));
        } else if (status == HttpStatus.NOT_FOUND) {
            return Mono.error(new UserNotFoundException(HttpStatus.NOT_FOUND, "User not found"));
        } else {
            return response
                    .bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new KeyClockApiException(status, errorBody)));
        }
    }

    private Function<ClientResponse, Mono<? extends Throwable>> handleStatusError() {
        return response -> {
            HttpStatus httpStatus = HttpStatus.valueOf(response.statusCode().value());
            log.error("{} HttpStatus: {}", API_REQUEST_FAILED_ERROR_MESSAGE, httpStatus.value());
            log.error(webClient.toString());
            return Mono.error(new KeyClockApiException(httpStatus, httpStatus.getReasonPhrase()));
        };
    }

}

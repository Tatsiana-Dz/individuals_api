package com.learning.individuals.auth.clients.keyclock.config;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
@RequiredArgsConstructor
@Component
public class KeyClockClientTokenProvider {

    private final ReactiveOAuth2AuthorizedClientManager keyCloakApiAuthorizedClientManager;
    private final Mono<OAuth2AuthorizeRequest> keyCloakApi;

    public Mono<String> getAccessToken(String registrationName) {
        return keyCloakApi
                .flatMap(authorizeRequest -> keyCloakApiAuthorizedClientManager
                        .authorize(authorizeRequest)
                        .<String>handle((authorizedClient, sink) -> {
                            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                            if (accessToken != null) {
                                sink.next(accessToken.getTokenValue());
                            } else {
                                log.error("Could not get access token for client {}", authorizeRequest.getPrincipal());
                                sink.error(new RuntimeException("Unable to authenticate and authorize keyCloak client "
                                        + authorizeRequest.getPrincipal()));
                            }
                        }))
                .doOnError(error -> log.error("Error fetching access token for registration {}: {}", registrationName,
                        error.getMessage()));
    }
}
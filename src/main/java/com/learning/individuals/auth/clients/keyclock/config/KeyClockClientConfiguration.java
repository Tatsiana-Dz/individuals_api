package com.learning.individuals.auth.clients.keyclock.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
public class KeyClockClientConfiguration {

    @Value("${spring.security.oauth2.client.registration.keyCloakApi}")
    private String clientRegistration;

    @Value("${spring.security.oauth2.client.registration.keyCloakApi.client-id}")
    private String clientPrincipal;

    @Value("${keycloak.auth-server-url}")
    private String keyClockApiUrl;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager keyCloakReactiveAuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService) {
        return new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository,
                reactiveOAuth2AuthorizedClientService);
    }

    @Bean
    public Mono<OAuth2AuthorizeRequest> keyCloakApi() {
        return Mono.just(
                OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistration).principal(clientPrincipal).build());
    }

    @Bean("keyClockWebClient")
    WebClient keyClockWebClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                authorizedClientManager);
        oauth2Client.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder().baseUrl(keyClockApiUrl).filter(oauth2Client).defaultHeaders(httpHeaders -> {
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        }).build();
    }
}

package com.learning.individuals.controller;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import com.learning.individuals.auth.config.SecurityConfig;
import com.learning.individuals.auth.dto.LoginRequest;
import com.learning.individuals.auth.dto.RefreshTokenRequest;
import com.learning.individuals.auth.dto.UserRegistrationRequest;
import com.learning.individuals.config.AbstractRestControllerBaseTest;

import reactor.core.publisher.Mono;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@Import({SecurityConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest extends AbstractRestControllerBaseTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    private WebTestClient webTestClient;

//    @Autowired
//    private WebApplicationContext context;

    @BeforeEach
    public void setUp() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
//        this.webTestClient = WebTestClient.bindToApplicationContext(context).build();
        //        developerRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Given valid registration details, when registering a new user, then the user is registered successfully")
    void registration_User_Successful() {
        // Given
        UserRegistrationRequest registrationRequest = UserRegistrationRequest
                .builder()
                .email("testuser@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        // When
        WebTestClient.ResponseSpec response = webTestClient
                .post()
                .uri("/v1/auth/registration")
                .body(Mono.just(registrationRequest), UserRegistrationRequest.class)
                .exchange();

        // Then
        response
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.access_token")
                .isNotEmpty()
                .jsonPath("$.refresh_token")
                .isNotEmpty()
                .jsonPath("$.token_type")
                .isEqualTo("Bearer");
    }

        @Test
        @Order(2)
        @DisplayName("Given valid login credentials, when logging in, then an access token is returned")
        void auth_User_Successful() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            // When
            WebTestClient.ResponseSpec response = webTestClient.post()
                    .uri("/v1/auth/login")
                    .body(Mono.just(loginRequest), LoginRequest.class)
                    .exchange();

            // Then
            response.expectStatus().isOk()
                    .expectBody()
                    .consumeWith(System.out::println)
                    .jsonPath("$.access_token").isNotEmpty()
                    .jsonPath("$.expires_in").isNotEmpty()
                    .jsonPath("$.refresh_token").isNotEmpty()
                    .jsonPath("$.token_type").isEqualTo("Bearer");
        }

        @Test
        @Order(3)
        @DisplayName("Given a valid access token, when retrieving user info, then user details are returned")
        void retrieve_User_Info_Successful() {
            // When
            // Obtain JWT token from login response
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("testuser@example.com")
                    .password("password123")
                    .build();

            String jwtToken = webTestClient.post()
                    .uri("/v1/auth/login")
                    .body(Mono.just(loginRequest), LoginRequest.class)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(Map.class) // Adjust type to match response body
                    .getResponseBody()
                    .blockFirst()
                    .get("access_token").toString(); // Adjust key based on your response

            WebTestClient.ResponseSpec response = webTestClient.get()
                    .uri("/v1/auth/me")
                    .headers(headers -> headers.setBearerAuth(jwtToken))
                    .exchange();

            // Then
            response.expectStatus().isOk()
                    .expectBody()
                    .consumeWith(System.out::println)
                    .jsonPath("$.email").isEqualTo("testuser@example.com")
                    .jsonPath("$.id").isNotEmpty();
        }

    @Test
    @Order(4)
    @DisplayName("Given an expired refresh token, when refreshing the token, then an error is returned")
    void refresh_Token_Failure() {
        // Given
        RefreshTokenRequest refreshTokenRequest = RefreshTokenRequest
                .builder()
                .refresh_token("expired-refresh-token")
                .build();

        // When
        WebTestClient.ResponseSpec response = webTestClient
                .post()
                .uri("/v1/auth/refresh-token")
                .body(Mono.just(refreshTokenRequest), RefreshTokenRequest.class)
                .exchange();

        // Then
        response
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("Invalid or expired refresh token");
    }

}
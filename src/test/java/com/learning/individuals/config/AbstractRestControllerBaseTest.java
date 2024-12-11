package com.learning.individuals.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public abstract class AbstractRestControllerBaseTest {

    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0.6";

    @Container
    static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(KEYCLOAK_IMAGE));

    static {

        String importFileInContainer = "/opt/keycloak/data/import/" + FilenameUtils.getName("realm-config.json");

        container.withExposedPorts(8080)
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withCopyFileToContainer(MountableFile.forClasspathResource("/realm-config.json", 0644), importFileInContainer)
                .withEnv("KEYCLOAK_IMPORT", "/opt/keycloak/data/import/realm-config.json")
                .withCommand("start-dev", "--import-realm")
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forHttp("/").forStatusCode(200));
        container.waitingFor(org.testcontainers.containers.wait.strategy.Wait.forHttp("/realms/individuals/.well-known/openid-configuration").forStatusCode(200));
        container.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {

        String containerHost = container.getHost();
        Integer containerPort = container.getMappedPort(8080);
        String containerUrl = "http://" + containerHost + ":" + containerPort;

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> containerUrl + "/realms/individuals");
        registry.add("spring.security.oauth2.client.provider.keyCloakApi.issuer-uri",
                () -> containerUrl + "/realms/individuals");
        registry.add("spring.security.oauth2.client.provider.keyCloakApi.token-uri",
                () -> containerUrl + "/realms/individuals/protocol/openid-connect/token");

        registry.add("keycloak.auth-server-url", container::getHost);

    }
}
package com.learning.individuals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebFlux
public class IndividualsApplication {
    public static void main(String[] args) {
        SpringApplication.run(IndividualsApplication.class, args);
    }
}

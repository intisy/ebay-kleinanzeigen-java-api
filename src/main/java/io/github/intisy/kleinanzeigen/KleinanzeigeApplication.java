package io.github.intisy.kleinanzeigen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;

/**
 * Spring Boot entry point for the eBay Kleinanzeigen Java API.
 * Jackson is excluded; Gson is used exclusively for JSON serialization.
 *
 * @author Finn Birich
 */
@SpringBootApplication(exclude = {JacksonAutoConfiguration.class})
public class KleinanzeigeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KleinanzeigeApplication.class, args);
    }
}

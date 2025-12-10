package com.b2g.recomendationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.b2g.recommendationservice", // il tuo servizio
        "com.b2g.shared"                 // se vuoi usare shared
})
public class RecomendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecomendationServiceApplication.class, args);
    }

}

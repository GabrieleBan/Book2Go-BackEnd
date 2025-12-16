package com.b2g.recomendationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.b2g.recomendationservice",
        "com.b2g.shared"
})
public class RecomendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecomendationServiceApplication.class, args);
    }

}

package com.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MerchantApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MerchantApiApplication.class, args);
        System.out.println("========================================");
        System.out.println("íº€ Merchant API Started Successfully!");
        System.out.println("í³– Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("í³„ OpenAPI JSON: http://localhost:8080/v3/api-docs");
        System.out.println("í²š Health Check: http://localhost:8080/actuator/health");
        System.out.println("========================================");
    }
}

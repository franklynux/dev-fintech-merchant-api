package com.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Merchant API Application
 * Main entry point for the Merchant Management API
 */
@SpringBootApplication
public class MerchantApiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MerchantApiApplication.class, args);
        printStartupBanner();
    }
    
    private static void printStartupBanner() {
        System.out.println("========================================");
        System.out.println("Merchant API Started Successfully!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("OpenAPI JSON: http://localhost:8080/v3/api-docs");
        System.out.println("Health Check: http://localhost:8080/actuator/health");
        System.out.println("========================================");
    }
}
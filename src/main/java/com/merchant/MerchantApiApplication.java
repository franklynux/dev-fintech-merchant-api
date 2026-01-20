package com.merchant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Merchant API Application
 * Main entry point for the Merchant Management API
 */
@SpringBootApplication
public class MerchantApiApplication implements CommandLineRunner {

    @Value("${base.path}")
    private String basePath;
    
    public static void main(String[] args) {
        SpringApplication.run(MerchantApiApplication.class, args);
    }
    
    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("Merchant API Started Successfully!");
        System.out.println("Swagger UI: " + basePath + "swagger-ui.html");
        System.out.println("OpenAPI JSON: " + basePath + "v3/api-docs");
        System.out.println("Health Check: " + basePath + "actuator/health");
        System.out.println("========================================");
    }
}
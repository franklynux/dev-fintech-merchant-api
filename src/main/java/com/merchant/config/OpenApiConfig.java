package com.merchant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI merchantApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Merchant API")
                        .description("Containerized Merchant Management API\n\n" +
                                "Features:\n" +
                                "- Transaction management\n" +
                                "- Rate limiting (100 req/min)\n" +
                                "- Fully containerized")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Olufemi Michael")
                                .email("support@merchant-api.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Docker Container")));
    }
}

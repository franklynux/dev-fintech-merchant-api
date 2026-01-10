package com.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyCreationResponse {
    private String id;
    
    @JsonProperty("api_key")
    private String key; // Shown ONCE only
    
    private String name;
    private String prefix;
    
    @JsonProperty("last_four")
    private String lastFour;
    
    private String environment;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
    
    private String message;
}

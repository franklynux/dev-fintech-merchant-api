package com.merchant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API Key Entity - SECURE IMPLEMENTATION
 * 
 * SECURITY NOTES:
 * - Raw key NEVER stored in database
 * - Only SHA-256 hash stored
 * - Last 4 characters stored for display
 * - Raw key shown ONCE on creation
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_key_hash", columnList = "keyHash"),
    @Index(name = "idx_merchant_id", columnList = "merchantId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * SHA-256 hash of the API key
     */
    @Column(nullable = false, unique = true, length = 64)
    private String keyHash;
    
    /**
     * Key prefix (mk_live_ or mk_test_)
     */
    @Column(nullable = false, length = 10)
    private String prefix;
    
    /**
     * Last 4 characters for display
     */
    @Column(nullable = false, length = 4)
    private String lastFour;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Boolean active;
    
    /**
     * Environment: live or test
     */
    @Column(nullable = false)
    private String environment;
    
    /**
     * Scopes/permissions (comma-separated)
     */
    private String scopes;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime lastUsedAt;
    
    private Integer usageCount;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        active = true;
        usageCount = 0;
    }
}

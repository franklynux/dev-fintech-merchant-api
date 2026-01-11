package com.merchant.service;

import com.merchant.dto.ApiKeyCreationResponse;
import com.merchant.model.ApiKey;
import com.merchant.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {
    
    private final ApiKeyRepository apiKeyRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();
    
    private static final String LIVE_PREFIX = "mk_live_";
    private static final String TEST_PREFIX = "mk_test_";
    
    /**
     * Generate a new API key
     * Returns raw key ONLY ONCE - never retrievable again
     */
    @Transactional
    public ApiKeyCreationResponse generateApiKey(String merchantId, String name, 
                                                  boolean isTest, Integer validityDays) {
        // Generate secure random key
        String prefix = isTest ? TEST_PREFIX : LIVE_PREFIX;
        String randomPart = generateRandomString(32);
        String rawKey = prefix + randomPart;
        
        // Hash for storage
        String keyHash = hashApiKey(rawKey);
        String lastFour = randomPart.substring(randomPart.length() - 4);
        
        ApiKey apiKey = ApiKey.builder()
                .keyHash(keyHash)
                .prefix(prefix)
                .lastFour(lastFour)
                .merchantId(merchantId)
                .name(name)
                .environment(isTest ? "test" : "live")
                .active(true)
                .expiresAt(validityDays != null ? 
                    LocalDateTime.now().plusDays(validityDays) : null)
                .scopes("payments:write,payments:read,refunds:write")
                .build();
        
        ApiKey saved = apiKeyRepository.save(apiKey);
        
        log.info("Generated {} API key for merchant: {}", 
                isTest ? "test" : "live", merchantId);
        
        return ApiKeyCreationResponse.builder()
                .id(saved.getId())
                .key(rawKey) // RAW KEY - shown once only
                .name(saved.getName())
                .prefix(saved.getPrefix())
                .lastFour(saved.getLastFour())
                .environment(saved.getEnvironment())
                .createdAt(saved.getCreatedAt())
                .expiresAt(saved.getExpiresAt())
                .message("⚠️ SAVE THIS KEY SECURELY. It will never be shown again.")
                .build();
    }
    
    /**
     * Validate API key
     */
    public boolean validateApiKey(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) {
            return false;
        }
        
        String keyHash = hashApiKey(rawKey);
        
        return apiKeyRepository.findByKeyHashAndActive(keyHash, true)
                .map(apiKey -> {
                    // Check expiration
                    if (apiKey.getExpiresAt() != null && 
                        apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
                        log.warn("Expired API key used: {}", apiKey.getId());
                        return false;
                    }
                    
                    // Update usage stats (async)
                    updateUsageAsync(apiKey);
                    
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Get merchant ID from API key
     */
    public String getMerchantIdFromKey(String rawKey) {
        String keyHash = hashApiKey(rawKey);
        return apiKeyRepository.findByKeyHashAndActive(keyHash, true)
                .map(ApiKey::getMerchantId)
                .orElse(null);
    }
    
    /**
     * Check if API key is for sandbox environment
     */
    public boolean isSandboxKey(String rawKey) {
        String keyHash = hashApiKey(rawKey);
        return apiKeyRepository.findByKeyHashAndActive(keyHash, true)
                .map(key -> "test".equals(key.getEnvironment()))
                .orElse(false);
    }
    
    private String generateRandomString(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    
    private String hashApiKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    private void updateUsageAsync(ApiKey apiKey) {
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKey.setUsageCount(apiKey.getUsageCount() + 1);
        apiKeyRepository.save(apiKey);
    }
}

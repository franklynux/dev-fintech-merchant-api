package com.merchant.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit Logging Service
 * 
 * Provides structured audit logging for:
 * - Payment transactions
 * - API key operations
 * - Authentication events
 * - Sensitive operations
 * 
 * Logs are JSON-formatted for easy parsing and analysis
 */
@Service
@Slf4j
public class AuditService {
    
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");
    
    public void logPaymentCreated(String paymentId, String merchantId, String amount) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "payment.created");
        auditData.put("payment_id", paymentId);
        auditData.put("merchant_id", merchantId);
        auditData.put("amount", amount);
        auditData.put("timestamp", LocalDateTime.now());
        
        MDC.put("payment_id", paymentId);
        MDC.put("merchant_id", merchantId);
        
        AUDIT_LOG.info("Payment created: {}", auditData);
        
        MDC.clear();
    }
    
    public void logPaymentCompleted(String paymentId, String merchantId) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "payment.completed");
        auditData.put("payment_id", paymentId);
        auditData.put("merchant_id", merchantId);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.info("Payment completed: {}", auditData);
    }
    
    public void logPaymentFailed(String paymentId, String merchantId, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "payment.failed");
        auditData.put("payment_id", paymentId);
        auditData.put("merchant_id", merchantId);
        auditData.put("failure_reason", reason);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.warn("Payment failed: {}", auditData);
    }
    
    public void logRefund(String paymentId, String merchantId, String amount) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "payment.refunded");
        auditData.put("payment_id", paymentId);
        auditData.put("merchant_id", merchantId);
        auditData.put("refund_amount", amount);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.info("Refund processed: {}", auditData);
    }
    
    public void logApiKeyCreated(String apiKeyId, String merchantId, String environment) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "api_key.created");
        auditData.put("api_key_id", apiKeyId);
        auditData.put("merchant_id", merchantId);
        auditData.put("environment", environment);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.info("API key created: {}", auditData);
    }
    
    public void logApiKeyRevoked(String apiKeyId, String merchantId) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "api_key.revoked");
        auditData.put("api_key_id", apiKeyId);
        auditData.put("merchant_id", merchantId);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.warn("API key revoked: {}", auditData);
    }
    
    public void logAuthenticationFailure(String ipAddress, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "auth.failed");
        auditData.put("ip_address", ipAddress);
        auditData.put("reason", reason);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.warn("Authentication failed: {}", auditData);
    }
    
    public void logRateLimitExceeded(String clientId, String endpoint) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event", "rate_limit.exceeded");
        auditData.put("client_id", clientId);
        auditData.put("endpoint", endpoint);
        auditData.put("timestamp", LocalDateTime.now());
        
        AUDIT_LOG.warn("Rate limit exceeded: {}", auditData);
    }
}

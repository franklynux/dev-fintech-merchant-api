package com.merchant.model;

import com.merchant.enums.PaymentMethod;
import com.merchant.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - PCI Compliant
 * 
 * CRITICAL: No actual card data stored here
 * Only tokenized references and last 4 digits
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_merchant_id", columnList = "merchantId"),
    @Index(name = "idx_payment_token", columnList = "paymentToken"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    /**
     * Tokenized payment method reference
     * NEVER store actual card numbers
     */
    @Column(nullable = false, unique = true)
    private String paymentToken;
    
    /**
     * Last 4 digits for display purposes only
     */
    @Column(length = 4)
    private String lastFour;
    
    /**
     * Card brand (Visa, Mastercard, etc.)
     */
    private String cardBrand;
    
    @Column(length = 500)
    private String description;
    
    private String customerEmail;
    
    private String customerName;
    
    /**
     * External transaction reference
     */
    private String externalReference;
    
    /**
     * Sandbox mode flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean sandbox = false;
    
    /**
     * Metadata for additional info (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime authorizedAt;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime failedAt;
    
    /**
     * Failure reason if status is FAILED
     */
    private String failureReason;
    
    /**
     * Refund information
     */
    private BigDecimal refundedAmount;
    
    private LocalDateTime refundedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sandbox == null) {
            sandbox = false;
        }
    }
}

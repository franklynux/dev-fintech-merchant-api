package com.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.merchant.enums.PaymentMethod;
import com.merchant.enums.PaymentStatus;
import com.merchant.model.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private String id;
    
    @JsonProperty("merchant_id")
    private String merchantId;
    
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;
    
    /**
     * MASKED card display (e.g., "•••• 4242")
     */
    @JsonProperty("card_display")
    private String cardDisplay;
    
    @JsonProperty("card_brand")
    private String cardBrand;
    
    private String description;
    
    @JsonProperty("customer_email")
    private String customerEmail;
    
    @JsonProperty("external_reference")
    private String externalReference;
    
    private Boolean sandbox;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
    
    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .merchantId(payment.getMerchantId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .cardDisplay("•••• " + payment.getLastFour())
                .cardBrand(payment.getCardBrand())
                .description(payment.getDescription())
                .customerEmail(payment.getCustomerEmail())
                .externalReference(payment.getExternalReference())
                .sandbox(payment.getSandbox())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}

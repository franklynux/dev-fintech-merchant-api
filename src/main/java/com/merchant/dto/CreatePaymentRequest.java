package com.merchant.dto;

import com.merchant.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
    
    @NotNull
    private PaymentMethod paymentMethod;
    
    /**
     * TOKENIZED payment method
     * NEVER send raw card numbers
     */
    @NotBlank
    private String paymentToken;
    
    private String description;
    
    @Email
    private String customerEmail;
    
    private String customerName;
    
    private String externalReference;
    
    private String metadata;
}

package com.merchant.dto;

import com.merchant.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentGatewayRequest {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String paymentToken;
    private String cardBrand;
    private String walletProvider;
    private String customerEmail;
    private String merchantId;
    private boolean sandbox;
}

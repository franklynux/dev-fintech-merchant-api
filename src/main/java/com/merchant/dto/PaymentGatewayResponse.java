package com.merchant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private boolean success;
    private String status;
    private String transactionId;
    private String gatewayName;
    private String message;
    private String errorMessage;
    private String errorCode;
}

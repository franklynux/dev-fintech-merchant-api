package com.merchant.gateway;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;

public interface PaymentGateway {
    
    PaymentGatewayResponse processPayment(PaymentGatewayRequest request);
    
    String getGatewayName();
    
    boolean supportsPaymentMethod(String paymentMethod);
}

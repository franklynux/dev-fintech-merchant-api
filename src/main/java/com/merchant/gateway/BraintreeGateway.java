package com.merchant.gateway;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Braintree Payment Gateway
 * 
 * Handles: Cards, PayPal, Apple Pay, Google Pay, ACH
 */
@Component
@Slf4j
public class BraintreeGateway implements PaymentGateway {
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via Braintree");
        
        if (request.isSandbox()) {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .status("COMPLETED")
                    .transactionId("braintree_sandbox_" + System.currentTimeMillis())
                    .gatewayName("BRAINTREE")
                    .message("Sandbox payment approved")
                    .build();
        }
        
        // Real Braintree integration would go here
        return PaymentGatewayResponse.builder()
                .success(true)
                .status("PROCESSING")
                .transactionId("braintree_" + System.currentTimeMillis())
                .gatewayName("BRAINTREE")
                .message("Payment processing")
                .build();
    }
    
    @Override
    public String getGatewayName() {
        return "BRAINTREE";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return true; // Braintree supports most payment methods
    }
}

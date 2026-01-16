package com.merchant.gateway;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adyen Payment Gateway
 * 
 * Handles: Global payments, bank transfers, wire transfers
 */
@Component
@Slf4j
public class AdyenGateway implements PaymentGateway {
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via Adyen");
        
        if (request.isSandbox()) {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .status("COMPLETED")
                    .transactionId("adyen_sandbox_" + System.currentTimeMillis())
                    .gatewayName("ADYEN")
                    .message("Sandbox payment approved")
                    .build();
        }
        
        // Real Adyen integration would go here
        return PaymentGatewayResponse.builder()
                .success(true)
                .status("PROCESSING")
                .transactionId("adyen_" + System.currentTimeMillis())
                .gatewayName("ADYEN")
                .message("Payment processing")
                .build();
    }
    
    @Override
    public String getGatewayName() {
        return "ADYEN";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return true; // Adyen supports most payment methods globally
    }
}

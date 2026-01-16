package com.merchant.gateway;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PayPal Payment Gateway
 * 
 * Handles: PayPal wallet, Venmo
 */
@Component
@Slf4j
public class PayPalGateway implements PaymentGateway {
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via PayPal");
        
        if (request.isSandbox()) {
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .status("COMPLETED")
                    .transactionId("paypal_sandbox_" + System.currentTimeMillis())
                    .gatewayName("PAYPAL")
                    .message("Sandbox payment approved")
                    .build();
        }
        
        // Real PayPal integration would go here
        return PaymentGatewayResponse.builder()
                .success(true)
                .status("PROCESSING")
                .transactionId("paypal_" + System.currentTimeMillis())
                .gatewayName("PAYPAL")
                .message("Payment processing")
                .build();
    }
    
    @Override
    public String getGatewayName() {
        return "PAYPAL";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return "DIGITAL_WALLET".equals(paymentMethod);
    }
}

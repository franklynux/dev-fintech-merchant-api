package com.merchant.gateway;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Stripe Payment Gateway
 * 
 * Handles: Credit cards, Debit cards, Apple Pay, Google Pay
 */
@Component
@Slf4j
public class StripeGateway implements PaymentGateway {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String STRIPE_API_URL = "https://api.stripe.com/v1";
    
    @Override
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via Stripe");
        
        try {
            // In production, integrate with Stripe API
            // For now, simulate successful payment
            
            if (request.isSandbox()) {
                return simulateSandboxPayment(request);
            }
            
            // Real Stripe API call would go here:
            // 1. Create PaymentIntent
            // 2. Confirm payment
            // 3. Handle response
            
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .status("PROCESSING")
                    .transactionId("stripe_" + System.currentTimeMillis())
                    .gatewayName("STRIPE")
                    .message("Payment processing")
                    .build();
            
        } catch (Exception e) {
            log.error("Stripe processing failed: {}", e.getMessage());
            return buildErrorResponse(e.getMessage());
        }
    }
    
    @Override
    public String getGatewayName() {
        return "STRIPE";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return paymentMethod.matches("CREDIT_CARD|DEBIT_CARD|APPLE_PAY|GOOGLE_PAY");
    }
    
    private PaymentGatewayResponse simulateSandboxPayment(PaymentGatewayRequest request) {
        // Sandbox mode - auto-approve
        return PaymentGatewayResponse.builder()
                .success(true)
                .status("COMPLETED")
                .transactionId("stripe_sandbox_" + System.currentTimeMillis())
                .gatewayName("STRIPE")
                .message("Sandbox payment approved")
                .build();
    }
    
    private PaymentGatewayResponse buildErrorResponse(String error) {
        return PaymentGatewayResponse.builder()
                .success(false)
                .status("FAILED")
                .gatewayName("STRIPE")
                .errorMessage(error)
                .build();
    }
}

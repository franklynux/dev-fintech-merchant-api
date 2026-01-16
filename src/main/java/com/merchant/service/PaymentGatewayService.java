package com.merchant.service;

import com.merchant.dto.PaymentGatewayRequest;
import com.merchant.dto.PaymentGatewayResponse;
import com.merchant.enums.PaymentMethod;
import com.merchant.gateway.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Payment Gateway Proxy Service
 * 
 * SPEC REQUIREMENT: "Support for multiple payment methods (cards, wallets) via proxy routing"
 * 
 * Routes payment requests to appropriate payment processors based on:
 * - Payment method (card, wallet, bank transfer)
 * - Card brand (Visa, Mastercard, Amex)
 * - Region/currency
 * 
 * Supported Gateways:
 * - Stripe: Credit/Debit cards, digital wallets
 * - PayPal: PayPal wallet, Venmo
 * - Braintree: Cards, PayPal, Apple Pay, Google Pay
 * - Adyen: Global payment processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService {
    
    private final StripeGateway stripeGateway;
    private final PayPalGateway payPalGateway;
    private final BraintreeGateway braintreeGateway;
    private final AdyenGateway adyenGateway;
    
    /**
     * Process payment through appropriate gateway
     * 
     * Routing logic:
     * - CREDIT_CARD, DEBIT_CARD → Route by card brand and region
     * - DIGITAL_WALLET → Route by wallet provider
     * - BANK_TRANSFER, ACH → Route by region
     */
    public PaymentGatewayResponse processPayment(PaymentGatewayRequest request) {
        log.info("Processing payment via gateway - Method: {}, Amount: {}", 
                request.getPaymentMethod(), request.getAmount());
        
        try {
            PaymentGateway gateway = selectGateway(request);
            
            log.info("Selected gateway: {} for payment method: {}", 
                    gateway.getGatewayName(), request.getPaymentMethod());
            
            PaymentGatewayResponse response = gateway.processPayment(request);
            
            log.info("Gateway response - Status: {}, Transaction ID: {}", 
                    response.getStatus(), response.getTransactionId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Gateway processing failed: {}", e.getMessage(), e);
            
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
    
    /**
     * Select appropriate payment gateway based on payment method and context
     */
    private PaymentGateway selectGateway(PaymentGatewayRequest request) {
        PaymentMethod method = request.getPaymentMethod();
        String cardBrand = request.getCardBrand();
        String currency = request.getCurrency();
        
        switch (method) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return selectCardGateway(cardBrand, currency);
                
            case DIGITAL_WALLET:
                return selectWalletGateway(request.getWalletProvider());
                
            case BANK_TRANSFER:
            case ACH:
                return selectBankTransferGateway(currency);
                
            case WIRE_TRANSFER:
                return adyenGateway; // Adyen for international transfers
                
            default:
                log.warn("Unknown payment method: {}, defaulting to Stripe", method);
                return stripeGateway;
        }
    }
    
    /**
     * Select gateway for card payments
     */
    private PaymentGateway selectCardGateway(String cardBrand, String currency) {
        // Route based on card brand and region
        
        if ("USD".equals(currency) || "CAD".equals(currency)) {
            // North America: Stripe or Braintree
            return "AMEX".equals(cardBrand) ? braintreeGateway : stripeGateway;
        }
        
        if ("EUR".equals(currency) || "GBP".equals(currency)) {
            // Europe: Adyen (better rates for EU)
            return adyenGateway;
        }
        
        // Default to Stripe (global coverage)
        return stripeGateway;
    }
    
    /**
     * Select gateway for digital wallet payments
     */
    private PaymentGateway selectWalletGateway(String walletProvider) {
        if (walletProvider == null) {
            return stripeGateway; // Default
        }
        
        switch (walletProvider.toUpperCase()) {
            case "PAYPAL":
            case "VENMO":
                return payPalGateway;
                
            case "APPLE_PAY":
            case "GOOGLE_PAY":
                return stripeGateway; // Stripe supports both
                
            default:
                return stripeGateway;
        }
    }
    
    /**
     * Select gateway for bank transfers
     */
    private PaymentGateway selectBankTransferGateway(String currency) {
        if ("USD".equals(currency)) {
            return braintreeGateway; // Braintree ACH
        }
        
        // International bank transfers
        return adyenGateway;
    }
    
    /**
     * Get supported payment methods for a given gateway
     */
    public String[] getSupportedMethods(String gatewayName) {
        switch (gatewayName.toUpperCase()) {
            case "STRIPE":
                return new String[]{"CREDIT_CARD", "DEBIT_CARD", "APPLE_PAY", "GOOGLE_PAY"};
            case "PAYPAL":
                return new String[]{"PAYPAL", "VENMO"};
            case "BRAINTREE":
                return new String[]{"CREDIT_CARD", "PAYPAL", "APPLE_PAY", "GOOGLE_PAY", "ACH"};
            case "ADYEN":
                return new String[]{"CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "WIRE_TRANSFER"};
            default:
                return new String[]{};
        }
    }
}

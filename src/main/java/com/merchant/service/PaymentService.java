package com.merchant.service;

import com.merchant.dto.CreatePaymentRequest;
import com.merchant.dto.PaymentResponse;
import com.merchant.enums.PaymentMethod;
import com.merchant.enums.PaymentStatus;
import com.merchant.model.Payment;
import com.merchant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Service - PCI Compliant
 * 
 * CRITICAL: This service NEVER handles raw card data
 * All card data must be tokenized before reaching this service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final TokenizationService tokenizationService;
    private final WebhookService webhookService;
    
    /**
     * Create a new payment
     * 
     * @param request Payment details (with token, not raw card data)
     * @param merchantId Merchant identifier
     * @param sandbox Sandbox mode flag
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request, 
                                        String merchantId, 
                                        boolean sandbox) {
        
        log.info("Creating payment for merchant: {}, sandbox: {}", merchantId, sandbox);
        
        // Validate payment token (should be pre-tokenized)
        if (!tokenizationService.isValidToken(request.getPaymentToken())) {
            throw new IllegalArgumentException("Invalid payment token");
        }
        
        // Get last 4 and card brand from token service
        var tokenDetails = tokenizationService.getTokenDetails(request.getPaymentToken());
        
        Payment payment = Payment.builder()
                .merchantId(merchantId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentToken(request.getPaymentToken())
                .lastFour(tokenDetails.getLastFour())
                .cardBrand(tokenDetails.getCardBrand())
                .description(request.getDescription())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .externalReference(request.getExternalReference())
                .sandbox(sandbox)
                .metadata(request.getMetadata())
                .build();
        
        Payment saved = paymentRepository.save(payment);
        
        // Trigger async payment processing
        processPaymentAsync(saved.getId());
        
        // Send webhook event
        webhookService.sendWebhookEvent(saved.getId(), "payment.created");
        
        return PaymentResponse.fromEntity(saved);
    }
    
    /**
     * Process payment asynchronously
     * In production, this would integrate with payment gateway
     */
    private void processPaymentAsync(String paymentId) {
        // This would be handled by a queue/async processor
        // For now, simulate immediate processing
        log.info("Processing payment: {}", paymentId);
        
        // Simulate payment processing
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        
        if (payment.getSandbox()) {
            // Sandbox mode - auto-approve
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
        } else {
            // Real mode - send to payment processor
            payment.setStatus(PaymentStatus.PROCESSING);
        }
        
        paymentRepository.save(payment);
        webhookService.sendWebhookEvent(paymentId, "payment.processing");
    }
    
    /**
     * Get payment by ID
     */
    public PaymentResponse getPayment(String paymentId, String merchantId) {
        Payment payment = paymentRepository
                .findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        return PaymentResponse.fromEntity(payment);
    }
    
    /**
     * Refund a payment
     */
    @Transactional
    public PaymentResponse refundPayment(String paymentId, String merchantId) {
        Payment payment = paymentRepository
                .findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAmount(payment.getAmount());
        payment.setRefundedAt(LocalDateTime.now());
        
        Payment refunded = paymentRepository.save(payment);
        
        webhookService.sendWebhookEvent(paymentId, "payment.refunded");
        
        return PaymentResponse.fromEntity(refunded);
    }
}

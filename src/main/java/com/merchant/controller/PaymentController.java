package com.merchant.controller;

import com.merchant.dto.CreatePaymentRequest;
import com.merchant.dto.PaymentResponse;
import com.merchant.service.ApiKeyService;
import com.merchant.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment Controller
 * 
 * PCI-DSS Compliant Payment API
 * - Never handles raw card data
 * - All payment methods tokenized
 * - Supports sandbox mode
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "PCI-Compliant Payment Processing API")
@SecurityRequirement(name = "ApiKeyAuth")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final ApiKeyService apiKeyService;
    
    @PostMapping
    @Operation(
        summary = "Create a new payment",
        description = "Process a payment using a tokenized payment method. " +
                     "Card data must be tokenized on the client side before sending. " +
                     "Supports sandbox mode for testing."
    )
    @ApiResponse(responseCode = "201", description = "Payment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        // Validate API key and get merchant ID
        if (!apiKeyService.validateApiKey(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String merchantId = apiKeyService.getMerchantIdFromKey(apiKey);
        boolean sandbox = apiKeyService.isSandboxKey(apiKey);
        
        PaymentResponse payment = paymentService.createPayment(request, merchantId, sandbox);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get payment details",
        description = "Retrieve payment information by ID"
    )
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content)
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String id,
            @RequestHeader("X-API-Key") String apiKey) {
        
        String merchantId = apiKeyService.getMerchantIdFromKey(apiKey);
        PaymentResponse payment = paymentService.getPayment(id, merchantId);
        
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{id}/refund")
    @Operation(
        summary = "Refund a payment",
        description = "Process a full refund for a completed payment"
    )
    @ApiResponse(responseCode = "200", description = "Refund processed")
    @ApiResponse(responseCode = "400", description = "Cannot refund", content = @Content)
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable String id,
            @RequestHeader("X-API-Key") String apiKey) {
        
        String merchantId = apiKeyService.getMerchantIdFromKey(apiKey);
        PaymentResponse payment = paymentService.refundPayment(id, merchantId);
        
        return ResponseEntity.ok(payment);
    }
}

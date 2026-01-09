package com.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/merchants")
@Tag(name = "Merchants", description = "Merchant Management")
public class MerchantController {
    
    @GetMapping("/profile")
    @Operation(summary = "Get merchant profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(Map.of(
            "merchantId", "merch_123",
            "businessName", "Tech Solutions Inc",
            "email", "contact@techsolutions.com",
            "status", "active"
        ));
    }
    
    @GetMapping("/balance")
    @Operation(summary = "Get merchant balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        return ResponseEntity.ok(Map.of(
            "availableBalance", 15000.00,
            "pendingBalance", 2500.00,
            "currency", "USD"
        ));
    }
}

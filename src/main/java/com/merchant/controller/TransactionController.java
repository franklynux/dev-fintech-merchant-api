package com.merchant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction Management API")
public class TransactionController {
    
    @GetMapping
    @Operation(summary = "List all transactions")
    public ResponseEntity<Map<String, Object>> listTransactions(
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        List<Map<String, Object>> transactions = List.of(
            Map.of("id", "txn_001", "amount", 150.00, "status", "completed"),
            Map.of("id", "txn_002", "amount", 250.50, "status", "pending")
        );
        
        return ResponseEntity.ok(Map.of(
            "data", transactions,
            "pagination", Map.of("page", page, "size", size, "total", 2)
        ));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details")
    public ResponseEntity<Map<String, Object>> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(Map.of(
            "id", id,
            "amount", 150.00,
            "status", "completed"
        ));
    }
    
    @PostMapping
    @Operation(summary = "Create new transaction")
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Map<String, Object> request) {
        Map<String, Object> transaction = new HashMap<>(request);
        transaction.put("id", "txn_" + UUID.randomUUID().toString().substring(0, 8));
        transaction.put("status", "pending");
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get transaction statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalTransactions", 1250,
            "totalVolume", 187500.00,
            "successRate", 94.5
        ));
    }
}

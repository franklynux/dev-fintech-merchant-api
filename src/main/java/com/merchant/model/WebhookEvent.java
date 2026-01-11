package com.merchant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_events", indexes = {
    @Index(name = "idx_payment_id", columnList = "paymentId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false)
    private String webhookUrl;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = false)
    private String status; // PENDING, SENT, FAILED
    
    private Integer attempts;
    
    private String responseCode;
    
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime nextRetryAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        attempts = 0;
        status = "PENDING";
    }
}

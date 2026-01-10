package com.merchant.service;

import com.merchant.model.Payment;
import com.merchant.model.WebhookEvent;
import com.merchant.repository.PaymentRepository;
import com.merchant.repository.WebhookEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final WebhookEventRepository webhookEventRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Send webhook event asynchronously
     */
    @Async
    public void sendWebhookEvent(String paymentId, String eventType) {
        try {
            Payment payment = paymentRepository.findById(paymentId).orElseThrow();
            
            // Get merchant webhook URL (from config/database)
            String webhookUrl = getMerchantWebhookUrl(payment.getMerchantId());
            
            if (webhookUrl == null) {
                log.info("No webhook URL configured for merchant: {}", payment.getMerchantId());
                return;
            }
            
            // Build payload
            Map<String, Object> payload = Map.of(
                "event_type", eventType,
                "payment_id", payment.getId(),
                "amount", payment.getAmount(),
                "currency", payment.getCurrency(),
                "status", payment.getStatus(),
                "timestamp", LocalDateTime.now()
            );
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            // Create webhook event record
            WebhookEvent event = WebhookEvent.builder()
                    .paymentId(paymentId)
                    .eventType(eventType)
                    .webhookUrl(webhookUrl)
                    .payload(payloadJson)
                    .status("PENDING")
                    .build();
            
            webhookEventRepository.save(event);
            
            // Send webhook
            sendWebhookWithRetry(event);
            
        } catch (Exception e) {
            log.error("Failed to send webhook: {}", e.getMessage());
        }
    }
    
    private void sendWebhookWithRetry(WebhookEvent event) {
        try {
            var response = restTemplate.postForEntity(
                event.getWebhookUrl(),
                event.getPayload(),
                String.class
            );
            
            event.setStatus("SENT");
            event.setResponseCode(String.valueOf(response.getStatusCode().value()));
            event.setResponseBody(response.getBody());
            event.setSentAt(LocalDateTime.now());
            event.setAttempts(event.getAttempts() + 1);
            
            webhookEventRepository.save(event);
            
            log.info("Webhook sent successfully: {}", event.getId());
            
        } catch (Exception e) {
            event.setStatus("FAILED");
            event.setResponseBody(e.getMessage());
            event.setAttempts(event.getAttempts() + 1);
            event.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            
            webhookEventRepository.save(event);
            
            log.error("Webhook failed: {}", e.getMessage());
        }
    }
    
    private String getMerchantWebhookUrl(String merchantId) {
        // In production, fetch from merchant settings
        return "https://merchant-webhook-url.com/webhook";
    }
}

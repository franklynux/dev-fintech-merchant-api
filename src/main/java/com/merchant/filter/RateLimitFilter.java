package com.merchant.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Rate Limiting Filter
 * Applies distributed rate limiting using Bucket4j with Redis backend
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final Optional<ProxyManager<String>> proxyManager;
    
    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;
    
    @Value("${rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;
    
    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    public RateLimitFilter(@Autowired(required = false) ProxyManager<String> proxyManager) {
        this.proxyManager = Optional.ofNullable(proxyManager);
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting if disabled or ProxyManager not available
        if (!rateLimitEnabled || proxyManager.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientId = getClientIdentifier(request);
        
        try {
            Bucket bucket = proxyManager.get().builder()
                    .build(clientId, bucketConfiguration());
            
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            
            if (probe.isConsumed()) {
                addRateLimitHeaders(response, probe);
                filterChain.doFilter(request, response);
            } else {
                handleRateLimitExceeded(response, probe);
            }
        } catch (Exception e) {
            log.error("Rate limiting error for client {}: {}", clientId, e.getMessage());
            // Allow request to proceed on rate limiting error
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * Extract client identifier from request
     * Priority: API Key > X-Forwarded-For > Remote Address
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey.substring(0, Math.min(20, apiKey.length()));
        }
        
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        
        return "ip:" + request.getRemoteAddr();
    }
    
    /**
     * Build Bucket4j configuration with minute and hour limits
     */
    private Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(requestsPerMinute, Duration.ofMinutes(1)))
                .addLimit(Bandwidth.simple(requestsPerHour, Duration.ofHours(1)))
                .build();
    }
    
    /**
     * Add rate limit information to response headers
     */
    private void addRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
    }
    
    /**
     * Handle rate limit exceeded scenario
     */
    private void handleRateLimitExceeded(HttpServletResponse response, ConsumptionProbe probe) 
            throws IOException {
        long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("Retry-After", String.valueOf(waitSeconds));
        response.addHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            "{\"error\": \"Rate limit exceeded\", \"retryAfter\": " + waitSeconds + "}"
        );
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/system/health") ||
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs");
    }
}

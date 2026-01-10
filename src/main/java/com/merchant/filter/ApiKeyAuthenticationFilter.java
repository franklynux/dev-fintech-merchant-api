package com.merchant.filter;

import com.merchant.service.ApiKeyService;
import com.merchant.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * API Key Authentication Filter
 * 
 * Validates API keys from X-API-Key header
 * Sets merchant ID in security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private final ApiKeyService apiKeyService;
    private final AuditService auditService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = extractApiKey(request);
        
        if (apiKey != null && !apiKey.isEmpty()) {
            // Validate API key
            if (apiKeyService.validateApiKey(apiKey)) {
                String merchantId = apiKeyService.getMerchantIdFromKey(apiKey);
                
                // Set authentication in security context
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        merchantId, 
                        null, 
                        Collections.emptyList()
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Authenticated merchant: {}", merchantId);
                
            } else {
                // Invalid API key
                log.warn("Invalid API key attempt from IP: {}", request.getRemoteAddr());
                auditService.logAuthenticationFailure(request.getRemoteAddr(), "Invalid API key");
                
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Invalid API key\",\"message\":\"The provided API key is invalid or expired\"}"
                );
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractApiKey(HttpServletRequest request) {
        // Try X-API-Key header first (preferred)
        String apiKey = request.getHeader("X-API-Key");
        
        // Fallback to Authorization header with Bearer scheme
        if (apiKey == null || apiKey.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer mk_")) {
                apiKey = authHeader.substring(7); // Remove "Bearer "
            }
        }
        
        return apiKey;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
               path.startsWith("/system/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/error");
    }
}

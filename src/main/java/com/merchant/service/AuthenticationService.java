package com.merchant.service;

import com.merchant.dto.LoginRequest;
import com.merchant.dto.LoginResponse;
import com.merchant.exception.AuthenticationException;
import com.merchant.model.Merchant;
import com.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service
 * Handles JWT-based authentication for merchant dashboard access
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final MerchantRepository merchantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    private static final int ACCESS_TOKEN_EXPIRY_SECONDS = 3600; // 1 hour
    
    /**
     * Authenticate merchant and generate JWT tokens
     * 
     * @param request Login credentials
     * @return LoginResponse with access and refresh tokens
     * @throws AuthenticationException if authentication fails
     */
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        log.debug("Authentication attempt for email: {}", request.getEmail());
        
        // Find merchant by email
        Merchant merchant = merchantRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.logAuthenticationFailure(request.getEmail(), "User not found");
                    return new AuthenticationException("Invalid credentials");
                });
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), merchant.getPasswordHash())) {
            auditService.logAuthenticationFailure(request.getEmail(), "Invalid password");
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Check if merchant is active
        if (!merchant.getActive()) {
            auditService.logAuthenticationFailure(request.getEmail(), "Account deactivated");
            throw new AuthenticationException("Merchant account is deactivated");
        }
        
        // Generate tokens
        Map<String, Object> claims = buildClaims(merchant);
        
        String accessToken = jwtService.generateToken(
            merchant.getId(),
            merchant.getEmail(),
            claims
        );
        
        String refreshToken = jwtService.generateRefreshToken(merchant.getId());
        
        // Update last login timestamp
        merchant.setLastLoginAt(LocalDateTime.now());
        merchantRepository.save(merchant);
        
        // Audit log
        auditService.logAuthenticationSuccess(merchant.getId(), merchant.getEmail());
        
        log.info("Merchant authenticated successfully: {}", merchant.getEmail());
        
        return buildLoginResponse(merchant, accessToken, refreshToken);
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshToken Valid refresh token
     * @return LoginResponse with new access token
     * @throws AuthenticationException if refresh token is invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Token refresh attempt");
        
        // Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new AuthenticationException("Invalid refresh token");
        }
        
        // Verify token type is refresh
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (tokenType == null || !tokenType.equals("refresh")) {
            log.warn("Invalid token type for refresh: {}", tokenType);
            throw new AuthenticationException("Invalid token type");
        }
        
        // Extract merchant ID
        String merchantId = jwtService.extractMerchantId(refreshToken);
        
        // Find merchant
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new AuthenticationException("Merchant not found"));
        
        // Check if merchant is still active
        if (!merchant.getActive()) {
            log.warn("Merchant account deactivated for refresh attempt: {}", merchantId);
            throw new AuthenticationException("Merchant account is deactivated");
        }
        
        // Generate new access token
        Map<String, Object> claims = buildClaims(merchant);
        
        String newAccessToken = jwtService.generateToken(
            merchant.getId(),
            merchant.getEmail(),
            claims
        );
        
        log.info("Token refreshed successfully for merchant: {}", merchant.getEmail());
        
        return buildLoginResponse(merchant, newAccessToken, refreshToken);
    }
    
    /**
     * Logout merchant (invalidate token)
     * 
     * @param token JWT token to invalidate
     */
    public void logout(String token) {
        try {
            String merchantId = jwtService.extractMerchantId(token);
            auditService.logLogout(merchantId);
            log.info("Merchant logged out: {}", merchantId);
            
            // TODO: In production, add token to Redis blacklist
            // jwtService.blacklistToken(token);
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }
    
    /**
     * Build JWT claims for merchant
     */
    private Map<String, Object> buildClaims(Merchant merchant) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", merchant.getRole());
        claims.put("business_name", merchant.getBusinessName());
        return claims;
    }
    
    /**
     * Build login response DTO
     */
    private LoginResponse buildLoginResponse(Merchant merchant, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(ACCESS_TOKEN_EXPIRY_SECONDS)
                .merchantId(merchant.getId())
                .email(merchant.getEmail())
                .businessName(merchant.getBusinessName())
                .build();
    }
}
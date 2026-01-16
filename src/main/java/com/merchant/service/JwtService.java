package com.merchant.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service
 * Handles JWT token generation, validation, and claims extraction
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret:your-256-bit-secret-key-change-in-production-must-be-at-least-32-characters}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}") // 1 hour in milliseconds
    private Long jwtExpirationMs;
    
    @Value("${jwt.refresh-expiration:86400000}") // 24 hours in milliseconds
    private Long refreshExpirationMs;
    
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_MERCHANT_ID = "merchant_id";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";
    
    /**
     * Generate access token with custom claims
     * 
     * @param merchantId Unique merchant identifier
     * @param email Merchant email
     * @param claims Additional claims to include
     * @return JWT access token
     */
    public String generateToken(String merchantId, String email, Map<String, Object> claims) {
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put(CLAIM_MERCHANT_ID, merchantId);
        tokenClaims.put(CLAIM_EMAIL, email);
        tokenClaims.put(CLAIM_TYPE, TOKEN_TYPE_ACCESS);
        
        return buildToken(tokenClaims, merchantId, jwtExpirationMs);
    }
    
    /**
     * Generate refresh token
     * 
     * @param merchantId Unique merchant identifier
     * @return JWT refresh token
     */
    public String generateRefreshToken(String merchantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, TOKEN_TYPE_REFRESH);
        
        return buildToken(claims, merchantId, refreshExpirationMs);
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract merchant ID from token
     * 
     * @param token JWT token
     * @return Merchant ID
     */
    public String extractMerchantId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract email from token
     * 
     * @param token JWT token
     * @return Email address
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_EMAIL, String.class));
    }
    
    /**
     * Extract token type (access or refresh)
     * 
     * @param token JWT token
     * @return Token type
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TYPE, String.class));
    }
    
    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Extract expiration date from token
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract specific claim from token
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     * 
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Build JWT token
     * 
     * @param claims Token claims
     * @param subject Token subject (merchant ID)
     * @param expirationMs Expiration time in milliseconds
     * @return JWT token string
     */
    private String buildToken(Map<String, Object> claims, String subject, Long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Get signing key from secret
     * 
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
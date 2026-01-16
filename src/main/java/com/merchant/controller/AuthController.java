package com.merchant.controller;

import com.merchant.dto.LoginRequest;
import com.merchant.dto.LoginResponse;
import com.merchant.dto.RefreshTokenRequest;
import com.merchant.service.AuthenticationService;
import com.merchant.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * SPEC REQUIREMENT: JWT authentication
 * 
 * Endpoints:
 * - POST /auth/login - Merchant login (returns JWT)
 * - POST /auth/refresh-token - Refresh expired JWT
 * - POST /auth/logout - Invalidate tokens
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT-based Authentication for Dashboard Access")
public class AuthController {
    
    private final AuthenticationService authService;
    private final JwtService jwtService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Merchant login",
        description = "Authenticate merchant and receive JWT tokens for dashboard access"
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh JWT token",
        description = "Get new access token using refresh token"
    )
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Invalidate current session (client should discard tokens)"
    )
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        
        return ResponseEntity.noContent().build();
    }
}

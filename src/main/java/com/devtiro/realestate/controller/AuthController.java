package com.devtiro.realestate.controller;

import com.devtiro.realestate.domain.dto.AuthResponse;
import com.devtiro.realestate.domain.dto.LoginRequest;
import com.devtiro.realestate.domain.dto.RefreshTokenRequest;
import com.devtiro.realestate.domain.dto.RegisterRequest;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        AuthResponse response = authService.register(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Login with existing credentials
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                     HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        AuthResponse response = authService.refreshToken(request.getRefreshToken(), ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by invalidating refresh token
     * POST /api/auth/logout
     * Requires authentication (valid access token in header)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user,
                                       HttpServletRequest httpRequest) {
        String ipAddress = getClientIP(httpRequest);
        authService.logout(user.getEmail(), ipAddress);
        return ResponseEntity.ok().build();
    }

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs, first one is the client
        return xForwardedForHeader.split(",")[0].trim();
    }
}

package com.devtiro.realestate.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking custom application metrics
 * Uses Micrometer for metrics collection and exposition
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Authentication Metrics
    private static final String METRIC_AUTH_REGISTRATION = "auth.registration";
    private static final String METRIC_AUTH_LOGIN = "auth.login";
    private static final String METRIC_AUTH_LOGOUT = "auth.logout";
    private static final String METRIC_AUTH_TOKEN_REFRESH = "auth.token.refresh";
    private static final String METRIC_AUTH_FAILED_ATTEMPTS = "auth.failed.attempts";
    private static final String METRIC_AUTH_ACCOUNT_LOCKED = "auth.account.locked";
    
    // Performance Metrics
    private static final String METRIC_AUTH_DURATION = "auth.duration";
    private static final String METRIC_TOKEN_GENERATION_DURATION = "auth.token.generation.duration";
    
    // Security Metrics
    private static final String METRIC_INVALID_TOKEN = "auth.invalid.token";
    private static final String METRIC_EXPIRED_TOKEN = "auth.expired.token";
    private static final String METRIC_PASSWORD_VALIDATION_FAILURE = "auth.password.validation.failure";

    // ============ Registration Metrics ============

    /**
     * Record successful registration
     */
    public void recordRegistrationSuccess() {
        Counter.builder(METRIC_AUTH_REGISTRATION)
                .tag("status", "success")
                .description("Total number of successful user registrations")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record failed registration
     */
    public void recordRegistrationFailure(String reason) {
        Counter.builder(METRIC_AUTH_REGISTRATION)
                .tag("status", "failure")
                .tag("reason", reason)
                .description("Total number of failed user registrations")
                .register(meterRegistry)
                .increment();
    }

    // ============ Login Metrics ============

    /**
     * Record successful login
     */
    public void recordLoginSuccess(String reason) {
        Counter.builder(METRIC_AUTH_LOGIN)
                .tag("status", "success")
                .tag("reason", reason)
                .description("Total number of successful login attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record failed login
     */
    public void recordLoginFailure(String reason) {
        Counter.builder(METRIC_AUTH_LOGIN)
                .tag("status", "failure")
                .tag("reason", reason)
                .description("Total number of failed login attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record login duration
     */
    public void recordLoginDuration(long durationMillis) {
        Timer.builder(METRIC_AUTH_DURATION)
                .tag("operation", "login")
                .description("Time taken to process login requests")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }

    // ============ Logout Metrics ============

    /**
     * Record logout event
     */
    public void recordLogout() {
        Counter.builder(METRIC_AUTH_LOGOUT)
                .description("Total number of logout events")
                .register(meterRegistry)
                .increment();
    }

    // ============ Token Refresh Metrics ============

    /**
     * Record successful token refresh
     */
    public void recordTokenRefreshSuccess() {
        Counter.builder(METRIC_AUTH_TOKEN_REFRESH)
                .tag("status", "success")
                .description("Total number of successful token refreshes")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record failed token refresh
     */
    public void recordTokenRefreshFailure(String reason) {
        Counter.builder(METRIC_AUTH_TOKEN_REFRESH)
                .tag("status", "failure")
                .tag("reason", reason)
                .description("Total number of failed token refreshes")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record token generation duration
     */
    public void recordTokenGenerationDuration(long durationMillis) {
        Timer.builder(METRIC_TOKEN_GENERATION_DURATION)
                .description("Time taken to generate JWT tokens")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }

    // ============ Security Metrics ============

    /**
     * Record failed login attempt (for rate limiting tracking)
     */
    public void recordFailedAttempt(String email) {
        Counter.builder(METRIC_AUTH_FAILED_ATTEMPTS)
                .tag("email", sanitizeEmail(email))
                .description("Number of failed login attempts per user")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record account lockout
     */
    public void recordAccountLocked(String email) {
        Counter.builder(METRIC_AUTH_ACCOUNT_LOCKED)
                .tag("email", sanitizeEmail(email))
                .description("Number of account lockouts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record invalid token attempt
     */
    public void recordInvalidToken(String reason) {
        Counter.builder(METRIC_INVALID_TOKEN)
                .tag("reason", reason)
                .description("Number of invalid token attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record expired token attempt
     */
    public void recordExpiredToken() {
        Counter.builder(METRIC_EXPIRED_TOKEN)
                .description("Number of expired token attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record password validation failure
     */
    public void recordPasswordValidationFailure(String reason) {
        Counter.builder(METRIC_PASSWORD_VALIDATION_FAILURE)
                .tag("reason", reason)
                .description("Number of password validation failures")
                .register(meterRegistry)
                .increment();
    }

    // ============ Helper Methods ============

    /**
     * Sanitize email for metric tags (remove @ and . for Prometheus compatibility)
     */
    private String sanitizeEmail(String email) {
        if (email == null) {
            return "unknown";
        }
        // Remove domain for privacy and Prometheus compatibility
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex) + "_redacted";
        }
        return email.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}

package com.devtiro.realestate.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for logging security-related events
 * Provides audit trail for authentication, authorization, and security incidents
 */
@Slf4j
@Service
public class SecurityAuditService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Log successful user registration
     */
    public void logRegistrationSuccess(String email, String username, String ipAddress) {
        log.info("SECURITY_AUDIT | EVENT=REGISTRATION_SUCCESS | user={} | username={} | ip={} | timestamp={}", 
                email, username, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log failed registration attempt
     */
    public void logRegistrationFailure(String email, String reason, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=REGISTRATION_FAILURE | user={} | reason={} | ip={} | timestamp={}", 
                email, reason, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log successful login
     */
    public void logLoginSuccess(String email, String ipAddress) {
        log.info("SECURITY_AUDIT | EVENT=LOGIN_SUCCESS | user={} | ip={} | timestamp={}", 
                email, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log failed login attempt
     */
    public void logLoginFailure(String email, String reason, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=LOGIN_FAILURE | user={} | reason={} | ip={} | timestamp={}", 
                email, reason, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log account lockout due to too many failed attempts
     */
    public void logAccountLocked(String email, int attemptCount, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=ACCOUNT_LOCKED | user={} | attempts={} | ip={} | timestamp={}", 
                email, attemptCount, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log successful token refresh
     */
    public void logTokenRefreshSuccess(String email, String ipAddress) {
        log.info("SECURITY_AUDIT | EVENT=TOKEN_REFRESH_SUCCESS | user={} | ip={} | timestamp={}", 
                email, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log failed token refresh attempt
     */
    public void logTokenRefreshFailure(String email, String reason, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=TOKEN_REFRESH_FAILURE | user={} | reason={} | ip={} | timestamp={}", 
                email, reason, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log user logout
     */
    public void logLogout(String email, String ipAddress) {
        log.info("SECURITY_AUDIT | EVENT=LOGOUT | user={} | ip={} | timestamp={}", 
                email, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log password change
     */
    public void logPasswordChange(String email, String ipAddress) {
        log.info("SECURITY_AUDIT | EVENT=PASSWORD_CHANGE | user={} | ip={} | timestamp={}", 
                email, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String email, String activity, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=SUSPICIOUS_ACTIVITY | user={} | activity={} | ip={} | timestamp={}", 
                email, activity, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log invalid JWT token usage attempt
     */
    public void logInvalidTokenAttempt(String reason, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=INVALID_TOKEN_ATTEMPT | reason={} | ip={} | timestamp={}", 
                reason, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log expired token usage attempt
     */
    public void logExpiredTokenAttempt(String email, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=EXPIRED_TOKEN_ATTEMPT | user={} | ip={} | timestamp={}", 
                email, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log access denied event
     */
    public void logAccessDenied(String email, String resource, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=ACCESS_DENIED | user={} | resource={} | ip={} | timestamp={}", 
                email, resource, ipAddress, getCurrentTimestamp());
    }

    /**
     * Log password validation failure
     */
    public void logPasswordValidationFailure(String email, String reason, String ipAddress) {
        log.warn("SECURITY_AUDIT | EVENT=PASSWORD_VALIDATION_FAILURE | user={} | reason={} | ip={} | timestamp={}", 
                email, reason, ipAddress, getCurrentTimestamp());
    }

    /**
     * Get current timestamp as formatted string
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}

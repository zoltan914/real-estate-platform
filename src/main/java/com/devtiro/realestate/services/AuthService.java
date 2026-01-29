package com.devtiro.realestate.services;

import com.devtiro.realestate.domain.dto.AuthResponse;
import com.devtiro.realestate.domain.dto.LoginRequest;
import com.devtiro.realestate.domain.dto.RegisterRequest;
import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.exceptions.InvalidRefreshTokenException;
import com.devtiro.realestate.repositories.UserRepository;
import com.devtiro.realestate.security.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service handling user authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordValidator passwordValidator;
    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService securityAuditService;
    private final MetricsService metricsService;

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request, String ipAddress) {

        try {
            passwordValidator.validate(request.getPassword());
        } catch (IllegalArgumentException e) {
            securityAuditService.logPasswordValidationFailure(request.getEmail(), e.getMessage(), ipAddress);
            metricsService.recordPasswordValidationFailure(e.getMessage());
            throw e;
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            securityAuditService.logRegistrationFailure(request.getEmail(), "Username already exists", ipAddress);
            metricsService.recordRegistrationFailure("username_exists");
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            securityAuditService.logRegistrationFailure(request.getEmail(), "Email already exists", ipAddress);
            metricsService.recordRegistrationFailure("email_exists");
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Generate tokens
        long tokenStartTime = System.currentTimeMillis();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        long tokenDuration = System.currentTimeMillis() - tokenStartTime;
        metricsService.recordTokenGenerationDuration(tokenDuration);

        // Store refresh token in user
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiryDate(
                LocalDateTime.now().plusSeconds(jwtService.getJwtRefreshTokenExpirationMs() / 1000)
        );

        userRepository.save(user);

        // Log successful registration
        securityAuditService.logRegistrationSuccess(user.getEmail(), user.getUsername(), ipAddress);
        metricsService.recordRegistrationSuccess();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .message("User registered successfully")
                .expiresIn(jwtService.getJwtAccessTokenExpirationMs())
                .refreshExpiresIn(jwtService.getJwtRefreshTokenExpirationMs())
                .build();
    }

    /**
     * Authenticate and login a user
     */
    //@CacheEvict(value = "usersByEmail", key = "#request.email")
    public AuthResponse login(LoginRequest request, String ipAddress) {
        long loginStartTime = System.currentTimeMillis();

        try {

            if (loginAttemptService.isBlocked(request.getEmail())) {
                securityAuditService.logAccountLocked(
                        request.getEmail(),
                        loginAttemptService.getMaxAttempts(),
                        ipAddress
                );
                metricsService.recordAccountLocked(request.getEmail());
                throw new IllegalStateException(
                        String.format("Account temporarily locked. Try again in %d minutes.", (int)(loginAttemptService.getLockTimeDuration()/60/1000))
                );
            }

            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Retrieve user from database
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Check if account is enabled
            if (!user.isEnabled()) {
                securityAuditService.logLoginFailure(request.getEmail(), "Account disabled", ipAddress);
                metricsService.recordLoginFailure("account_disabled");
                throw new IllegalArgumentException("Account is disabled");
            }

            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                securityAuditService.logLoginFailure(request.getEmail(), "Account locked", ipAddress);
                metricsService.recordLoginFailure("account_locked");
                throw new IllegalArgumentException("Account is locked");
            }

            // Generate tokens
            long tokenStartTime = System.currentTimeMillis();
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            long tokenDuration = System.currentTimeMillis() - tokenStartTime;
            metricsService.recordTokenGenerationDuration(tokenDuration);

            // Store refresh token in user
            user.setRefreshToken(refreshToken);
            user.setRefreshTokenExpiryDate(
                    LocalDateTime.now().plusSeconds(jwtService.getJwtRefreshTokenExpirationMs() / 1000)
            );

            userRepository.save(user);

            loginAttemptService.loginSucceeded(request.getEmail());

            // Log successful login and record metrics
            securityAuditService.logLoginSuccess(request.getEmail(), ipAddress);
            metricsService.recordLoginSuccess("email_and_password");
            long loginDuration = System.currentTimeMillis() - loginStartTime;
            metricsService.recordLoginDuration(loginDuration);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .message("Login successful")
                    .expiresIn(jwtService.getJwtAccessTokenExpirationMs())
                    .refreshExpiresIn(jwtService.getJwtRefreshTokenExpirationMs())
                    .build();

        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(request.getEmail());
            securityAuditService.logLoginFailure(request.getEmail(), "Invalid credentials", ipAddress);
            metricsService.recordLoginFailure("invalid_credentials");
            metricsService.recordFailedAttempt(request.getEmail());
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    @CacheEvict(value = "usersByEmail", key = "#result.email")
    public AuthResponse refreshToken(String refreshToken, String ipAddress) {
        try {
            // Validate refresh token
            if (!jwtService.validateToken(refreshToken)) {
                securityAuditService.logTokenRefreshFailure("unknown", "Invalid or expired token", ipAddress);
                metricsService.recordTokenRefreshFailure("invalid_token");
                throw new InvalidRefreshTokenException(
                        "Invalid or expired refresh token"
                );
            }

            // Verify it's actually a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                securityAuditService.logTokenRefreshFailure("unknown", "Not a refresh token", ipAddress);
                metricsService.recordTokenRefreshFailure("not_refresh_token");
                throw new InvalidRefreshTokenException(
                        "Token is not a refresh token"
                );
            }

            // Get user email from token
            String email = jwtService.getEmailFromToken(refreshToken);

            // Find user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        securityAuditService.logTokenRefreshFailure(email, "User not found", ipAddress);
                        metricsService.recordTokenRefreshFailure("user_not_found");
                        return new IllegalArgumentException("User not found");
                    });

            // Verify the refresh token matches the one stored in database
            if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
                securityAuditService.logTokenRefreshFailure(email, "Token mismatch", ipAddress);
                metricsService.recordTokenRefreshFailure("token_mismatch");
                throw new InvalidRefreshTokenException(
                        "Refresh token does not match"
                );
            }

            // Check if refresh token has expired in database
            if (user.getRefreshTokenExpiryDate() != null &&
                    user.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
                securityAuditService.logTokenRefreshFailure(email, "Token expired", ipAddress);
                metricsService.recordTokenRefreshFailure("token_expired");
                throw new InvalidRefreshTokenException(
                        "Refresh token has expired"
                );
            }

            // Generate new access token
            long tokenStartTime = System.currentTimeMillis();
            String newAccessToken = jwtService.generateAccessToken(user);

            // Optionally rotate refresh token (generate new one)
            String newRefreshToken = jwtService.generateRefreshToken(user);
            long tokenDuration = System.currentTimeMillis() - tokenStartTime;
            metricsService.recordTokenGenerationDuration(tokenDuration);

            user.setRefreshToken(newRefreshToken);
            user.setRefreshTokenExpiryDate(
                    LocalDateTime.now().plusSeconds(jwtService.getJwtRefreshTokenExpirationMs() / 1000)
            );
            userRepository.save(user);

            // Log successful token refresh
            securityAuditService.logTokenRefreshSuccess(email, ipAddress);
            metricsService.recordTokenRefreshSuccess();

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .message("Token refreshed successfully")
                    .expiresIn(jwtService.getJwtAccessTokenExpirationMs())
                    .refreshExpiresIn(jwtService.getJwtRefreshTokenExpirationMs())
                    .build();
        } catch (InvalidRefreshTokenException e) {
            // Already logged in specific catch blocks above
            throw e;
        } catch (Exception e) {
            securityAuditService.logTokenRefreshFailure("unknown", e.getMessage(), ipAddress);
            throw e;
        }
    }

    /**
     * Logout user by invalidating refresh token
     * Clears refresh token from database and evicts user from cache
     *
     * @param email user's email
     * @param ipAddress client IP address
     */
    @CacheEvict(value = "usersByEmail", key = "#email")
    public void logout(String email, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Clear refresh token
        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);

        userRepository.save(user);

        // Log logout event and record metric
        securityAuditService.logLogout(email, ipAddress);
        metricsService.recordLogout();

        log.info("User logged out successfully: {}", email);
    }
}

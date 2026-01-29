package com.devtiro.realestate.security;

import com.devtiro.realestate.domain.entities.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final MetricsService metricsService;
    private final SecurityAuditService securityAuditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            var jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Check if token is expired first (faster check)
                if (jwtService.isTokenExpired(jwt)) {
                    log.debug("JWT token is expired for request: {}", request.getRequestURI());

                    // Record expired token metric
                    metricsService.recordExpiredToken();

                    // Try to extract email for audit log (may fail if token is malformed)
                    try {
                        String email = jwtService.getEmailFromToken(jwt);
                        String ipAddress = getClientIP(request);
                        securityAuditService.logExpiredTokenAttempt(email, ipAddress);
                    } catch (Exception e) {
                        // Token too corrupted to extract email, log with unknown user
                        String ipAddress = getClientIP(request);
                        securityAuditService.logExpiredTokenAttempt("unknown", ipAddress);
                    }

                    filterChain.doFilter(request, response);
                    return;
                }

                // Validate token signature and structure
                if (jwtService.validateToken(jwt)) {
                    var email = jwtService.getEmailFromToken(jwt);
                    User user = (User) userDetailsService.loadUserByUsername(email);

                    if (user != null && email.equals(user.getEmail()) && user.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("User not found or disabled: {}", email);
                    }
                } else {
                    log.debug("Invalid JWT token for request: {}", request.getRequestURI());

                    // Record invalid token metric
                    metricsService.recordInvalidToken("invalid_signature");

                    String ipAddress = getClientIP(request);
                    securityAuditService.logInvalidTokenAttempt("Invalid signature", ipAddress);
                }
            }

        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);

            // Record invalid token metric for malformed tokens
            metricsService.recordInvalidToken("malformed");

            String ipAddress = getClientIP(request);
            securityAuditService.logInvalidTokenAttempt("Malformed token: " + ex.getMessage(), ipAddress);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        var bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
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

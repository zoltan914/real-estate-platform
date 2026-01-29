package com.devtiro.realestate.security;

import com.devtiro.realestate.domain.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Getter
    @Value("${jwt.access-token-expiration}") // 24 hours in milliseconds
    private long jwtAccessTokenExpirationMs;

    @Getter
    @Value("${jwt.refresh-token-expiration}")
    private long jwtRefreshTokenExpirationMs;

    public String generateAccessToken(User user) {
        var now = new Date();
        var expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationMs);

        Map<String, Object> extraClaims = Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "type", "access"
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claims(extraClaims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        var now = new Date();
        var expiryDate = new Date(now.getTime() + jwtRefreshTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validate if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            log.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }


    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }


    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

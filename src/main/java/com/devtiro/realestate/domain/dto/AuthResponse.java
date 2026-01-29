package com.devtiro.realestate.domain.dto;

import com.devtiro.realestate.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private Role role;
    private String message;
    private Long expiresIn; // Access token expiration in milliseconds
    private Long refreshExpiresIn; // Refresh token expiration in milliseconds
}

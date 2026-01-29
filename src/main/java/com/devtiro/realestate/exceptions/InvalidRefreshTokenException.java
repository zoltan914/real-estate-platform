package com.devtiro.realestate.exceptions;

/**
 * Exception thrown when a refresh token is invalid or expired
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}

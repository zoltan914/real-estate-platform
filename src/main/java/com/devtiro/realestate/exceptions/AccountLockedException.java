package com.devtiro.realestate.exceptions;

/**
 * Exception thrown when a user account is locked due to too many failed login attempts
 */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}

package com.devtiro.realestate.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    @Value("${security.password.min-length}")
    private static int passwordMinLength;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{" + passwordMinLength + ",}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public void validate(String password) {
        if (password == null || password.length() < passwordMinLength) {
            throw new IllegalArgumentException(
                    String.format("Password must be at least %d characters long", passwordMinLength)
            );
        }

        if (!pattern.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must contain at least one digit, one lowercase, " +
                            "one uppercase letter, one special character, and no whitespace"
            );
        }
    }

}

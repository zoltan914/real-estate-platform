package com.devtiro.realestate.config;

import com.devtiro.realestate.domain.entities.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Optional;

public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                // or Authentication::getName, and the stuff below is not necessary
                .map(principal -> {
                    // Case 1: Principal is your custom User entity
                    if (principal instanceof User user) {
                        return user.getUsername();
                    }
                    // Case 2: Principal is already a String (e.g., anonymousUser)
                    if (principal instanceof String s) {
                        return s;
                    }
                    // Case 3: Principal is a generic UserDetails
                    if (principal instanceof UserDetails userDetails) {
                        return userDetails.getUsername();
                    }
                    return null;
                });
    }
}

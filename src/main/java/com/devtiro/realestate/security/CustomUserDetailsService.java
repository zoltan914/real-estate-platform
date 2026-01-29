package com.devtiro.realestate.security;

import com.devtiro.realestate.domain.entities.User;
import com.devtiro.realestate.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation for loading user data
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email with caching
     *
     * @param email user's email
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     *
     * Cache Configuration:
     * - Cache name: "usersByEmail"
     * - Cache key: email
     * - TTL: 15 minutes (configured in CacheConfig)
     * - Max size: 1000 entries
     * - Evicted on: token refresh, logout, password change
     */
    @Override
    @Cacheable(value = "usersByEmail", key = "#email", unless = "#result == null")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return user;
    }
}

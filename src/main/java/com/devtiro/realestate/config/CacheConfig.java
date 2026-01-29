package com.devtiro.realestate.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine
 * Caches user lookups to reduce database queries during authentication
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with Caffeine
     * Uses in-memory caching with TTL and size limits
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("usersByEmail");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Caffeine cache configuration
     * - Maximum 1000 entries per cache
     * - Expire after 15 minutes of write
     * - Expire after 5 minutes of no access
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats(); // Enable statistics for monitoring
    }
}

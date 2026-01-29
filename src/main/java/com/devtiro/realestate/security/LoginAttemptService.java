package com.devtiro.realestate.security;

import com.devtiro.realestate.domain.dto.AttemptInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    @Value("${security.login-attempts.max-attempts}")
    private int MAX_ATTEMPTS;

    private long LOCK_TIME_DURATION;

    public LoginAttemptService(@Value("${security.login-attempts.lockout-duration-minutes}") long LOCK_TIME_DURATION) {
        this.LOCK_TIME_DURATION = LOCK_TIME_DURATION * 60 * 1000;
    }

    // private static final int MAX_ATTEMPTS = 5;
    // private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15 minutes

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        AttemptInfo attempts = attemptsCache.getOrDefault(
                key,
                new AttemptInfo(0, null)
        );

        int currentAttempts = attempts.getAttempts() + 1;

        if (currentAttempts >= MAX_ATTEMPTS) {
            attemptsCache.put(key, new AttemptInfo(currentAttempts, System.currentTimeMillis()));
        } else {
            attemptsCache.put(key, new AttemptInfo(currentAttempts, null));
        }
    }

    public boolean isBlocked(String key) {
        AttemptInfo attempts = attemptsCache.get(key);

        if (attempts == null) return false;
        if (attempts.getAttempts() < MAX_ATTEMPTS) return false;
        if (attempts.getLockedTime() == null) return false;

        long unlockTime = attempts.getLockedTime() + LOCK_TIME_DURATION;

        if (System.currentTimeMillis() > unlockTime) {
            attemptsCache.remove(key);
            return false;
        }

        return true;
    }
    public long getLockTimeDuration() {
        return LOCK_TIME_DURATION;
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

}

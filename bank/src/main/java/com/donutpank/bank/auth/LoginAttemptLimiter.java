package com.donutpank.bank.auth;

import com.donutpank.bank.config.LoginProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory lockout to slow down brute-force attempts against /auth/login. */
@Component
public class LoginAttemptLimiter {

    private record Attempts(int count, Instant lockedUntil) {
    }

    private final LoginProperties loginProperties;
    private final ConcurrentHashMap<String, Attempts> attemptsByUsername = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(LoginProperties loginProperties) {
        this.loginProperties = loginProperties;
    }

    public boolean isLocked(String username) {
        Attempts attempts = attemptsByUsername.get(normalize(username));
        return attempts != null && attempts.lockedUntil() != null && Instant.now().isBefore(attempts.lockedUntil());
    }

    public void recordFailure(String username) {
        attemptsByUsername.compute(normalize(username), (key, previous) -> {
            // If a previous lockout has expired, treat this as a fresh start.
            boolean expired = previous != null && previous.lockedUntil() != null
                    && !Instant.now().isBefore(previous.lockedUntil());
            int count = (previous == null || expired ? 0 : previous.count()) + 1;
            boolean justLockedOut = count >= loginProperties.maxAttempts();
            Instant lockedUntil = justLockedOut
                    ? Instant.now().plus(Duration.ofMinutes(loginProperties.lockoutMinutes()))
                    : null;
            return new Attempts(count, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attemptsByUsername.remove(normalize(username));
    }

    private String normalize(String username) {
        return username.toLowerCase();
    }
}

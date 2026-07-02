package com.donutpank.bank.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.donutpank.bank.config.LoginProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptLimiterTest {

    private LoginAttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginAttemptLimiter(new LoginProperties(3, 15));
    }

    @Test
    void notLockedByDefault() {
        assertThat(limiter.isLocked("alice")).isFalse();
    }

    @Test
    void lockedAfterMaxFailures() {
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");
        assertThat(limiter.isLocked("alice")).isFalse();

        limiter.recordFailure("alice");
        assertThat(limiter.isLocked("alice")).isTrue();
    }

    @Test
    void belowMaxFailuresDoesNotLock() {
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");

        assertThat(limiter.isLocked("alice")).isFalse();
    }

    @Test
    void successClearsLockout() {
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");
        limiter.recordFailure("alice");
        assertThat(limiter.isLocked("alice")).isTrue();

        limiter.recordSuccess("alice");
        assertThat(limiter.isLocked("alice")).isFalse();
    }

    @Test
    void usernameIsCaseInsensitive() {
        limiter.recordFailure("Alice");
        limiter.recordFailure("ALICE");
        limiter.recordFailure("alice");

        assertThat(limiter.isLocked("ALICE")).isTrue();
        assertThat(limiter.isLocked("alice")).isTrue();
    }
}

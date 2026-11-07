package org.example.vgtuaventory.service;

import org.example.vgtuaventory.exception.AccountLockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private AuthServiceTest.MutableClock clock;
    private LoginAttemptService loginAttempts;

    @BeforeEach
    void setUp() {
        clock = new AuthServiceTest.MutableClock(Instant.parse("2026-11-07T10:00:00Z"));
        loginAttempts = new LoginAttemptService(clock);
    }

    private void failTimes(String key, int times) {
        for (int i = 0; i < times; i++) {
            loginAttempts.recordFailure(key);
        }
    }

    @Test
    void assertNotLocked_shouldPass_whenNoFailuresRecorded() {
        assertDoesNotThrow(() -> loginAttempts.assertNotLocked("test@test.com"));
    }

    @Test
    void recordFailure_shouldLockOnFifthFailure() {
        for (int attempt = 1; attempt <= 4; attempt++) {
            assertFalse(loginAttempts.recordFailure("test@test.com"), "attempt " + attempt);
        }

        assertTrue(loginAttempts.recordFailure("test@test.com"));
        assertThrows(AccountLockedException.class, () -> loginAttempts.assertNotLocked("test@test.com"));
    }

    @Test
    void assertNotLocked_shouldClearExpiredLock_andRestartCounting() {
        failTimes("test@test.com", 5);

        clock.advance(Duration.ofMinutes(5));

        assertDoesNotThrow(() -> loginAttempts.assertNotLocked("test@test.com"));
        failTimes("test@test.com", 4);
        assertDoesNotThrow(() -> loginAttempts.assertNotLocked("test@test.com"));
    }

    @Test
    void reset_shouldForgetFailures() {
        failTimes("test@test.com", 4);

        loginAttempts.reset("test@test.com");

        assertFalse(loginAttempts.recordFailure("test@test.com"));
    }

    @Test
    void lock_shouldApplyOnlyToItsOwnKey() {
        failTimes("first@test.com", 5);

        assertThrows(AccountLockedException.class, () -> loginAttempts.assertNotLocked("first@test.com"));
        assertDoesNotThrow(() -> loginAttempts.assertNotLocked("second@test.com"));
    }
}

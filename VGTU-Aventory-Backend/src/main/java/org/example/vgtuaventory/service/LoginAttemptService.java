package org.example.vgtuaventory.service;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.exception.AccountLockedException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits login attempts: after 5 failed logins an account is locked for 5 minutes.
 * Keys are normalized (lower-case) emails.
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final Clock clock;
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    /**
     * Throws while the lock is active; an expired lock is cleared together with its counter.
     */
    public void assertNotLocked(String loginKey) {
        Instant blockedUntil = lockedUntil.get(loginKey);
        if (blockedUntil == null) {
            return;
        }

        if (blockedUntil.isAfter(clock.instant())) {
            throw new AccountLockedException();
        }

        reset(loginKey);
    }

    /**
     * Counts a failed login and locks the account when the limit is reached.
     *
     * @return true when this failure locked the account
     */
    public boolean recordFailure(String loginKey) {
        int attempts = failedLoginAttempts.merge(loginKey, 1, Integer::sum);

        if (attempts < MAX_FAILED_LOGIN_ATTEMPTS) {
            return false;
        }

        lockedUntil.put(loginKey, clock.instant().plus(LOCK_DURATION));
        failedLoginAttempts.remove(loginKey);
        return true;
    }

    public void reset(String loginKey) {
        failedLoginAttempts.remove(loginKey);
        lockedUntil.remove(loginKey);
    }
}

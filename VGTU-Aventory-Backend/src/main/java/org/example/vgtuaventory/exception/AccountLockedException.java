package org.example.vgtuaventory.exception;

/**
 * Thrown when an account is temporarily blocked after too many failed logins.
 * Mapped to HTTP 423 Locked.
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Account is temporarily blocked. Try again later.");
    }
}

package org.example.vgtuaventory.exception;

import org.springframework.http.HttpStatus;

/**
 * Paskyra laikinai uzblokuota po per daug nesekmingu prisijungimu (HTTP 423 Locked).
 */
public class AccountLockedException extends ApiException {

    private static final long serialVersionUID = 1L;

    public AccountLockedException() {
        super(HttpStatus.LOCKED, "Account is temporarily blocked. Try again later.");
    }
}

package org.example.vgtuaventory.exception;

import org.springframework.http.HttpStatus;

/**
 * Nezinomas el. pastas arba neteisingas slaptazodis (HTTP 401 Unauthorized).
 */
public class InvalidCredentialsException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}

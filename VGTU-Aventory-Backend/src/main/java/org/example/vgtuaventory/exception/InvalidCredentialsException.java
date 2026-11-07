package org.example.vgtuaventory.exception;

/**
 * Thrown when the email is unknown or the password does not match.
 * Mapped to HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}

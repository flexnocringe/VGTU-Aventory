package org.example.vgtuaventory.exception;

import org.springframework.http.HttpStatus;

/**
 * Bazine API klaida: zino, koki HTTP koda grazinti klientui.
 * Naujos projekto isimtys paveldi is sios klases, o GlobalExceptionHandler jas apdoroja vienoje vietoje.
 */
public abstract class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

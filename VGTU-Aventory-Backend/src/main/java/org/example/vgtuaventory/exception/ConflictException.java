package org.example.vgtuaventory.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

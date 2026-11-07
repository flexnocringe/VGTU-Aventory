package org.example.vgtuaventory.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

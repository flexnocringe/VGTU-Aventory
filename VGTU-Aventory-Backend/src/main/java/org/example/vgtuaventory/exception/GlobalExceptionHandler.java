package org.example.vgtuaventory.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Vienas klaidu apdorojimas visiems kontroleriams.
 * Grazina ta pati HTTP koda ir ta pati teksta, kuri anksciau kure kiekvienas kontrolerio metodas atskirai.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
}

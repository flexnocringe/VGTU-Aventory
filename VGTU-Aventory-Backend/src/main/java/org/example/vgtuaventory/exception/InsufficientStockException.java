package org.example.vgtuaventory.exception;

/**
 * Thrown when a sale asks for more units than the product has in stock.
 * Mapped to HTTP 409 Conflict: the request is valid, but conflicts with the current stock.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Not enough stock");
    }
}

package org.example.vgtuaventory.dto;

/**
 * Produkto kurimo ir redagavimo uzklausa (JSON laukai nepakito).
 */
public record ProductRequest(
        String productName,
        Double price,
        String productDescription,
        String photoUrl,
        Integer quantity,
        String qrCode,
        Integer categoryId
) {}

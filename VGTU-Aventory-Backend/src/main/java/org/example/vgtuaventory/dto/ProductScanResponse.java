package org.example.vgtuaventory.dto;

/**
 * Atsakymas nuskenavus produkto QR koda (JSON laukai nepakito).
 */
public record ProductScanResponse(
        int productId,
        String productName,
        Double price,
        String productDescription,
        String photoUrl,
        int quantity,
        String qrCode,
        Integer categoryId
) {}

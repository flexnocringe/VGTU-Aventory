package org.example.vgtuaventory.dto;

/**
 * One row of GET /sales/top-selling. A missing product name is returned as an empty string.
 */
public record TopSellingProductResponse(int productId, String productName, int salesCount) {

    public TopSellingProductResponse {
        productName = productName == null ? "" : productName;
    }
}

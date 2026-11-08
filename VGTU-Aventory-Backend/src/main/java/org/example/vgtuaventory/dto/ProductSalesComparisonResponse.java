package org.example.vgtuaventory.dto;

/**
 * One row of POST /sales/sales-comparison. A missing product name is returned as an empty string.
 */
public record ProductSalesComparisonResponse(int productId, String productName, int totalSalesCount) {

    public ProductSalesComparisonResponse {
        productName = productName == null ? "" : productName;
    }
}

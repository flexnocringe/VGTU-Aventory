package org.example.vgtuaventory.dto;

import java.util.List;

/**
 * Body of POST /sales/sales-comparison.
 */
public record ProductSalesComparisonRequest(List<Integer> productIds) {
}

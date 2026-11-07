package org.example.vgtuaventory.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Body of POST /sales/sales-comparison.
 * The list is copied and wrapped as unmodifiable, so the record cannot be changed from outside.
 * A missing list becomes an empty one: the service rejects both with the same
 * "At least one product must be selected" error, so the HTTP response does not change.
 * {@code List.copyOf} is not used because it rejects null elements, which the endpoint accepts today.
 */
public record ProductSalesComparisonRequest(List<Integer> productIds) {

    public ProductSalesComparisonRequest {
        List<Integer> copy = productIds == null ? new ArrayList<>() : new ArrayList<>(productIds);
        productIds = Collections.unmodifiableList(copy);
    }
}

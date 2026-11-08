package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.dto.ProductSalesComparisonRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductSalesComparisonRequestTest {

    @Test
    void shouldTurnMissingListIntoEmptyList() {
        assertEquals(List.of(), new ProductSalesComparisonRequest(null).productIds());
    }

    @Test
    void shouldNotChangeWhenCallerListChanges() {
        List<Integer> ids = new ArrayList<>(List.of(1, 2));
        ProductSalesComparisonRequest request = new ProductSalesComparisonRequest(ids);

        ids.add(3);

        assertEquals(List.of(1, 2), request.productIds());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        ProductSalesComparisonRequest request = new ProductSalesComparisonRequest(List.of(1));

        assertThrows(UnsupportedOperationException.class, () -> request.productIds().add(2));
    }

    @Test
    void shouldAcceptNullElements() {
        assertEquals(Arrays.asList(1, null), new ProductSalesComparisonRequest(Arrays.asList(1, null)).productIds());
    }
}

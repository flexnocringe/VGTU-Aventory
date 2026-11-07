package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.dto.ProductSalesComparisonResponse;
import org.example.vgtuaventory.dto.TopSellingProductResponse;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.service.SalesAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Characterization tests for the sales analytics (originally written against SaleService,
 * moved here with the methods). They pin down messages, check order, grouping and sorting.
 */
@ExtendWith(MockitoExtension.class)
class SalesAnalyticsServiceTest {

    private static final int USER_ID = 1;
    private static final LocalDate START = LocalDate.of(2026, 1, 10);
    private static final LocalDate END = LocalDate.of(2026, 1, 20);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-25T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final LocalDateTime FIRST_SALE = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    private SalesAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new SalesAnalyticsService(saleRepository, productRepository, CLOCK);
    }

    // ---- helpers ----

    private Product product(int id, String name) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductName(name);
        return p;
    }

    private Sale sale(Product product, int quantity) {
        Sale s = new Sale();
        s.setProduct(product);
        s.setQuantity(quantity);
        s.setSaleType(SaleType.SALE);
        return s;
    }

    private Sale firstSale(LocalDateTime date) {
        Sale s = new Sale();
        s.setSaleDate(date);
        return s;
    }

    private void givenFirstSaleAt(LocalDateTime date) {
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.of(firstSale(date)));
    }

    private void givenSalesInRange(List<Sale> sales) {
        when(saleRepository.findAllByOwner_IdAndSaleTypeAndSaleDateBetween(
                eq(USER_ID), eq(SaleType.SALE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sales);
    }

    private void givenOwnedProducts(Product... products) {
        for (Product p : products) {
            when(productRepository.findByProductIdAndOwner_Id(p.getProductId(), USER_ID))
                    .thenReturn(Optional.of(p));
        }
    }

    // Accessors are kept in one place, so a change of the DTO shape touches only these lines.
    private static String name(TopSellingProductResponse r) {
        return r.productName();
    }

    private static int count(TopSellingProductResponse r) {
        return r.salesCount();
    }

    private static int id(TopSellingProductResponse r) {
        return r.productId();
    }

    private static String name(ProductSalesComparisonResponse r) {
        return r.productName();
    }

    private static int count(ProductSalesComparisonResponse r) {
        return r.totalSalesCount();
    }

    private static int id(ProductSalesComparisonResponse r) {
        return r.productId();
    }

    // =====================================================================
    // getProductSalesComparison
    // =====================================================================

    @Test
    void comparison_shouldRejectEndDateInFuture() {
        LocalDate tomorrow = TODAY.plusDays(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, tomorrow, List.of(1)));

        assertEquals("End date cannot be in the future", ex.getMessage());
        verifyNoInteractions(saleRepository, productRepository);
    }

    @Test
    void comparison_shouldRejectStartDateLaterThanEndDate() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, END, START, List.of(1)));

        assertEquals("Start date cannot be later than end date", ex.getMessage());
        verifyNoInteractions(saleRepository, productRepository);
    }

    @Test
    void comparison_shouldRejectNullProductIds() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, null));

        assertEquals("At least one product must be selected", ex.getMessage());
        verifyNoInteractions(saleRepository, productRepository);
    }

    @Test
    void comparison_shouldRejectEmptyProductIds() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, List.of()));

        assertEquals("At least one product must be selected", ex.getMessage());
        verifyNoInteractions(saleRepository, productRepository);
    }

    @Test
    void comparison_shouldRejectMoreThanFiveProducts() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1, 2, 3, 4, 5, 6)));

        assertEquals("Maximum 5 products can be compared", ex.getMessage());
        verifyNoInteractions(saleRepository, productRepository);
    }

    @Test
    void comparison_shouldCheckDatesBeforeProductSelection() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, END, START, null));

        assertEquals("Start date cannot be later than end date", ex.getMessage());
    }

    @Test
    void comparison_shouldRejectWhenSystemHasNoSales() {
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1)));

        assertEquals("No sales found in the system", ex.getMessage());
        verifyNoInteractions(productRepository);
    }

    @Test
    void comparison_shouldRejectStartDateBeforeFirstRecordedSale() {
        givenFirstSaleAt(START.plusDays(1).atTime(9, 0));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1)));

        assertEquals("Start date cannot be earlier than first recorded sale date", ex.getMessage());
        verify(saleRepository, never())
                .findAllByOwner_IdAndSaleTypeAndSaleDateBetween(anyInt(), any(), any(), any());
        verifyNoInteractions(productRepository);
    }

    @Test
    void comparison_shouldRejectProductNotOwnedByUser() {
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of());
        when(productRepository.findByProductIdAndOwner_Id(7, USER_ID)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(7)));

        assertEquals("Product not found with ID: 7", ex.getMessage());
    }

    @Test
    void comparison_shouldAllowFiveProductsAndStartOnFirstSaleDay() {
        Product[] products = {product(1, "A"), product(2, "B"), product(3, "C"), product(4, "D"), product(5, "E")};
        givenFirstSaleAt(START.atTime(15, 30));
        givenSalesInRange(List.of());
        givenOwnedProducts(products);

        List<ProductSalesComparisonResponse> result =
                analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1, 2, 3, 4, 5));

        assertEquals(5, result.size());
    }

    @Test
    void comparison_shouldQuerySalesOfTypeSaleForWholeDateRange() {
        Product apple = product(1, "Apple");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of());
        givenOwnedProducts(apple);

        analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1));

        verify(saleRepository).findAllByOwner_IdAndSaleTypeAndSaleDateBetween(
                USER_ID,
                SaleType.SALE,
                START.atStartOfDay(),
                END.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @Test
    void comparison_shouldSumSelectedProductsAndIgnoreInvalidOrUnselectedSales() {
        Product apple = product(1, "Apple");
        Product pear = product(2, "Pear");
        Product other = product(3, "Not selected");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(
                sale(apple, 2),
                sale(apple, 3),
                sale(apple, 0),
                sale(null, 9),
                sale(other, 4),
                sale(pear, 1)));
        givenOwnedProducts(apple, pear);

        List<ProductSalesComparisonResponse> result =
                analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1, 2));

        assertEquals(2, result.size());
        assertEquals(1, id(result.get(0)));
        assertEquals("Apple", name(result.get(0)));
        assertEquals(5, count(result.get(0)));
        assertEquals(2, id(result.get(1)));
        assertEquals(1, count(result.get(1)));
    }

    @Test
    void comparison_shouldKeepRequestedOrderAndReturnZeroForProductsWithoutSales() {
        Product apple = product(1, "Apple");
        Product pear = product(2, "Pear");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(sale(apple, 10)));
        givenOwnedProducts(apple, pear);

        List<ProductSalesComparisonResponse> result =
                analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(2, 1));

        assertEquals(List.of(2, 1), result.stream().map(SalesAnalyticsServiceTest::id).toList());
        assertEquals(0, count(result.get(0)));
        assertEquals(10, count(result.get(1)));
    }

    @Test
    void comparison_shouldTakeNameFromOwnedProductAndReturnEmptyStringForNullName() {
        Product unnamed = product(1, null);
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(sale(product(1, "Name on sale"), 2)));
        givenOwnedProducts(unnamed);

        List<ProductSalesComparisonResponse> result =
                analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(1));

        assertEquals("", name(result.get(0)));
        assertEquals(2, count(result.get(0)));
    }

    @Test
    void comparison_shouldAcceptEndDateToday() {
        LocalDate today = TODAY;
        Product apple = product(1, "Apple");
        givenFirstSaleAt(today.atStartOfDay());
        givenSalesInRange(List.of(sale(apple, 1)));
        givenOwnedProducts(apple);

        List<ProductSalesComparisonResponse> result =
                analyticsService.getProductSalesComparison(USER_ID, today, today, List.of(1));

        assertEquals(1, count(result.get(0)));
    }

    // =====================================================================
    // getTopSellingProductsByDateRange
    // =====================================================================

    @Test
    void topSelling_shouldRejectEndDateInFuture() {
        LocalDate tomorrow = TODAY.plusDays(1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getTopSellingProductsByDateRange(USER_ID, START, tomorrow));

        assertEquals("End date cannot be in the future", ex.getMessage());
        verifyNoInteractions(saleRepository);
    }

    @Test
    void topSelling_shouldRejectStartDateLaterThanEndDate() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getTopSellingProductsByDateRange(USER_ID, END, START));

        assertEquals("Start date cannot be later than end date", ex.getMessage());
        verifyNoInteractions(saleRepository);
    }

    @Test
    void topSelling_shouldRejectWhenSystemHasNoSales() {
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END));

        assertEquals("No sales found in the system", ex.getMessage());
    }

    @Test
    void topSelling_shouldRejectStartDateBeforeFirstRecordedSale() {
        givenFirstSaleAt(START.plusDays(1).atStartOfDay());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END));

        assertEquals("Start date cannot be earlier than first recorded sale date", ex.getMessage());
        verify(saleRepository, never())
                .findAllByOwner_IdAndSaleTypeAndSaleDateBetween(anyInt(), any(), any(), any());
    }

    @Test
    void topSelling_shouldQuerySalesOfTypeSaleForWholeDateRange() {
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of());

        analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END);

        verify(saleRepository).findAllByOwner_IdAndSaleTypeAndSaleDateBetween(
                USER_ID,
                SaleType.SALE,
                START.atStartOfDay(),
                END.plusDays(1).atStartOfDay().minusNanos(1));
    }

    @Test
    void topSelling_shouldReturnEmptyListWhenNoSalesInRange() {
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(new ArrayList<>());

        assertTrue(analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END).isEmpty());
    }

    @Test
    void topSelling_shouldGroupByProductAndIgnoreZeroQuantityAndMissingProduct() {
        Product apple = product(1, "Apple");
        Product pear = product(2, "Pear");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(
                sale(apple, 2),
                sale(pear, 1),
                sale(apple, 4),
                sale(apple, 0),
                sale(null, 50)));

        List<TopSellingProductResponse> result =
                analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END);

        assertEquals(2, result.size());
        assertEquals(1, id(result.get(0)));
        assertEquals("Apple", name(result.get(0)));
        assertEquals(6, count(result.get(0)));
        assertEquals(2, id(result.get(1)));
        assertEquals(1, count(result.get(1)));
    }

    @Test
    void topSelling_shouldSortByCountDescendingThenNameIgnoringCase() {
        Product banana = product(1, "banana");
        Product apple = product(2, "Apple");
        Product cherry = product(3, "Cherry");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(
                sale(banana, 3),
                sale(cherry, 7),
                sale(apple, 3)));

        List<TopSellingProductResponse> result =
                analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END);

        assertEquals(List.of("Cherry", "Apple", "banana"),
                result.stream().map(SalesAnalyticsServiceTest::name).toList());
    }

    @Test
    void topSelling_shouldTreatNullNameAsEmptyWhenSorting() {
        Product unnamed = product(1, null);
        Product zed = product(2, "Zed");
        givenFirstSaleAt(FIRST_SALE);
        givenSalesInRange(List.of(
                sale(zed, 2),
                sale(unnamed, 2)));

        List<TopSellingProductResponse> result =
                analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END);

        assertEquals("", name(result.get(0)));
        assertEquals("Zed", name(result.get(1)));
    }
}

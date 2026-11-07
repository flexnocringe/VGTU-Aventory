package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.ProductSalesComparisonResponse;
import org.example.vgtuaventory.dto.TopSellingProductResponse;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only sales analytics (top-selling products, product comparison) for the authenticated user.
 * Extracted from SaleService, which keeps registering and listing sales.
 */
@Service
public class SalesAnalyticsService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SalesAnalyticsService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    public List<TopSellingProductResponse> getTopSellingProductsByDateRange(
            int currentUserId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);
        validateNotBeforeFirstSale(startDate);
        List<Sale> sales = findSalesInRange(currentUserId, startDate, endDate);

        Map<Integer, Product> productById = new HashMap<>();
        Map<Integer, Integer> countById = new HashMap<>();
        for (Sale sale : sales) {
            if (!isCountedSale(sale)) {
                continue;
            }

            Product product = sale.getProduct();
            productById.putIfAbsent(product.getProductId(), product);
            countById.merge(product.getProductId(), sale.getQuantity(), Integer::sum);
        }

        List<TopSellingProductResponse> result = new ArrayList<>();
        countById.forEach((productId, count) -> result.add(
                new TopSellingProductResponse(productId, productById.get(productId).getProductName(), count)));
        result.sort(
                Comparator.comparingInt(TopSellingProductResponse::salesCount).reversed()
                        .thenComparing(TopSellingProductResponse::productName, String.CASE_INSENSITIVE_ORDER)
        );

        return result;
    }

    public List<ProductSalesComparisonResponse> getProductSalesComparison(
            int currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            List<Integer> productIds
    ) {
        validateDateRange(startDate, endDate);

        if (productIds == null || productIds.isEmpty()) {
            throw new RuntimeException("At least one product must be selected");
        }

        if (productIds.size() > 5) {
            throw new RuntimeException("Maximum 5 products can be compared");
        }

        validateNotBeforeFirstSale(startDate);
        List<Sale> sales = findSalesInRange(currentUserId, startDate, endDate);

        Map<Integer, String> nameById = new HashMap<>();
        for (Integer productId : productIds) {
            Product product = productRepository.findByProductIdAndOwner_Id(productId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            nameById.put(productId, product.getProductName());
        }

        Map<Integer, Integer> totalById = new HashMap<>();
        for (Sale sale : sales) {
            if (!isCountedSale(sale)) {
                continue;
            }

            int productId = sale.getProduct().getProductId();
            if (nameById.containsKey(productId)) {
                totalById.merge(productId, sale.getQuantity(), Integer::sum);
            }
        }

        List<ProductSalesComparisonResponse> result = new ArrayList<>();
        for (Integer productId : productIds) {
            result.add(new ProductSalesComparisonResponse(
                    productId, nameById.get(productId), totalById.getOrDefault(productId, 0)));
        }

        return result;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("End date cannot be in the future");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be later than end date");
        }
    }

    private void validateNotBeforeFirstSale(LocalDate startDate) {
        Sale firstSale = saleRepository.findFirstByOrderBySaleDateAsc()
                .orElseThrow(() -> new RuntimeException("No sales found in the system"));

        if (startDate.isBefore(firstSale.getSaleDate().toLocalDate())) {
            throw new RuntimeException("Start date cannot be earlier than first recorded sale date");
        }
    }

    private List<Sale> findSalesInRange(int ownerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return saleRepository.findAllByOwner_IdAndSaleTypeAndSaleDateBetween(
                ownerId,
                SaleType.SALE,
                startDateTime,
                endDateTime
        );
    }

    private static boolean isCountedSale(Sale sale) {
        return sale.getQuantity() > 0 && sale.getProduct() != null;
    }
}

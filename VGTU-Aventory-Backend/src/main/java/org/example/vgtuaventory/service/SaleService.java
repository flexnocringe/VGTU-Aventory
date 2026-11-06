package org.example.vgtuaventory.service;

import jakarta.transaction.Transactional;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Sale registerSale(SaleRequest request, int currentUserId) {
        if (request.quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = productRepository.findByProductIdAndOwner_Id(request.productId, currentUserId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId));

        User owner = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));

        if (request.ownerId != 0 && request.ownerId != currentUserId) {
            throw new IllegalStateException("You can only create sales for your own account");
        }

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setOwner(owner);
        sale.setQuantity(request.quantity);
        sale.setSaleLocation(request.saleLocation);
        sale.setSaleType(request.saleType);
        sale.setSaleNote(request.saleNote);
        sale.setSaleDate(java.time.LocalDateTime.now());

        sale.setSaleProductPrice(product.getPrice());
        sale.setTotalPrice(product.getPrice() * request.quantity);

        if (request.saleType == SaleType.SALE) {
            if (request.quantity > product.getQuantity()) {
                throw new RuntimeException("Not enough stock");
            }
            product.setQuantity(product.getQuantity() - request.quantity);
        } else {
            product.setQuantity(product.getQuantity() + request.quantity);
        }

        productRepository.save(product);
        return saleRepository.save(sale);
    }

    public List<Sale> getSalesByUser(int userId) {
        return saleRepository.findByOwnerId(userId);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<TopSellingProductResponse> getTopSellingProductsByDateRange(
            int currentUserId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);
        validateNotBeforeFirstSale(startDate);
        List<Sale> sales = findSalesInRange(currentUserId, startDate, endDate);

        Map<Integer, TopSellingProductResponse> grouped = new HashMap<>();
        for (Sale sale : sales) {
            if (!isCountedSale(sale)) {
                continue;
            }

            Product product = sale.getProduct();
            int productId = product.getProductId();
            String productName = product.getProductName();

            TopSellingProductResponse current = grouped.get(productId);
            if (current == null) {
                grouped.put(productId, new TopSellingProductResponse(productId, productName, sale.getQuantity()));
            } else {
                current.salesCount += sale.getQuantity();
            }
        }

        List<TopSellingProductResponse> result = new ArrayList<>(grouped.values());
        result.sort(
                Comparator.comparingInt(TopSellingProductResponse::getSalesCount).reversed()
                        .thenComparing(TopSellingProductResponse::getProductName, String.CASE_INSENSITIVE_ORDER)
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

        Map<Integer, ProductSalesComparisonResponse> productMap = new HashMap<>();
        for (Integer productId : productIds) {
            Product product = productRepository.findByProductIdAndOwner_Id(productId, currentUserId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            productMap.put(productId, new ProductSalesComparisonResponse(productId, product.getProductName(), 0));
        }

        for (Sale sale : sales) {
            if (!isCountedSale(sale)) {
                continue;
            }

            int productId = sale.getProduct().getProductId();
            if (productMap.containsKey(productId)) {
                ProductSalesComparisonResponse response = productMap.get(productId);
                response.totalSalesCount += sale.getQuantity();
            }
        }

        List<ProductSalesComparisonResponse> result = new ArrayList<>();
        for (Integer productId : productIds) {
            result.add(productMap.get(productId));
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

    public static class SaleRequest {
        public int productId;
        public int ownerId;
        public int quantity;
        public String saleLocation;
        public SaleType saleType;
        public String saleNote;
    }

    public static class TopSellingProductResponse {
        public int productId;
        public String productName;
        public int salesCount;

        public TopSellingProductResponse(int productId, String productName, int salesCount) {
            this.productId = productId;
            this.productName = productName;
            this.salesCount = salesCount;
        }

        public String getProductName() {
            return productName == null ? "" : productName;
        }

        public int getSalesCount() {
            return salesCount;
        }
    }

    public static class ProductSalesComparisonResponse {
        public int productId;
        public String productName;
        public int totalSalesCount;

        public ProductSalesComparisonResponse(int productId, String productName, int totalSalesCount) {
            this.productId = productId;
            this.productName = productName;
            this.totalSalesCount = totalSalesCount;
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName == null ? "" : productName;
        }

        public int getTotalSalesCount() {
            return totalSalesCount;
        }
    }
}
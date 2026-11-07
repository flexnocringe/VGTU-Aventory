package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.dto.ProductSalesComparisonRequest;
import org.example.vgtuaventory.dto.ProductSalesComparisonResponse;
import org.example.vgtuaventory.dto.SaleRequest;
import org.example.vgtuaventory.dto.TopSellingProductResponse;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.service.SaleService;
import org.example.vgtuaventory.service.SalesAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.vgtuaventory.utils.AuthSessionAttributes;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final SalesAnalyticsService salesAnalyticsService;

    public SaleController(SaleService saleService, SalesAnalyticsService salesAnalyticsService) {
        this.saleService = saleService;
        this.salesAnalyticsService = salesAnalyticsService;
    }

    @PostMapping
    public ResponseEntity<?> createSale(
            @RequestBody SaleRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId){
        try {
            Sale savedSale = saleService.registerSale(request, currentUserId);
            return ResponseEntity.ok(savedSale);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Sale>> getUserSales(
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {

        return ResponseEntity.ok(saleService.getSalesByUser(currentUserId));
    }

    @GetMapping("/top-selling/{startDate}/{endDate}")
    public ResponseEntity<?> getTopSellingProducts(
            @PathVariable LocalDate startDate,
            @PathVariable LocalDate endDate,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        try {
            List<TopSellingProductResponse> topSellingProducts =
                    salesAnalyticsService.getTopSellingProductsByDateRange(currentUserId, startDate, endDate);
            return ResponseEntity.ok(topSellingProducts);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/sales-comparison/{startDate}/{endDate}")
    public ResponseEntity<?> getProductSalesComparison(
            @PathVariable LocalDate startDate,
            @PathVariable LocalDate endDate,
            @RequestBody ProductSalesComparisonRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        try {
            List<ProductSalesComparisonResponse> comparison =
                    salesAnalyticsService.getProductSalesComparison(currentUserId, startDate, endDate, request.productIds());
            return ResponseEntity.ok(comparison);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
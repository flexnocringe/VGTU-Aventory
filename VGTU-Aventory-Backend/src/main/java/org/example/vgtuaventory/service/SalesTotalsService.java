package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Totals over a user's sales in a date interval (revenue, units sold, per-sale prices).
 * Moved out of TotalSalesController, which now only parses dates and formats JSON.
 * Validation errors are IllegalArgumentException with the messages the clients already show.
 */
@Service
public class SalesTotalsService {

    private final SaleRepository saleRepository;
    private final Clock clock;

    public SalesTotalsService(SaleRepository saleRepository, Clock clock) {
        this.saleRepository = saleRepository;
        this.clock = clock;
    }

    public Map<Integer, Double> totalPriceBySaleId(int ownerId, LocalDate startDate, LocalDate endDate) {
        Map<Integer, Double> totalPriceBySaleId = new HashMap<>();
        for (Sale sale : findSalesInInterval(ownerId, startDate, endDate)) {
            totalPriceBySaleId.put(sale.getId(), sale.getTotalPrice());
        }
        return totalPriceBySaleId;
    }

    public double totalProfit(int ownerId, LocalDate startDate, LocalDate endDate) {
        double sum = 0.0;
        for (Sale sale : findSalesInInterval(ownerId, startDate, endDate)) {
            sum += sale.getTotalPrice();
        }
        return sum;
    }

    public int totalSalesCount(int ownerId, LocalDate startDate, LocalDate endDate) {
        int totalSalesCount = 0;
        for (Sale sale : findSalesInInterval(ownerId, startDate, endDate)) {
            if (sale.getQuantity() < 0) {
                throw new IllegalArgumentException("Sales count cannot be negative.");
            }
            totalSalesCount += sale.getQuantity();
        }
        return totalSalesCount;
    }

    public Optional<LocalDate> firstSaleDate() {
        return saleRepository.findFirstByOrderBySaleDateAsc()
                .map(sale -> sale.getSaleDate().toLocalDate());
    }

    private List<Sale> findSalesInInterval(int ownerId, LocalDate startDate, LocalDate endDate) {
        validateInterval(startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        return saleRepository.findAllByOwner_IdAndSaleDateBetween(ownerId, startDateTime, endDateTime);
    }

    private void validateInterval(LocalDate startDate, LocalDate endDate) {
        if (endDate.isAfter(LocalDate.now(clock))) {
            throw new IllegalArgumentException("End date cannot be in the future.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be later than end date.");
        }

        LocalDate firstSaleDate = firstSaleDate()
                .orElseThrow(() -> new IllegalArgumentException("No sales found in the system."));
        if (startDate.isBefore(firstSaleDate)) {
            throw new IllegalArgumentException("Start date cannot be earlier than first recorded sale date.");
        }
    }
}

package org.example.vgtuaventory.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.utils.LocalDateTimeAdapter;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class TotalSalesController {
    private final SaleRepository saleRepository;

    @Autowired
    public TotalSalesController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    private record DateInterval(LocalDate startDate, LocalDate endDate, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.badRequest().body(response);
    }

    private DateInterval validateInterval(LocalDate parsedStartDate, LocalDate parsedEndDate) {
        if (parsedEndDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future.");
        }

        if (parsedStartDate.isAfter(parsedEndDate)) {
            throw new IllegalArgumentException("Start date cannot be later than end date.");
        }

        Optional<Sale> firstSaleOptional = saleRepository.findFirstByOrderBySaleDateAsc();
        if (firstSaleOptional.isEmpty()) {
            throw new IllegalArgumentException("No sales found in the system.");
        }

        LocalDate firstSaleDate = firstSaleOptional.get().getSaleDate().toLocalDate();
        if (parsedStartDate.isBefore(firstSaleDate)) {
            throw new IllegalArgumentException("Start date cannot be earlier than first recorded sale date.");
        }

        LocalDateTime startDate = parsedStartDate.atStartOfDay();
        LocalDateTime endDate = parsedEndDate.plusDays(1).atStartOfDay().minusNanos(1);
        return new DateInterval(parsedStartDate, parsedEndDate, startDate, endDate);
    }

    @GetMapping(value  = "/allSalesInPeriod")
    public ResponseEntity<?> findAllBySaleDateBetween(
            @RequestBody String dateInfo,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = build.setPrettyPrinting().create();
        JsonObject dateInterval = gson.fromJson(dateInfo, JsonObject.class);
        
        if (!dateInterval.has("startDate") || !dateInterval.has("endDate")) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "startDate and endDate are required");
            return ResponseEntity.badRequest().body(response);
        }

        DateInterval interval;
        try {
            LocalDate parsedStartDate = LocalDate.parse(dateInterval.get("startDate").getAsString());
            LocalDate parsedEndDate = LocalDate.parse(dateInterval.get("endDate").getAsString());
            interval = validateInterval(parsedStartDate, parsedEndDate);
        } catch (DateTimeParseException exception) {
            return badRequest("Dates must be valid ISO date strings (YYYY-MM-DD).");
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

        List<Sale> sales = saleRepository.findAllByOwner_IdAndSaleDateBetween(currentUserId, interval.startDateTime(), interval.endDateTime());
        Map<Integer, Double> totalProfit = new HashMap<>();
        for(Sale sale : sales) {
            totalProfit.put(sale.getId(), sale.getTotalPrice());
        }
        return ResponseEntity.ok(totalProfit);
    }

    @GetMapping(value = "/totalProfitInInterval/{startDate}/{endDate}")
    public ResponseEntity<?> getTotalProfitInInterval(
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = build.setPrettyPrinting().create();
        
        DateInterval interval;
        try {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            interval = validateInterval(parsedStartDate, parsedEndDate);
        } catch (DateTimeParseException exception) {
            return badRequest("Dates must be valid ISO date strings (YYYY-MM-DD).");
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

        List<Sale> sales = saleRepository.findAllByOwner_IdAndSaleDateBetween(currentUserId, interval.startDateTime(), interval.endDateTime());
        Double sum = 0.0;
        for(Sale sale : sales) {
            sum += sale.getTotalPrice();
        }
        JsonObject totalProfit = new JsonObject();
        totalProfit.addProperty("totalProfit", sum);
        return ResponseEntity.ok(gson.toJson(totalProfit));
    }

    @GetMapping(value = "/totalSalesCountInInterval/{startDate}/{endDate}")
    public ResponseEntity<?> getTotalSalesCountInInterval(
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        DateInterval interval;
        try {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            interval = validateInterval(parsedStartDate, parsedEndDate);
        } catch (DateTimeParseException exception) {
            return badRequest("Dates must be valid ISO date strings (YYYY-MM-DD).");
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }

        List<Sale> sales = saleRepository.findAllByOwner_IdAndSaleDateBetween(
                currentUserId,
                interval.startDateTime(),
                interval.endDateTime()
        );

        int totalSalesCount = 0;
        for (Sale sale : sales) {
            int quantity = sale.getQuantity();
            if (quantity < 0) {
                return badRequest("Sales count cannot be negative.");
            }
            totalSalesCount += quantity;
        }

        JsonObject totalSalesCountJson = new JsonObject();
        totalSalesCountJson.addProperty("totalSalesCount", totalSalesCount);
        return ResponseEntity.ok(gson.toJson(totalSalesCountJson));
    }

    @GetMapping(value = "/firstSaleDate")
    public ResponseEntity<?> getFirstSaleDate() {
        Optional<Sale> firstSaleOptional = saleRepository.findFirstByOrderBySaleDateAsc();
        if (firstSaleOptional.isEmpty()) {
            return badRequest("No sales found in the system.");
        }

        JsonObject response = new JsonObject();
        response.addProperty("firstSaleDate", firstSaleOptional.get().getSaleDate().toLocalDate().toString());
        return ResponseEntity.ok(response.toString());
    }

}

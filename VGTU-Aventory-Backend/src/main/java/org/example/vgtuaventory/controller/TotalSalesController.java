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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TotalSalesController {
    @Autowired
    private SaleRepository saleRepository;

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

        LocalDate parsedStartDate = LocalDate.parse(dateInterval.get("startDate").getAsString());
        LocalDate parsedEndDate = LocalDate.parse(dateInterval.get("endDate").getAsString());
        LocalDateTime startDate = parsedStartDate.atStartOfDay();
        LocalDateTime endDate = parsedEndDate.plusDays(1).atStartOfDay().minusNanos(1);

        if (startDate.isAfter(endDate)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Start date cannot be later than end date.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Sale> sales = saleRepository.findAllByOwner_IdAndSaleDateBetween(currentUserId, startDate, endDate);
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
        
        LocalDate parsedStartDate = LocalDate.parse(startDate);
        LocalDate parsedEndDate = LocalDate.parse(endDate);
        LocalDateTime startDateFormatted = parsedStartDate.atStartOfDay();
        LocalDateTime endDateFormatted = parsedEndDate.plusDays(1).atStartOfDay().minusNanos(1);

        if (parsedEndDate.isAfter(LocalDate.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "End date cannot be in the future.");
            return ResponseEntity.badRequest().body(response);
        }

        if (startDateFormatted.isAfter(endDateFormatted)) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Start date cannot be later than end date.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Sale> sales = saleRepository.findAllByOwner_IdAndSaleDateBetween(currentUserId, startDateFormatted, endDateFormatted);
        Double sum = 0.0;
        for(Sale sale : sales) {
            sum += sale.getTotalPrice();
        }
        JsonObject totalProfit = new JsonObject();
        totalProfit.addProperty("totalProfit", sum);
        return ResponseEntity.ok(gson.toJson(totalProfit));
    }

}

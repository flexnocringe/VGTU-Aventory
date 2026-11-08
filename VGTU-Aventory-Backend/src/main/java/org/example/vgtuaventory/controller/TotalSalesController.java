package org.example.vgtuaventory.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.example.vgtuaventory.service.SalesTotalsService;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@RestController
public class TotalSalesController {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SalesTotalsService salesTotalsService;

    public TotalSalesController(SalesTotalsService salesTotalsService) {
        this.salesTotalsService = salesTotalsService;
    }

    @GetMapping(value  = "/allSalesInPeriod")
    public ResponseEntity<?> findAllBySaleDateBetween(
            @RequestBody String dateInfo,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        JsonObject dateInterval = GSON.fromJson(dateInfo, JsonObject.class);
        if (!dateInterval.has("startDate") || !dateInterval.has("endDate")) {
            return badRequest("startDate and endDate are required");
        }

        return withInterval(
                dateInterval.get("startDate").getAsString(),
                dateInterval.get("endDate").getAsString(),
                (start, end) -> ResponseEntity.ok(salesTotalsService.totalPriceBySaleId(currentUserId, start, end)));
    }

    @GetMapping(value = "/totalProfitInInterval/{startDate}/{endDate}")
    public ResponseEntity<?> getTotalProfitInInterval(
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        return withInterval(startDate, endDate, (start, end) -> {
            JsonObject totalProfit = new JsonObject();
            totalProfit.addProperty("totalProfit", salesTotalsService.totalProfit(currentUserId, start, end));
            return ResponseEntity.ok(GSON.toJson(totalProfit));
        });
    }

    @GetMapping(value = "/totalSalesCountInInterval/{startDate}/{endDate}")
    public ResponseEntity<?> getTotalSalesCountInInterval(
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        return withInterval(startDate, endDate, (start, end) -> {
            JsonObject totalSalesCount = new JsonObject();
            totalSalesCount.addProperty("totalSalesCount",
                    salesTotalsService.totalSalesCount(currentUserId, start, end));
            return ResponseEntity.ok(GSON.toJson(totalSalesCount));
        });
    }

    @GetMapping(value = "/firstSaleDate")
    public ResponseEntity<?> getFirstSaleDate() {
        Optional<LocalDate> firstSaleDate = salesTotalsService.firstSaleDate();
        if (firstSaleDate.isEmpty()) {
            return badRequest("No sales found in the system.");
        }

        JsonObject response = new JsonObject();
        response.addProperty("firstSaleDate", firstSaleDate.get().toString());
        return ResponseEntity.ok(response.toString());
    }

    /**
     * Parses both ISO dates and runs the action; parse and validation errors become 400 {"error": ...}.
     */
    private ResponseEntity<?> withInterval(String startDate, String endDate,
                                           BiFunction<LocalDate, LocalDate, ResponseEntity<?>> action) {
        try {
            return action.apply(LocalDate.parse(startDate), LocalDate.parse(endDate));
        } catch (DateTimeParseException exception) {
            return badRequest("Dates must be valid ISO date strings (YYYY-MM-DD).");
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}

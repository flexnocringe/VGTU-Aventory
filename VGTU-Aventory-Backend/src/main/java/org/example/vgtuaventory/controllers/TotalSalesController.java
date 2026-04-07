package org.example.vgtuaventory.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repositories.SaleRepository;
import org.example.vgtuaventory.utils.LocalDateAdapter;
import org.example.vgtuaventory.utils.LocalDateTimeAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TotalSalesController {
    @Autowired
    private SaleRepository saleRepository;

    @GetMapping(value  = "/allSalesInPeriod")
    public @ResponseBody Map<Integer, Double> findAllBySaleDateBetween(@RequestBody String dateInfo) {
        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = build.setPrettyPrinting().create();
        JsonObject dateInterval = gson.fromJson(dateInfo, JsonObject.class);
        LocalDateTime startDate = LocalDate.parse(dateInterval.get("startDate").getAsString()).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(dateInterval.get("endDate").getAsString()).atStartOfDay();
        List<Sale> sales = saleRepository.findAllBySaleDateBetween(startDate, endDate);
        Map<Integer, Double> totalProfit = new HashMap<>();
        for(Sale sale : sales) {
            totalProfit.put(sale.getId(), sale.getTotalPrice());
        }
        return totalProfit;
    }

    @GetMapping(value = "/totalProfitInInterval/{startDate}/{endDate}")
    public @ResponseBody String getTotalProfitInInterval(@PathVariable String startDate, @PathVariable String endDate) {
        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = build.setPrettyPrinting().create();
        LocalDateTime startDateFormatted = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateFormatted = LocalDate.parse(endDate).atStartOfDay();
        List<Sale> sales = saleRepository.findAllBySaleDateBetween(startDateFormatted, endDateFormatted);
        Double sum = 0.0;
        for(Sale sale : sales) {
            sum += sale.getTotalPrice();
        }
        JsonObject totalProfit = new JsonObject();
        totalProfit.addProperty("totalProfit", sum);
        return gson.toJson(totalProfit);
    }

}

package org.example.vgtuaventory.unitTests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.controller.TotalSalesController;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalProfitTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private TotalSalesController totalSalesController;

    @Test
    void findAllBySaleDateBetween_returnsSaleIdToTotalPriceMap() {
        Sale sale1 = new Sale();
        sale1.setId(1);
        sale1.setTotalPrice(10.5);
        User owner = new User();
        owner.setId(1);
        sale1.setOwner(owner);

        Sale sale2 = new Sale();
        sale2.setId(2);
        sale2.setTotalPrice(22.0);
        sale2.setOwner(owner);

        when(saleRepository.findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00")))
                .thenReturn(List.of(sale1, sale2));

        String dateInfo = "{\"startDate\":\"2026-03-01\",\"endDate\":\"2026-03-10\"}";

        ResponseEntity<?> response = totalSalesController.findAllBySaleDateBetween(dateInfo, 1);
        Map<Integer, Double> result = (Map<Integer, Double>) response.getBody();

        assertEquals(2, result.size());
        assertEquals(10.5, result.get(1));
        assertEquals(22.0, result.get(2));
        verify(saleRepository, times(1)).findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00"));
    }

    @Test
    void getTotalProfitInInterval_returnsSummedTotalProfitAsJson() {
        Sale sale1 = new Sale();
        sale1.setTotalPrice(11.25);
        User owner = new User();
        owner.setId(1);
        sale1.setOwner(owner);

        Sale sale2 = new Sale();
        sale2.setTotalPrice(8.75);
        sale2.setOwner(owner);

        when(saleRepository.findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00")))
                .thenReturn(List.of(sale1, sale2));

        ResponseEntity<?> response = totalSalesController.getTotalProfitInInterval("2026-03-01", "2026-03-10", 1);
        String result = (String) response.getBody();
        JsonObject json = new Gson().fromJson(result, JsonObject.class);

        assertEquals(20.0, json.get("totalProfit").getAsDouble());
        verify(saleRepository, times(1)).findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00"));
    }

    @Test
    void getTotalProfitInInterval_returnsZeroForNoSales() {
        when(saleRepository.findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00")))
                .thenReturn(List.of());

        ResponseEntity<?> response = totalSalesController.getTotalProfitInInterval("2026-03-01", "2026-03-10", 1);
        String result = (String) response.getBody();
        JsonObject json = new Gson().fromJson(result, JsonObject.class);

        assertEquals(0.0, json.get("totalProfit").getAsDouble());
        verify(saleRepository, times(1)).findAllByOwner_IdAndSaleDateBetween(1,
                LocalDateTime.parse("2026-03-01T00:00:00"),
                LocalDateTime.parse("2026-03-10T00:00:00"));
    }

    @Test
    void findAllBySaleDateBetween_throwsWhenDateFieldIsMissing() {
        String invalidDateInfo = "{\"startDate\":\"2026-03-01\"}";

        ResponseEntity<?> response = totalSalesController.findAllBySaleDateBetween(invalidDateInfo, 1);
        assertEquals(400, response.getStatusCode().value());
    }
}


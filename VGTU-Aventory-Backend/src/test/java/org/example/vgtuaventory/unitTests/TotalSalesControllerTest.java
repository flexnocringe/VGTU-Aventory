package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.controller.TotalSalesController;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.service.SalesTotalsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for the error paths of TotalSalesController, written before its
 * validation and totals moved into SalesTotalsService. Error bodies are JSON {"error": "..."}.
 * The controller runs with the real service over a mocked repository.
 */
@ExtendWith(MockitoExtension.class)
class TotalSalesControllerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private SaleRepository saleRepository;

    // Built in one place so a constructor change only touches this method.
    private TotalSalesController controller() {
        return new TotalSalesController(new SalesTotalsService(saleRepository, CLOCK));
    }

    private static String today() {
        return LocalDate.now(CLOCK).toString();
    }

    private static String tomorrow() {
        return LocalDate.now(CLOCK).plusDays(1).toString();
    }

    private void givenFirstSaleAt(String isoDateTime) {
        Sale first = new Sale();
        first.setSaleDate(LocalDateTime.parse(isoDateTime));
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.of(first));
    }

    private static void assertError(ResponseEntity<?> response, String message) {
        assertEquals(400, response.getStatusCode().value());
        assertEquals(Map.of("error", message), response.getBody());
    }

    @Test
    void totalProfit_shouldRejectInvalidDateFormat() {
        assertError(controller().getTotalProfitInInterval("2026-13-01", "2026-03-10", 1),
                "Dates must be valid ISO date strings (YYYY-MM-DD).");
        verifyNoInteractions(saleRepository);
    }

    @Test
    void totalProfit_shouldRejectEndDateInFuture() {
        assertError(controller().getTotalProfitInInterval(today(), tomorrow(), 1),
                "End date cannot be in the future.");
        verifyNoInteractions(saleRepository);
    }

    @Test
    void totalProfit_shouldRejectStartDateLaterThanEndDate() {
        assertError(controller().getTotalProfitInInterval("2026-03-10", "2026-03-01", 1),
                "Start date cannot be later than end date.");
        verifyNoInteractions(saleRepository);
    }

    @Test
    void totalProfit_shouldRejectWhenSystemHasNoSales() {
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.empty());

        assertError(controller().getTotalProfitInInterval("2026-03-01", "2026-03-10", 1),
                "No sales found in the system.");
        verify(saleRepository, never()).findAllByOwner_IdAndSaleDateBetween(anyInt(), any(), any());
    }

    @Test
    void totalProfit_shouldRejectStartDateBeforeFirstRecordedSale() {
        givenFirstSaleAt("2026-03-02T09:00:00");

        assertError(controller().getTotalProfitInInterval("2026-03-01", "2026-03-10", 1),
                "Start date cannot be earlier than first recorded sale date.");
    }

    @Test
    void totalProfit_shouldAcceptEndDateToday() {
        givenFirstSaleAt("2026-01-01T00:00:00");
        when(saleRepository.findAllByOwner_IdAndSaleDateBetween(anyInt(), any(), any())).thenReturn(List.of());

        assertEquals(200, controller().getTotalProfitInInterval("2026-01-01", today(), 1).getStatusCode().value());
    }

    @Test
    void totalSalesCount_shouldRejectNegativeQuantity() {
        givenFirstSaleAt("2026-03-01T00:00:00");
        Sale ok = new Sale();
        ok.setQuantity(3);
        Sale negative = new Sale();
        negative.setQuantity(-1);
        when(saleRepository.findAllByOwner_IdAndSaleDateBetween(anyInt(), any(), any()))
                .thenReturn(List.of(ok, negative));

        assertError(controller().getTotalSalesCountInInterval("2026-03-01", "2026-03-10", 1),
                "Sales count cannot be negative.");
    }

    @Test
    void totalSalesCount_shouldRejectInvalidDateFormat() {
        assertError(controller().getTotalSalesCountInInterval("not-a-date", "2026-03-10", 1),
                "Dates must be valid ISO date strings (YYYY-MM-DD).");
    }

    @Test
    void allSalesInPeriod_shouldRejectInvalidDateFormat() {
        assertError(controller().findAllBySaleDateBetween("{\"startDate\":\"x\",\"endDate\":\"2026-03-10\"}", 1),
                "Dates must be valid ISO date strings (YYYY-MM-DD).");
    }

    @Test
    void allSalesInPeriod_shouldRequireBothDates() {
        assertError(controller().findAllBySaleDateBetween("{\"endDate\":\"2026-03-10\"}", 1),
                "startDate and endDate are required");
    }

    @Test
    void firstSaleDate_shouldReturnDateOfFirstSaleAsJson() {
        givenFirstSaleAt("2026-03-01T15:30:00");

        ResponseEntity<?> response = controller().getFirstSaleDate();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("{\"firstSaleDate\":\"2026-03-01\"}", response.getBody());
    }

    @Test
    void firstSaleDate_shouldRejectWhenSystemHasNoSales() {
        when(saleRepository.findFirstByOrderBySaleDateAsc()).thenReturn(Optional.empty());

        assertError(controller().getFirstSaleDate(), "No sales found in the system.");
    }
}

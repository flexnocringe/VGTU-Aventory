package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.dto.ProductSalesComparisonResponse;
import org.example.vgtuaventory.dto.SaleRequest;
import org.example.vgtuaventory.dto.TopSellingProductResponse;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.service.SaleService;
import org.example.vgtuaventory.service.SalesAnalyticsService;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the HTTP/JSON contract of /sales that the web and Android clients rely on:
 * request field names, response field names and plain-text error bodies.
 */
@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    private static final int USER_ID = 1;
    private static final LocalDate START = LocalDate.of(2026, 1, 10);
    private static final LocalDate END = LocalDate.of(2026, 1, 20);

    private MockMvc mockMvc;

    @Mock
    private SaleService saleService;

    @Mock
    private SalesAnalyticsService analyticsService;

    @InjectMocks
    private SaleController saleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(saleController).build();
    }

    // Accessors kept in one place so a DTO shape change only touches these lines.
    private static int productId(SaleRequest r) { return r.getProductId(); }
    private static int ownerId(SaleRequest r) { return r.getOwnerId(); }
    private static int quantity(SaleRequest r) { return r.getQuantity(); }
    private static String location(SaleRequest r) { return r.getSaleLocation(); }
    private static SaleType type(SaleRequest r) { return r.getSaleType(); }
    private static String note(SaleRequest r) { return r.getSaleNote(); }

    @Test
    void createSale_shouldBindAllRequestFields() throws Exception {
        when(saleService.registerSale(any(), eq(USER_ID))).thenReturn(new Sale());

        mockMvc.perform(post("/sales")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":7,"ownerId":1,"quantity":3,
                                 "saleLocation":"Vilnius","saleType":"RETURN","saleNote":"gift"}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<SaleRequest> captor = ArgumentCaptor.forClass(SaleRequest.class);
        verify(saleService).registerSale(captor.capture(), eq(USER_ID));
        SaleRequest r = captor.getValue();
        assertEquals(7, productId(r));
        assertEquals(1, ownerId(r));
        assertEquals(3, quantity(r));
        assertEquals("Vilnius", location(r));
        assertEquals(SaleType.RETURN, type(r));
        assertEquals("gift", note(r));
    }

    @Test
    void createSale_shouldDefaultMissingFields() throws Exception {
        when(saleService.registerSale(any(), eq(USER_ID))).thenReturn(new Sale());

        mockMvc.perform(post("/sales")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":7,\"quantity\":2,\"saleType\":\"SALE\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<SaleRequest> captor = ArgumentCaptor.forClass(SaleRequest.class);
        verify(saleService).registerSale(captor.capture(), eq(USER_ID));
        SaleRequest r = captor.getValue();
        assertEquals(0, ownerId(r));
        assertEquals(null, location(r));
        assertEquals(null, note(r));
    }

    @Test
    void createSale_shouldReturnPlainTextBadRequestOnRuntimeException() throws Exception {
        when(saleService.registerSale(any(), anyInt())).thenThrow(new RuntimeException("Not enough stock"));

        mockMvc.perform(post("/sales")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":7,\"quantity\":99,\"saleType\":\"SALE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough stock"));
    }

    @Test
    void createSale_shouldReturnForbiddenOnIllegalStateException() throws Exception {
        when(saleService.registerSale(any(), anyInt()))
                .thenThrow(new IllegalStateException("You can only create sales for your own account"));

        mockMvc.perform(post("/sales")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":7,\"ownerId\":2,\"quantity\":1,\"saleType\":\"SALE\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only create sales for your own account"));
    }

    @Test
    void topSelling_shouldSerializeExactFieldsAndEmptyStringForNullName() throws Exception {
        when(analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END)).thenReturn(List.of(
                new TopSellingProductResponse(3, "Milk", 5),
                new TopSellingProductResponse(4, null, 2)));

        mockMvc.perform(get("/sales/top-selling/2026-01-10/2026-01-20")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"productId":3,"productName":"Milk","salesCount":5},
                         {"productId":4,"productName":"","salesCount":2}]
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void topSelling_shouldReturnPlainTextBadRequestOnRuntimeException() throws Exception {
        when(analyticsService.getTopSellingProductsByDateRange(USER_ID, START, END))
                .thenThrow(new RuntimeException("No sales found in the system"));

        mockMvc.perform(get("/sales/top-selling/2026-01-10/2026-01-20")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No sales found in the system"));
    }

    @Test
    void comparison_shouldBindProductIdsAndSerializeExactFields() throws Exception {
        when(analyticsService.getProductSalesComparison(USER_ID, START, END, List.of(3, 4))).thenReturn(List.of(
                new ProductSalesComparisonResponse(3, "Milk", 5),
                new ProductSalesComparisonResponse(4, null, 0)));

        mockMvc.perform(post("/sales/sales-comparison/2026-01-10/2026-01-20")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\":[3,4]}"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"productId":3,"productName":"Milk","totalSalesCount":5},
                         {"productId":4,"productName":"","totalSalesCount":0}]
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void comparison_shouldReturnPlainTextBadRequestOnRuntimeException() throws Exception {
        when(analyticsService.getProductSalesComparison(eq(USER_ID), eq(START), eq(END), any()))
                .thenThrow(new RuntimeException("Maximum 5 products can be compared"));

        mockMvc.perform(post("/sales/sales-comparison/2026-01-10/2026-01-20")
                        .requestAttr(AuthSessionAttributes.CURRENT_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productIds\":[1,2,3,4,5,6]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum 5 products can be compared"));
    }
}

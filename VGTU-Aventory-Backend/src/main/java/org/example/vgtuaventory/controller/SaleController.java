package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.dto.SaleRequestDTO;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody SaleRequestDTO dto) {
        try {
            Sale savedSale = saleService.registerSale(dto);
            return ResponseEntity.ok(savedSale);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.vgtuaventory.utils.AuthSessionAttributes;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<?> createSale(
            @RequestBody SaleService.SaleRequest request,
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
}
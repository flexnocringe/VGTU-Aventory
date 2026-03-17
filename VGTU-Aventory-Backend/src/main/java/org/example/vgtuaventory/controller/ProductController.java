package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Integer categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);

        if (products.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of("message", "Prekių šioje kategorijoje nėra")
            );
        }

        return ResponseEntity.ok(products);
    }
}
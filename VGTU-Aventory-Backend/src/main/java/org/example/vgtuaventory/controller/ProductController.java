package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.dto.ProductMapper;
import org.example.vgtuaventory.dto.ProductRequest;
import org.example.vgtuaventory.dto.ProductScanResponse;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.service.ProductService;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> list(
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        return ResponseEntity.ok(productService.getProductsForOwner(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        return ResponseEntity.ok(productService.getProductForOwner(id, currentUserId));
    }

    @GetMapping("/scan")
    public ResponseEntity<ProductScanResponse> getByQrCode(
            @RequestParam String qrCode,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product product = productService.getProductByQrCodeForOwner(qrCode, currentUserId);
        return ResponseEntity.ok(ProductMapper.toScanResponse(product));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product saved = productService.createProduct(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable int id,
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        productService.deleteProduct(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}

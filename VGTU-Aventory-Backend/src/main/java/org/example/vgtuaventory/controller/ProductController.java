package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found"));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        if (request.productName() == null || request.productName().isBlank()) {
            return ResponseEntity.badRequest().body("productName is required");
        }
        if (productRepository.existsByProductName(request.productName().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("productName already exists");
        }

        Product p = new Product();
        p.setProductName(request.productName().trim());
        p.setPrice(request.price());
        p.setProductDescription(request.productDescription());
        p.setPhotoUrl(request.photoUrl());
        p.setQuantity(request.quantity());
        p.setQrCode(request.qrCode());

        Product saved = productRepository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody ProductRequest request) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(existing -> {
                    if (request.productName() != null && !request.productName().isBlank()) {
                        String newName = request.productName().trim();
                        boolean nameTaken = productRepository.findByProductName(newName)
                                .filter(p -> p.getProductId() != id)
                                .isPresent();
                        if (nameTaken) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("productName already exists");
                        }
                        existing.setProductName(newName);
                    }

                    if (request.price() != null) existing.setPrice(request.price());
                    if (request.productDescription() != null) existing.setProductDescription(request.productDescription());
                    if (request.photoUrl() != null) existing.setPhotoUrl(request.photoUrl());
                    if (request.quantity() != null) existing.setQuantity(request.quantity());
                    if (request.qrCode() != null) existing.setQrCode(request.qrCode());

                    Product saved = productRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record ProductRequest(
            String productName,
            Double price,
            String productDescription,
            String photoUrl,
            Integer quantity,
            String qrCode
    ) {}
}
package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.example.vgtuaventory.utils.AuthSessionAttributes;

@CrossOrigin(origins = "http://localhost:3000")

@RestController
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> list(@RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        return ResponseEntity.ok(productRepository.findAllByOwner_Id(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(product -> {
                    if (product.getOwner() == null || product.getOwner().getId() != currentUserId) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own products");
                    }
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found"));
    }

    @GetMapping("/scan")
    public ResponseEntity<?> getByQrCode(
            @RequestParam String qrCode,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        try {
            Product product = productService.getProductByQrCodeForOwner(qrCode, currentUserId);
            return ResponseEntity.ok(new ProductScanResponse(
                    product.getProductId(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getProductDescription(),
                    product.getPhotoUrl(),
                    product.getQuantity(),
                    product.getQrCode()
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        if (request.productName() == null || request.productName().isBlank()) {
            return ResponseEntity.badRequest().body("productName is required");
        }
        if (productRepository.existsByProductNameAndOwner_Id(request.productName().trim(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("productName already exists");
        }

        Product p = new Product();
        User owner = new User();
        owner.setId(currentUserId);
        p.setOwner(owner);
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
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        return productRepository.findByProductIdAndOwner_Id(id, currentUserId)
                .<ResponseEntity<?>>map(existing -> {
                    if (request.productName() != null && !request.productName().isBlank()) {
                        String newName = request.productName().trim();
                        boolean nameTaken = productRepository.findByProductNameAndOwner_Id(newName, currentUserId)
                                .filter(p -> p.getProductId() != id)
                                .isPresent();
                        if (nameTaken) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("productName already exists");
                        }
                        existing.setProductName(newName);
                    }


                    Product saved = productRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only modify your own products"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        if (product.getOwner() == null || product.getOwner().getId() != currentUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own products");
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

        public record ProductScanResponse(
            int productId,
            String productName,
            Double price,
            String productDescription,
            String photoUrl,
            int quantity,
            String qrCode
        ) {}
}
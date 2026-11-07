package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.exception.BadRequestException;
import org.example.vgtuaventory.exception.ConflictException;
import org.example.vgtuaventory.exception.ForbiddenException;
import org.example.vgtuaventory.exception.NotFoundException;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.example.vgtuaventory.repository.ProductRepository;
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

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String NAME_ALREADY_EXISTS = "productName already exists";
    private static final String INVALID_CATEGORY = "Invalid categoryId (not found or not yours)";

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> list(
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        return ResponseEntity.ok(productRepository.findAllByOwner_Id(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
        if (product.getOwner() == null || product.getOwner().getId() != currentUserId) {
            throw new ForbiddenException("You can only access your own products");
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/scan")
    public ResponseEntity<ProductScanResponse> getByQrCode(
            @RequestParam String qrCode,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product product = productService.getProductByQrCodeForOwner(qrCode, currentUserId);

        Integer categoryId = product.getCategory() != null ? product.getCategory().getCategoryId() : null;

        return ResponseEntity.ok(new ProductScanResponse(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getProductDescription(),
                product.getPhotoUrl(),
                product.getQuantity(),
                product.getQrCode(),
                categoryId
        ));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        if (request.productName() == null || request.productName().isBlank()) {
            throw new BadRequestException("productName is required");
        }

        String name = request.productName().trim();
        if (productRepository.existsByProductNameAndOwner_Id(name, currentUserId)) {
            throw new ConflictException(NAME_ALREADY_EXISTS);
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByCategoryIdAndOwner_Id(request.categoryId(), currentUserId)
                    .orElseThrow(() -> new BadRequestException(INVALID_CATEGORY));
        }

        Product p = new Product();
        User owner = new User();
        owner.setId(currentUserId);

        p.setOwner(owner);
        p.setProductName(name);
        p.setPrice(request.price());
        p.setProductDescription(request.productDescription());
        p.setPhotoUrl(request.photoUrl());

        if (request.quantity() != null) p.setQuantity(request.quantity());
        p.setQrCode(request.qrCode());

        p.setCategory(category);

        Product saved = productRepository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable int id,
            @RequestBody ProductRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product existing = productRepository.findByProductIdAndOwner_Id(id, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You can only modify your own products"));

        if (request.productName() != null && !request.productName().isBlank()) {
            String newName = request.productName().trim();

            boolean nameTaken = productRepository.findByProductNameAndOwner_Id(newName, currentUserId)
                    .filter(p -> p.getProductId() != id)
                    .isPresent();

            if (nameTaken) {
                throw new ConflictException(NAME_ALREADY_EXISTS);
            }
            existing.setProductName(newName);
        }

        if (request.price() != null) existing.setPrice(request.price());
        if (request.productDescription() != null) existing.setProductDescription(request.productDescription());
        if (request.photoUrl() != null) existing.setPhotoUrl(request.photoUrl());
        if (request.quantity() != null) existing.setQuantity(request.quantity());
        if (request.qrCode() != null) existing.setQrCode(request.qrCode());

        // Kategorija: null - paliekama esama; nurodyta, bet netinkama - 400
        if (request.categoryId() != null) {
            existing.setCategory(categoryRepository.findByCategoryIdAndOwner_Id(request.categoryId(), currentUserId)
                    .orElseThrow(() -> new BadRequestException(INVALID_CATEGORY)));
        }

        Product saved = productRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable int id,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId
    ) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
        if (product.getOwner() == null || product.getOwner().getId() != currentUserId) {
            throw new ForbiddenException("You can only delete your own products");
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
            String qrCode,
            Integer categoryId
    ) {}

    public record ProductScanResponse(
            int productId,
            String productName,
            Double price,
            String productDescription,
            String photoUrl,
            int quantity,
            String qrCode,
            Integer categoryId
    ) {}
}

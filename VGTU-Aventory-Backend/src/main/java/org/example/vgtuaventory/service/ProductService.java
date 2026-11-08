package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.ProductRequest;
import org.example.vgtuaventory.exception.BadRequestException;
import org.example.vgtuaventory.exception.ConflictException;
import org.example.vgtuaventory.exception.ForbiddenException;
import org.example.vgtuaventory.exception.NotFoundException;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Produktu verslo taisykles ir duomenu prieiga.
 * Kontroleris tik priima HTTP uzklausa ir grazina atsakyma.
 */
@Service
public class ProductService {

    private static final String PRODUCT_NOT_FOUND = "Product not found";
    private static final String NAME_ALREADY_EXISTS = "productName already exists";
    private static final String INVALID_CATEGORY = "Invalid categoryId (not found or not yours)";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getProductsForOwner(int ownerId) {
        return productRepository.findAllByOwner_Id(ownerId);
    }

    public Product getProductForOwner(int id, int ownerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
        if (product.getOwner() == null || product.getOwner().getId() != ownerId) {
            throw new ForbiddenException("You can only access your own products");
        }
        return product;
    }

    public Product getProductByQrCodeForOwner(String qrCode, int ownerId) {
        return productRepository
                .findByQrCodeAndOwner_Id(qrCode, ownerId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
    }

    public Product createProduct(ProductRequest request, int ownerId) {
        if (request.productName() == null || request.productName().isBlank()) {
            throw new BadRequestException("productName is required");
        }

        String name = request.productName().trim();
        if (productRepository.existsByProductNameAndOwner_Id(name, ownerId)) {
            throw new ConflictException(NAME_ALREADY_EXISTS);
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByCategoryIdAndOwner_Id(request.categoryId(), ownerId)
                    .orElseThrow(() -> new BadRequestException(INVALID_CATEGORY));
        }

        Product p = new Product();
        User owner = new User();
        owner.setId(ownerId);

        p.setOwner(owner);
        p.setProductName(name);
        p.setPrice(request.price());
        p.setProductDescription(request.productDescription());
        p.setPhotoUrl(request.photoUrl());

        if (request.quantity() != null) p.setQuantity(request.quantity());
        p.setQrCode(request.qrCode());

        p.setCategory(category);

        return productRepository.save(p);
    }

    public Product updateProduct(int id, ProductRequest request, int ownerId) {
        Product existing = productRepository.findByProductIdAndOwner_Id(id, ownerId)
                .orElseThrow(() -> new ForbiddenException("You can only modify your own products"));

        if (request.productName() != null && !request.productName().isBlank()) {
            String newName = request.productName().trim();

            boolean nameTaken = productRepository.findByProductNameAndOwner_Id(newName, ownerId)
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
            existing.setCategory(categoryRepository.findByCategoryIdAndOwner_Id(request.categoryId(), ownerId)
                    .orElseThrow(() -> new BadRequestException(INVALID_CATEGORY)));
        }

        return productRepository.save(existing);
    }

    public void deleteProduct(int id, int ownerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
        if (product.getOwner() == null || product.getOwner().getId() != ownerId) {
            throw new ForbiddenException("You can only delete your own products");
        }
        productRepository.deleteById(id);
    }
}

package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.ProductMapper;
import org.example.vgtuaventory.dto.ProductRequest;
import org.example.vgtuaventory.exception.BadRequestException;
import org.example.vgtuaventory.exception.ConflictException;
import org.example.vgtuaventory.exception.ForbiddenException;
import org.example.vgtuaventory.exception.NotFoundException;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Produktų verslo taisyklės ir duomenų prieiga.
 * Kontroleris tik priima HTTP užklausą ir grąžina atsakymą.
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

    public Product getProductForOwner(int productId, int ownerId) {
        return getOwnedProduct(productId, ownerId, "You can only access your own products");
    }

    public Product getProductByQrCodeForOwner(String qrCode, int ownerId) {
        return productRepository
                .findByQrCodeAndOwner_Id(qrCode, ownerId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
    }

    public Product createProduct(ProductRequest request, int ownerId) {
        String name = requireName(request.productName());
        if (productRepository.existsByProductNameAndOwner_Id(name, ownerId)) {
            throw new ConflictException(NAME_ALREADY_EXISTS);
        }
        Category category = request.categoryId() != null ? resolveCategory(request.categoryId(), ownerId) : null;
        return productRepository.save(ProductMapper.toNewProduct(request, name, category, ownerId));
    }

    /**
     * Pastaba: neegzistuojančiam produktui grąžinama 403 (ne 404), kaip ir iki pertvarkymo.
     * Elgsena sąmoningai nekeista; žr. ataskaitos 5.2 poskyrį.
     */
    public Product updateProduct(int productId, ProductRequest request, int ownerId) {
        Product existing = productRepository.findByProductIdAndOwner_Id(productId, ownerId)
                .orElseThrow(() -> new ForbiddenException("You can only modify your own products"));

        renameIfRequested(existing, request.productName(), ownerId);
        ProductMapper.applyOptionalFields(request, existing);
        if (request.categoryId() != null) {
            existing.setCategory(resolveCategory(request.categoryId(), ownerId));
        }
        return productRepository.save(existing);
    }

    public void deleteProduct(int productId, int ownerId) {
        getOwnedProduct(productId, ownerId, "You can only delete your own products");
        productRepository.deleteById(productId);
    }

    private Product getOwnedProduct(int productId, int ownerId, String forbiddenMessage) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
        if (!isOwnedBy(product, ownerId)) {
            throw new ForbiddenException(forbiddenMessage);
        }
        return product;
    }

    private static boolean isOwnedBy(Product product, int ownerId) {
        return product.getOwner() != null && product.getOwner().getId() == ownerId;
    }

    private static String requireName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new BadRequestException("productName is required");
        }
        return productName.trim();
    }

    private void renameIfRequested(Product existing, String requestedName, int ownerId) {
        if (requestedName == null || requestedName.isBlank()) {
            return;
        }
        String newName = requestedName.trim();
        boolean nameTaken = productRepository.findByProductNameAndOwner_Id(newName, ownerId)
                .filter(other -> other.getProductId() != existing.getProductId())
                .isPresent();
        if (nameTaken) {
            throw new ConflictException(NAME_ALREADY_EXISTS);
        }
        existing.setProductName(newName);
    }

    private Category resolveCategory(int categoryId, int ownerId) {
        return categoryRepository.findByCategoryIdAndOwner_Id(categoryId, ownerId)
                .orElseThrow(() -> new BadRequestException(INVALID_CATEGORY));
    }
}

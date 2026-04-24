package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductByQrCodeForOwner(String qrCode, int ownerId) {
        return productRepository
                .findByQrCodeAndOwner_Id(qrCode, ownerId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}

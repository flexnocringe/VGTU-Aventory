package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryIdOrderByProductNameAsc(categoryId);
    }
}
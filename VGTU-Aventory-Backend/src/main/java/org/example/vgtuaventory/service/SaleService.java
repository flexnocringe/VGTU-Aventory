package org.example.vgtuaventory.service;

import jakarta.transaction.Transactional;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Sale registerSale(Sale sale) {
        // Fetch product
        Optional<Product> productOpt = productRepository.findById(sale.getProduct().getProductId());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + sale.getProduct().getProductId());
        }

        Product product = productOpt.get();

        // Check stock
        if (sale.getQuantity() > product.getQuantity()) {
            throw new RuntimeException("Not enough stock for product: " + product.getProductName());
        }

        // Calculate total price
        sale.setSaleProductPrice(product.getPrice());
        sale.setTotalPrice(product.getPrice() * sale.getQuantity());

        // Reduce product quantity
        product.setQuantity(product.getQuantity() - sale.getQuantity());
        productRepository.save(product);  // optional since transactional, but explicit save is safe

        // Save sale
        return saleRepository.save(sale);
    }
}
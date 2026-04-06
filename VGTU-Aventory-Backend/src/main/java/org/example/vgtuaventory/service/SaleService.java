package org.example.vgtuaventory.service;

import jakarta.transaction.Transactional;
import org.example.vgtuaventory.dto.SaleRequestDTO;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Sale registerSale(SaleRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getProductId()));

        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getOwnerId()));

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setOwner(owner);
        sale.setQuantity(dto.getQuantity());
        sale.setSaleLocation(dto.getSaleLocation());
        sale.setSaleType(dto.getSaleType());
        sale.setSaleNote(dto.getSaleNote());
        sale.setSaleProductPrice(product.getPrice());
        sale.setTotalPrice(product.getPrice() * dto.getQuantity());

        if (dto.getSaleType() == SaleType.SALE) {
            if (dto.getQuantity() > product.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getProductName());
            }
            product.setQuantity(product.getQuantity() - dto.getQuantity());
        } else if (dto.getSaleType() == SaleType.RETURN) {
            product.setQuantity(product.getQuantity() + dto.getQuantity());
        }

        productRepository.save(product);
        return saleRepository.save(sale);
    }
}
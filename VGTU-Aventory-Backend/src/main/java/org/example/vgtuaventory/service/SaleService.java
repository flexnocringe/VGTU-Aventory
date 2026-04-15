package org.example.vgtuaventory.service;

import jakarta.transaction.Transactional;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repositories.SaleRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Sale registerSale(SaleRequest request) {
        if (request.quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(request.productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.productId));

        User owner = userRepository.findById(request.ownerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.ownerId));

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setOwner(owner);
        sale.setQuantity(request.quantity);
        sale.setSaleLocation(request.saleLocation);
        sale.setSaleType(request.saleType);
        sale.setSaleNote(request.saleNote);
        sale.setSaleDate(java.time.LocalDateTime.now());

        sale.setSaleProductPrice(product.getPrice());
        sale.setTotalPrice(product.getPrice() * request.quantity);

        if (request.saleType == SaleType.SALE) {
            if (request.quantity > product.getQuantity()) {
                throw new RuntimeException("Not enough stock");
            }
            product.setQuantity(product.getQuantity() - request.quantity);
        } else {
            product.setQuantity(product.getQuantity() + request.quantity);
        }

        productRepository.save(product);
        return saleRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public static class SaleRequest {
        public int productId;
        public int ownerId;
        public int quantity;
        public String saleLocation;
        public SaleType saleType;
        public String saleNote;
    }
}
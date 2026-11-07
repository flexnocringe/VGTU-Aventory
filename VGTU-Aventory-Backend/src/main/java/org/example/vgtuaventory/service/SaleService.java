package org.example.vgtuaventory.service;

import jakarta.transaction.Transactional;
import org.example.vgtuaventory.dto.SaleRequest;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public Sale registerSale(SaleRequest request, int currentUserId) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = productRepository.findByProductIdAndOwner_Id(request.getProductId(), currentUserId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));

        User owner = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));

        if (request.getOwnerId() != 0 && request.getOwnerId() != currentUserId) {
            throw new IllegalStateException("You can only create sales for your own account");
        }

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setOwner(owner);
        sale.setQuantity(request.getQuantity());
        sale.setSaleLocation(request.getSaleLocation());
        sale.setSaleType(request.getSaleType());
        sale.setSaleNote(request.getSaleNote());
        sale.setSaleDate(LocalDateTime.now());

        sale.setSaleProductPrice(product.getPrice());
        sale.setTotalPrice(product.getPrice() * request.getQuantity());

        if (request.getSaleType() == SaleType.SALE) {
            if (request.getQuantity() > product.getQuantity()) {
                throw new RuntimeException("Not enough stock");
            }
            product.setQuantity(product.getQuantity() - request.getQuantity());
        } else {
            product.setQuantity(product.getQuantity() + request.getQuantity());
        }

        productRepository.save(product);
        return saleRepository.save(sale);
    }

    public List<Sale> getSalesByUser(int userId) {
        return saleRepository.findByOwnerId(userId);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
}

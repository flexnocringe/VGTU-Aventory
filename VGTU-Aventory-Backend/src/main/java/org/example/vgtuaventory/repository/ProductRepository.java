package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByQrCode(String qrCode);
}

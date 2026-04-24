package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByProductName(String productName);
    boolean existsByProductName(String productName);
    List<Product> findAllByOwner_Id(int ownerId);
    Optional<Product> findByProductIdAndOwner_Id(int productId, int ownerId);
    Optional<Product> findByProductNameAndOwner_Id(String productName, int ownerId);
    boolean existsByProductNameAndOwner_Id(String productName, int ownerId);

    Optional<Product> findByQrCode(String qrCode);
    Optional<Product> findByQrCodeAndOwner_Id(String qrCode, int ownerId);
}
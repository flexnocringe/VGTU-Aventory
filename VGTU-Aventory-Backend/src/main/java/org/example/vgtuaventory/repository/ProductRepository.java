package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
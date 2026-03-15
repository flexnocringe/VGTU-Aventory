package org.example.vgtuaventory.repositories;

import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
    Product findById(int id);
}
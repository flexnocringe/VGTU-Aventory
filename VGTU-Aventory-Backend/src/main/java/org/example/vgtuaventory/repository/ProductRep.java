package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategoryIdOrderByProductNameAsc(Integer categoryId);
}
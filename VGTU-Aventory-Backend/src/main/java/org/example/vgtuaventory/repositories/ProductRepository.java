package org.example.vgtuaventory.repositories;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Integer> {
    Sale deleteById(int id);
}

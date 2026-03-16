package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Integer> {
}
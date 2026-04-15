package org.example.vgtuaventory.repositories;

import org.example.vgtuaventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Integer> {
     Sale findById(int id);

     List<Sale> findAllBySaleDateBetween(LocalDateTime start, LocalDateTime end);
     List<Sale> findAllByOwner_Id(int ownerId);
     List<Sale> findAllByOwner_IdAndSaleDateBetween(int ownerId, LocalDateTime start, LocalDateTime end);
}

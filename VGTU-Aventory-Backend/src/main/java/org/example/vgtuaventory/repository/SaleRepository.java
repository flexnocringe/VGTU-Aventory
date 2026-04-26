package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Integer> {
     Sale findById(int id);

     List<Sale> findAllBySaleDateBetween(LocalDateTime start, LocalDateTime end);
     List<Sale> findAllByOwner_Id(int ownerId);
    List<Sale> findByOwnerId(int ownerId);
     List<Sale> findAllByOwner_IdAndSaleDateBetween(int ownerId, LocalDateTime start, LocalDateTime end);
     List<Sale> findAllByOwner_IdAndSaleTypeAndSaleDateBetween(int ownerId, SaleType saleType, LocalDateTime start, LocalDateTime end);
     Optional<Sale> findFirstByOrderBySaleDateAsc();
}

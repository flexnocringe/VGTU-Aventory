package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
    List<Category> findAllByOwner_Id(int ownerId);
    Optional<Category> findByCategoryIdAndOwner_Id(int categoryId, int ownerId);
    Optional<Category> findByCategoryNameAndOwner_Id(String categoryName, int ownerId);
    boolean existsByCategoryNameAndOwner_Id(String categoryName, int ownerId);
}
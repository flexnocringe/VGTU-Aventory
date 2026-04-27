package org.example.vgtuaventory.repository;

import org.example.vgtuaventory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}

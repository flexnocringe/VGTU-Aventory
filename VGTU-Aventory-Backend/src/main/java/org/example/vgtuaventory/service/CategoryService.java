package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category updateCategory(int id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (updatedCategory.getCategoryName() == null || updatedCategory.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        existingCategory.setCategoryName(updatedCategory.getCategoryName());
        if (updatedCategory.getColor() != null) {
            existingCategory.setColor(updatedCategory.getColor());
        }

        return categoryRepository.save(existingCategory);
    }
}

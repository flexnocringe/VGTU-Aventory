package org.example.vgtuaventory;

import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testCategoryRepository_SaveAndFind() {
        // Create and save a category
        Category category = new Category();
        category.setCategoryName("Test Category");
        category.setColor("blue");

        Category savedCategory = categoryRepository.save(category);
        assertNotNull(savedCategory.getId());
        assertEquals("Test Category", savedCategory.getCategoryName());
        assertEquals("blue", savedCategory.getColor());

        // Find the category
        Category foundCategory = categoryRepository.findById(savedCategory.getId()).orElse(null);
        assertNotNull(foundCategory);
        assertEquals("Test Category", foundCategory.getCategoryName());
        assertEquals("blue", foundCategory.getColor());
    }

    @Test
    void testCategoryRepository_Update() {
        // Create and save a category
        Category category = new Category();
        category.setCategoryName("Original Name");
        category.setColor("red");

        Category savedCategory = categoryRepository.save(category);

        // Update the category
        savedCategory.setCategoryName("Updated Name");
        savedCategory.setColor("green");
        Category updatedCategory = categoryRepository.save(savedCategory);

        // Verify update
        assertEquals("Updated Name", updatedCategory.getCategoryName());
        assertEquals("green", updatedCategory.getColor());
    }
}

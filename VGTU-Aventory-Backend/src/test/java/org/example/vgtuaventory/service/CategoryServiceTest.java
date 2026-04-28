package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void updateCategory_ValidInput_ShouldUpdateCategory() {
        // Arrange
        int categoryId = 1;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setCategoryName("Old Name");
        existingCategory.setColor("oldColor");

        Category updatedCategory = new Category();
        updatedCategory.setCategoryName("New Name");
        updatedCategory.setColor("newColor");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        // Act
        Category result = categoryService.updateCategory(categoryId, updatedCategory);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", existingCategory.getCategoryName());
        assertEquals("newColor", existingCategory.getColor());
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    void updateCategory_EmptyName_ShouldThrowException() {
        // Arrange
        int categoryId = 1;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);

        Category updatedCategory = new Category();
        updatedCategory.setCategoryName(""); // Empty name

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> categoryService.updateCategory(categoryId, updatedCategory));
        assertEquals("Category name cannot be null or empty", exception.getMessage());
    }

    @Test
    void updateCategory_CategoryNotFound_ShouldThrowException() {
        // Arrange
        int categoryId = 1;
        Category updatedCategory = new Category();
        updatedCategory.setCategoryName("New Name");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> categoryService.updateCategory(categoryId, updatedCategory));
        assertEquals("Category not found", exception.getMessage());
    }
}

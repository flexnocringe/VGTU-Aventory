package org.example.vgtuaventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void updateCategory_ValidInput_ShouldReturnOk() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        // Arrange
        int categoryId = 1;
        Category inputCategory = new Category();
        inputCategory.setCategoryName("Updated Name");
        inputCategory.setColor("blue");

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setCategoryName("Updated Name");
        updatedCategory.setColor("blue");

        when(categoryService.updateCategory(eq(categoryId), any(Category.class))).thenReturn(updatedCategory);

        // Act & Assert
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.categoryName").value("Updated Name"))
                .andExpect(jsonPath("$.color").value("blue"));
    }

    @Test
    void updateCategory_EmptyName_ShouldReturnBadRequest() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        // Arrange
        int categoryId = 1;
        Category inputCategory = new Category();
        inputCategory.setCategoryName("");

        when(categoryService.updateCategory(eq(categoryId), any(Category.class)))
                .thenThrow(new IllegalArgumentException("Category name cannot be null or empty"));

        // Act & Assert
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_CategoryNotFound_ShouldReturnNotFound() throws Exception {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        // Arrange
        int categoryId = 999;
        Category inputCategory = new Category();
        inputCategory.setCategoryName("New Name");

        when(categoryService.updateCategory(eq(categoryId), any(Category.class)))
                .thenThrow(new RuntimeException("Category not found"));

        // Act & Assert
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputCategory)))
                .andExpect(status().isNotFound());
    }
}

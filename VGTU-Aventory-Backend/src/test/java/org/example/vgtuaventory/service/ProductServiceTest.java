package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductsByCategory_ShouldReturnProducts() {
        // Arrange
        Integer categoryId = 1;
        Product product1 = new Product();
        product1.setProductId(1);
        product1.setProductName("Product A");

        Product product2 = new Product();
        product2.setProductId(2);
        product2.setProductName("Product B");

        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(productRepository.findByCategory_IdOrderByProductNameAsc(categoryId)).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productService.getProductsByCategory(categoryId);

        // Assert
        assertEquals(2, actualProducts.size());
        assertEquals("Product A", actualProducts.get(0).getProductName());
        assertEquals("Product B", actualProducts.get(1).getProductName());
        verify(productRepository, times(1)).findByCategory_IdOrderByProductNameAsc(categoryId);
    }

    @Test
    void getProductsByCategory_ShouldReturnEmptyList() {
        // Arrange
        Integer categoryId = 2;
        when(productRepository.findByCategory_IdOrderByProductNameAsc(categoryId)).thenReturn(Arrays.asList());

        // Act
        List<Product> actualProducts = productService.getProductsByCategory(categoryId);

        // Assert
        assertTrue(actualProducts.isEmpty());
        verify(productRepository, times(1)).findByCategory_IdOrderByProductNameAsc(categoryId);
    }
}

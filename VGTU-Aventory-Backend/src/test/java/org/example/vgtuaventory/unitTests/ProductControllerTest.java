package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.controller.ProductController;
import org.example.vgtuaventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    void getAllProducts_returnsAllProductsFromRepository() {
        Product p1 = new Product();
        p1.setProductId(1);
        Product p2 = new Product();
        p2.setProductId(2);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        Iterable<Product> result = productController.getAllProducts();

        assertNotNull(result);
        assertEquals(2, ((List<Product>) result).size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void editProduct_returnsNull_whenProductDoesNotExist() {
        Product incoming = new Product();
        incoming.setProductId(99);

        when(productRepository.findById(99)).thenReturn(null);

        Product result = productController.editProduct(incoming);

        assertNull(result);
        verify(productRepository, times(1)).findById(99);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void editProduct_updatesAndReturnsProduct_whenProductExists() {
        Product incoming = new Product();
        incoming.setProductId(1);
        incoming.setProductName("New Name");
        incoming.setProductDescription("New Desc");
        incoming.setPrice(12.5);
        incoming.setPhotoUrl("new-url");

        Product existing = new Product();
        existing.setProductId(1);
        existing.setProductName("Old Name");
        existing.setProductDescription("Old Desc");
        existing.setPrice(5.0);
        existing.setPhotoUrl("old-url");

        when(productRepository.findById(1))
                .thenReturn(existing) // first call inside editProduct
                .thenReturn(existing); // second call for return

        Product result = productController.editProduct(incoming);

        assertNotNull(result);
        assertEquals("New Name", result.getProductName());
        assertEquals("New Desc", result.getProductDescription());
        assertEquals(12.5, result.getPrice());
        assertEquals("new-url", result.getPhotoUrl());

        verify(productRepository, times(2)).findById(1);
        verify(productRepository, times(1)).save(existing);
    }
}

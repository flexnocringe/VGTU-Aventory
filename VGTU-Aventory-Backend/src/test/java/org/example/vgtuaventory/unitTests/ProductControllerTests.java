package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.controller.ProductController;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    void list_returnsAllProducts() {
        Product p1 = new Product();
        p1.setProductId(1);
        p1.setProductName("Pen");

        Product p2 = new Product();
        p2.setProductId(2);
        p2.setProductName("Notebook");

        when(productRepository.findAllByOwner_Id(1)).thenReturn(List.of(p1, p2));

        ResponseEntity<List<Product>> response = productController.list(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Pen", response.getBody().get(0).getProductName());
        assertEquals("Notebook", response.getBody().get(1).getProductName());

        verify(productRepository, times(1)).findAllByOwner_Id(1);
    }

    @Test
    void getById_whenFound_returnsProduct() {
        Product p = new Product();
        p.setProductId(7);
        p.setProductName("Marker");
        User owner = new User();
        owner.setId(1);
        p.setOwner(owner);

        when(productRepository.findById(7)).thenReturn(Optional.of(p));

        ResponseEntity<?> response = productController.getById(7, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Product);
        Product body = (Product) response.getBody();
        assertEquals(7, body.getProductId());
        assertEquals("Marker", body.getProductName());

        verify(productRepository, times(1)).findById(7);
    }

    @Test
    void getById_whenMissing_returns404WithMessage() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = productController.getById(999, 1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Product not found", response.getBody());

        verify(productRepository, times(1)).findById(999);
    }

    @Test
    void create_whenProductNameBlank_returns400() {
        ProductController.ProductRequest request = new ProductController.ProductRequest(
                "   ",
                1.99,
                null,
                null,
                1,
                null
        );

            ResponseEntity<?> response = productController.create(request, 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("productName is required", response.getBody());

        verify(productRepository, never()).save(any(Product.class));
        verify(productRepository, never()).existsByProductNameAndOwner_Id(anyString(), anyInt());
    }

    @Test
    void create_whenProductNameAlreadyExists_returns409() {
        when(productRepository.existsByProductNameAndOwner_Id("Pencil", 1)).thenReturn(true);

        ProductController.ProductRequest request = new ProductController.ProductRequest(
                "Pencil",
                0.50,
                null,
                null,
                10,
                null
        );

            ResponseEntity<?> response = productController.create(request, 1);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("productName already exists", response.getBody());

        verify(productRepository, times(1)).existsByProductNameAndOwner_Id("Pencil", 1);
        verify(productRepository, never()).save(any(Product.class));
    }
}
package org.example.vgtuaventory.controller;

import com.google.gson.JsonSyntaxException;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    void getAllProducts_returnsDataFromRepository() {
        List<Product> products = List.of(new Product(), new Product());
        when(productRepository.findAll()).thenReturn(products);

        Iterable<Product> result = productController.getAllProducts();

        assertSame(products, result);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void deleteProduct_deletesEachProvidedId_andReturnsSuccessMessage() {
        String requestBody = "{\"productId\":[1,2,3]}";

        String result = productController.deleteProduct(requestBody);

        assertEquals("Products deleted successfully", result);
    }

    @Test
    void deleteProduct_withEmptyProductIdList_doesNotCallDeleteById() {
        String requestBody = "{\"productId\":[]}";

        String result = productController.deleteProduct(requestBody);

        assertEquals("Products deleted successfully", result);
        verify(productRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void deleteProduct_withMalformedJson_throwsJsonSyntaxException() {
        String malformedBody = "not-json";

        assertThrows(JsonSyntaxException.class, () -> productController.deleteProduct(malformedBody));
    }

    @Test
    void deleteProduct_withoutProductIdArray_throwsNullPointerException() {
        String missingProductIdBody = "{\"ids\":[1,2,3]}";

        assertThrows(NullPointerException.class, () -> productController.deleteProduct(missingProductIdBody));
        verify(productRepository, never()).deleteById(org.mockito.ArgumentMatchers.anyInt());
    }
}


package org.example.vgtuaventory;

import org.example.vgtuaventory.dto.SaleRequestDTO;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SaleServiceTest {

    private SaleRepository saleRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private SaleService saleService;

    @BeforeEach
    void setup() {
        saleRepository = mock(SaleRepository.class);
        productRepository = mock(ProductRepository.class);
        userRepository = mock(UserRepository.class);
        saleService = new SaleService(saleRepository, productRepository, userRepository);

        // Mock save grąžina tą patį objektą
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registerSale_shouldReduceProductQuantity() {
        Product product = new Product();
        product.setProductId(1);
        product.setProductName("Zaisliukas");
        product.setPrice(3.99);
        product.setQuantity(25);

        User owner = new User();
        owner.setId(1);

        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setProductId(1);
        dto.setOwnerId(1);
        dto.setQuantity(5);
        dto.setSaleType(SaleType.SALE);
        dto.setSaleLocation("Vilnius");

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));

        saleService.registerSale(dto);

        assertEquals(20, product.getQuantity()); // quantity reduced
        verify(productRepository).save(product);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void registerSale_shouldThrowExceptionWhenStockNotEnough() {
        Product product = new Product();
        product.setProductId(1);
        product.setProductName("Zaisliukas");
        product.setPrice(3.99);
        product.setQuantity(2);

        User owner = new User();
        owner.setId(1);

        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setProductId(1);
        dto.setOwnerId(1);
        dto.setQuantity(5);
        dto.setSaleType(SaleType.SALE);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> saleService.registerSale(dto));

        // Testas nesikabino prie produkto pavadinimo
        assertTrue(exception.getMessage().startsWith("Not enough stock for product"));

        verify(productRepository, never()).save(product);
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerSale_shouldCalculateTotalPrice() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(3.0);
        product.setQuantity(10);

        User owner = new User();
        owner.setId(1);

        SaleRequestDTO dto = new SaleRequestDTO();
        dto.setProductId(1);
        dto.setOwnerId(1);
        dto.setQuantity(4);
        dto.setSaleType(SaleType.SALE);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));

        Sale savedSale = saleService.registerSale(dto);

        assertEquals(12.0, savedSale.getTotalPrice()); // 3.0 * 4
        verify(productRepository).save(product);
        verify(saleRepository).save(any(Sale.class));
    }

}
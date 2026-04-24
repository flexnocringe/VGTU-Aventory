package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.model.SaleType;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.service.SaleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SaleService saleService;

    private SaleService.SaleRequest req(int productId, int ownerId, int qty, SaleType type) {
        SaleService.SaleRequest r = new SaleService.SaleRequest();
        r.productId = productId;
        r.ownerId = ownerId;
        r.quantity = qty;
        r.saleType = type;
        r.saleLocation = "Test";
        r.saleNote = "Note";
        return r;
    }

    @Test
    void registerSale_shouldReduceProductQuantity() {

        Product product = new Product();
        product.setProductId(1);
        product.setPrice(3.99);
        product.setQuantity(25);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(saleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Sale result = saleService.registerSale(
            req(1, 1, 5, SaleType.SALE),1
        );

        assertEquals(20, product.getQuantity());
        verify(productRepository, times(1)).save(product);
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void registerSale_shouldThrowExceptionWhenStockNotEnough() {

        Product product = new Product();
        product.setProductId(1);
        product.setQuantity(2);
        product.setPrice(3.0);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> saleService.registerSale(req(1, 1, 5, SaleType.SALE),1)
        );

        assertTrue(ex.getMessage().contains("Not enough stock"));

        verify(productRepository, never()).save(any());
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

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(saleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Sale result = saleService.registerSale(
            req(1, 1, 4, SaleType.SALE),1
        );

        assertEquals(12.0, result.getTotalPrice());
        verify(productRepository, times(1)).save(product);
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void registerSale_shouldThrowExceptionWhenQuantityIsZero() {

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> saleService.registerSale(req(1, 1, 0, SaleType.SALE),1)
        );

        assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));
        verify(productRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerSale_shouldThrowExceptionWhenQuantityIsNegative() {

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> saleService.registerSale(req(1, 1, -5, SaleType.SALE),1)
        );

        assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));
        verify(productRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerSale_shouldRejectWhenRequestOwnerDoesNotMatchAuthenticatedUser() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(3.0);
        product.setQuantity(10);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> saleService.registerSale(req(1, 2, 4, SaleType.SALE),1));

        assertTrue(ex.getMessage().contains("own account"));
        verify(saleRepository, never()).save(any());
    }
}
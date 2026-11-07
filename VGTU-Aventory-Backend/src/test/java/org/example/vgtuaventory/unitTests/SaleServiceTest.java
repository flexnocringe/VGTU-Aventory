package org.example.vgtuaventory.unitTests;

import org.example.vgtuaventory.dto.SaleRequest;
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

import java.time.LocalDateTime;
import java.util.List;
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

    private SaleRequest req(int productId, int ownerId, int qty, SaleType type) {
        SaleRequest r = new SaleRequest();
        r.setProductId(productId);
        r.setOwnerId(ownerId);
        r.setQuantity(qty);
        r.setSaleType(type);
        r.setSaleLocation("Test");
        r.setSaleNote("Note");
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

    @Test
    void registerSale_returnShouldIncreaseStockWithoutStockCheck() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(2.5);
        product.setQuantity(1);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(saleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Sale result = saleService.registerSale(req(1, 1, 5, SaleType.RETURN), 1);

        assertEquals(6, product.getQuantity());
        assertEquals(SaleType.RETURN, result.getSaleType());
        assertEquals(12.5, result.getTotalPrice());
    }

    @Test
    void registerSale_shouldAllowSellingWholeStock() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(1.0);
        product.setQuantity(5);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(saleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        saleService.registerSale(req(1, 1, 5, SaleType.SALE), 1);

        assertEquals(0, product.getQuantity());
    }

    @Test
    void registerSale_shouldFillSaleFromRequestAndProduct() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(4.0);
        product.setQuantity(10);

        User owner = new User();
        owner.setId(1);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(saleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDateTime before = LocalDateTime.now();
        Sale result = saleService.registerSale(req(1, 0, 2, SaleType.SALE), 1);

        assertSame(product, result.getProduct());
        assertSame(owner, result.getOwner());
        assertEquals(2, result.getQuantity());
        assertEquals("Test", result.getSaleLocation());
        assertEquals("Note", result.getSaleNote());
        assertEquals(SaleType.SALE, result.getSaleType());
        assertEquals(4.0, result.getSaleProductPrice());
        assertEquals(8.0, result.getTotalPrice());
        assertFalse(result.getSaleDate().isBefore(before));
    }

    @Test
    void registerSale_shouldThrowWhenProductNotFound() {
        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> saleService.registerSale(req(1, 1, 2, SaleType.SALE), 1));

        assertEquals("Product not found with ID: 1", ex.getMessage());
        verifyNoInteractions(userRepository);
        verify(productRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerSale_shouldThrowWhenUserNotFound() {
        Product product = new Product();
        product.setProductId(1);
        product.setPrice(1.0);
        product.setQuantity(10);

        when(productRepository.findByProductIdAndOwner_Id(1, 1)).thenReturn(Optional.of(product));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> saleService.registerSale(req(1, 1, 2, SaleType.SALE), 1));

        assertEquals("User not found with ID: 1", ex.getMessage());
        verify(productRepository, never()).save(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void getSalesByUser_shouldReturnRepositoryResult() {
        List<Sale> sales = List.of(new Sale());
        when(saleRepository.findByOwnerId(1)).thenReturn(sales);

        assertSame(sales, saleService.getSalesByUser(1));
    }

    @Test
    void getAllSales_shouldReturnRepositoryResult() {
        List<Sale> sales = List.of(new Sale(), new Sale());
        when(saleRepository.findAll()).thenReturn(sales);

        assertSame(sales, saleService.getAllSales());
    }
}
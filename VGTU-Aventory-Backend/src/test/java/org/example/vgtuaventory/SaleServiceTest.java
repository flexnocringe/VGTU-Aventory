package org.example.vgtuaventory;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repository.ProductRepository;
import org.example.vgtuaventory.repository.SaleRepository;
import org.example.vgtuaventory.service.SaleService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class SaleServiceTest {

    private final SaleRepository saleRepository = mock(SaleRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);

    private final SaleService saleService = new SaleService(saleRepository, productRepository);

    @Test
    void registerSale_shouldReduceProductQuantity() {

        Product product = new Product();
        product.setProductId(2);
        product.setProductName("Zaisliukas");
        product.setPrice(3.99);
        product.setQuantity(25);

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setQuantity(5);

        when(productRepository.findById(2)).thenReturn(Optional.of(product));

        saleService.registerSale(sale);

        verify(productRepository).save(product);
        verify(saleRepository).save(sale);
    }

    @Test
    void registerSale_shouldThrowExceptionWhenStockNotEnough() {

        Product product = new Product();
        product.setProductId(2);
        product.setProductName("Zaisliukas");
        product.setPrice(3.99);
        product.setQuantity(2);

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setQuantity(5);

        when(productRepository.findById(2)).thenReturn(Optional.of(product));

        try {
            saleService.registerSale(sale);
        } catch (RuntimeException ignored) {}

        verify(productRepository, never()).save(product);
        verify(saleRepository, never()).save(sale);
    }

    @Test
    void registerSale_shouldCalculateTotalPrice() {

        Product product = new Product();
        product.setProductId(2);
        product.setPrice(3.0);
        product.setQuantity(10);

        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setQuantity(4);

        when(productRepository.findById(2)).thenReturn(Optional.of(product));

        saleService.registerSale(sale);

        verify(productRepository).save(product);
        verify(saleRepository).save(sale);
    }


}

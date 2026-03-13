package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/qr/{qrCode}")
    public Product getProductByQrCode(@PathVariable String qrCode) {
        return productService.getProductByQrCode(qrCode);
    }
}

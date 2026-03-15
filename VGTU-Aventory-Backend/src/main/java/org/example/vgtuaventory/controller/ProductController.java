package org.example.vgtuaventory.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.example.vgtuaventory.model.Product;
import org.example.vgtuaventory.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping(value = "/getAllProducts")
    public Iterable<Product> getAllProducts(){
        return productRepository.findAll();
    }

    @PutMapping(value = "/editProduct")
    public Product editProduct(@RequestBody Product product){
        Product productToEdit = productRepository.findById(product.getProductId());
        if(productToEdit == null) return null;
        productToEdit.setProductName(product.getProductName());
        productToEdit.setProductDescription(product.getProductDescription());
        productToEdit.setPrice(product.getPrice());
        productToEdit.setPhotoUrl(product.getPhotoUrl());
        productRepository.save(productToEdit);
    }
}
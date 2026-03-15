package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.model.Sale;
import org.example.vgtuaventory.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductDeleteController {
    @Autowired
    private ProductRepository productRepository;


}

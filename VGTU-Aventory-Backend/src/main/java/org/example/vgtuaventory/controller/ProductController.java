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

    @DeleteMapping(value = "/deleteProducts")
    public String deleteProduct(@RequestBody String idListJson) {
        Gson gson = new Gson();
        JsonObject idList = gson.fromJson(idListJson, JsonObject.class);
        List<Integer> ids = gson.fromJson(idList.getAsJsonArray("productId"), new TypeToken<List<Integer>>(){}.getType());
        System.out.println(ids);
        for (Integer id : ids) {
            productRepository.deleteById(id);
        }
        return "Products deleted successfully";
    }
}

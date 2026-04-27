package org.example.vgtuaventory.controller;

import lombok.RequiredArgsConstructor;
import org.example.vgtuaventory.model.Category;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.CategoryRepository;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CategoryRequest request,
            @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        if (request.categoryName() == null || request.categoryName().isBlank()) {
            return ResponseEntity.badRequest().body("categoryName is required!");
        }
        if (categoryRepository.existsByCategoryNameAndOwner_Id(request.categoryName().trim(), currentUserId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("categoryName already exists");
        }
//
        Category c = new Category();
        User owner = new User();
        owner.setId(currentUserId);
        c.setOwner(owner);
        c.setCategoryName(request.categoryName().trim());


        Category saved = categoryRepository.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record CategoryRequest(
            Integer categoryId,
            String categoryName
    ) {}
}
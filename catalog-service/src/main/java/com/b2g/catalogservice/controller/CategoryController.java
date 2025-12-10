package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.dto.CategoryCreateRequestDTO;
import com.b2g.catalogservice.model.Category;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.commons.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping({"", "/"})
    public ResponseEntity<List<Category>> getAllCategories() {
        // Fetch all categories from database
        List<Category> categories = categoryRepository.findAll();


        return ResponseEntity.ok(categories);
    }
    @RequireRole("ADMIN")
    @PostMapping({"", "/"})
    public ResponseEntity<Category> createCategory(@RequestBody @Valid CategoryCreateRequestDTO request) {

        // Create Category entity from request
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        // Save the category
        Category savedCategory = categoryRepository.save(category);


        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }
}

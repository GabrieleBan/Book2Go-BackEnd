package com.b2g.catalogservice.controller;

import com.b2g.catalogservice.annotation.RequireRole;
import com.b2g.catalogservice.dto.CategoryCreateRequestDTO;
import com.b2g.catalogservice.model.VO.Category;
import com.b2g.catalogservice.repository.CategoryRepository;
import com.b2g.catalogservice.service.application.CategoryApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    @GetMapping({"", "/"})
    public ResponseEntity<List<Category>> getAllCategories() {
        // Fetch all categories from database
        List<Category> categories = categoryApplicationService.getAllCategories();

        return ResponseEntity.ok(categories);
    }
    @RequireRole("ADMIN")
    @PostMapping({"", "/"})
    public ResponseEntity<Category> createCategory(@RequestBody @Valid CategoryCreateRequestDTO request) {
        Category savedCategory = categoryApplicationService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }


}
